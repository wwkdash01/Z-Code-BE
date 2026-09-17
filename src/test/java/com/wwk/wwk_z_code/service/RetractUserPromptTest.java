package com.wwk.wwk_z_code.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.mapper.AppMapper;
import com.wwk.wwk_z_code.mapper.ChatHistoryMapper;
import com.wwk.wwk_z_code.model.dto.RetractUserPromptRequestDTO;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.model.entity.ChatHistory;
import com.wwk.wwk_z_code.model.enums.MessageType;
import com.wwk.wwk_z_code.model.vo.UserVO;
import com.wwk.wwk_z_code.service.impl.ChatHistoryServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static com.wwk.wwk_z_code.constant.UserConstant.USER_LOGIN_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ChatHistoryServiceImpl 撤销用户提示词单元测试：mock mapper + 继承方法 spy，覆盖各撤销分支。
 */
@ExtendWith(MockitoExtension.class)
class RetractUserPromptTest {

    private static final Long APP_ID = 100L;

    private static final Long USER_ID = 1L;

    @Mock
    private AppMapper appMapper;

    @Mock
    private ChatHistoryMapper chatHistoryMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private ChatHistoryServiceImpl service;

    @BeforeEach
    void setUp() {
        ChatHistoryServiceImpl real = new ChatHistoryServiceImpl(appMapper);
        // mapper 是 ServiceImpl 的 protected 字段，反射注入 mock，避免 stub 落空时落到真实 mapper
        ReflectionTestUtils.setField(real, "mapper", chatHistoryMapper);
        // spy：getOne/update 等继承方法可在 spy 上直接 stub
        service = Mockito.spy(real);
    }

    // region 指定 chatHistoryId

    @Test
    void retract_withChatHistoryId_rewritesUserRowToRetraction() {
        loginAs(USER_ID);
        mockOwnedApp();
        doReturn(ChatHistory.builder().id(9L).appId(APP_ID).userId(USER_ID)
                .message("帮我写个登录页").messageType(MessageType.USER).build())
                .when(service).getOne(any(QueryWrapper.class));
        doReturn(true).when(service).update(any(ChatHistory.class), any(QueryWrapper.class));

        assertTrue(service.retractUserPrompt(retractDto(9L), request));

        ArgumentCaptor<ChatHistory> captor = ArgumentCaptor.forClass(ChatHistory.class);
        verify(service).update(captor.capture(), any(QueryWrapper.class));
        assertEquals(MessageType.RETRACTION, captor.getValue().getMessageType());
        assertNotNull(captor.getValue().getEditTime());
        assertNotNull(captor.getValue().getUpdateTime());
    }

    @Test
    void retract_withChatHistoryId_targetMissing_throwsForbidden() {
        loginAs(USER_ID);
        mockOwnedApp();
        doReturn(null).when(service).getOne(any(QueryWrapper.class));

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.retractUserPrompt(retractDto(9L), request));

        assertEquals(ErrorCode.FORBIDDEN_ERROR.getCode(), e.getCode());
        assertEquals("聊天记录不存在或已删除", e.getMessage());
        verify(service, never()).update(any(ChatHistory.class), any(QueryWrapper.class));
    }

    @Test
    void retract_withChatHistoryId_alreadyRetracted_returnsTrueWithoutUpdate() {
        loginAs(USER_ID);
        mockOwnedApp();
        doReturn(ChatHistory.builder().id(9L).appId(APP_ID).userId(USER_ID)
                .messageType(MessageType.RETRACTION).build())
                .when(service).getOne(any(QueryWrapper.class));

        assertTrue(service.retractUserPrompt(retractDto(9L), request));

        verify(service, never()).update(any(ChatHistory.class), any(QueryWrapper.class));
    }

    @Test
    void retract_withChatHistoryId_targetIsAi_throwsNotUserPrompt() {
        loginAs(USER_ID);
        mockOwnedApp();
        doReturn(ChatHistory.builder().id(9L).appId(APP_ID).userId(USER_ID)
                .messageType(MessageType.AI).build())
                .when(service).getOne(any(QueryWrapper.class));

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.retractUserPrompt(retractDto(9L), request));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("只能撤销用户提示词", e.getMessage());
        verify(service, never()).update(any(ChatHistory.class), any(QueryWrapper.class));
    }

    @Test
    void retract_withChatHistoryId_targetIsError_throwsNotUserPrompt() {
        loginAs(USER_ID);
        mockOwnedApp();
        doReturn(ChatHistory.builder().id(9L).appId(APP_ID).userId(USER_ID)
                .messageType(MessageType.ERROR).build())
                .when(service).getOne(any(QueryWrapper.class));

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.retractUserPrompt(retractDto(9L), request));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("只能撤销用户提示词", e.getMessage());
        verify(service, never()).update(any(ChatHistory.class), any(QueryWrapper.class));
    }

    // endregion

    // region 未指定 chatHistoryId（按最新一轮失败对话定位）

    @Test
    void retract_withoutId_latestIsAi_throwsNoRetractableMessage() {
        loginAs(USER_ID);
        mockOwnedApp();
        doReturn(ChatHistory.builder().id(10L).messageType(MessageType.AI).build())
                .when(service).getOne(any(QueryWrapper.class));

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.retractUserPrompt(retractDto(null), request));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("无可撤销的消息", e.getMessage());
    }

    @Test
    void retract_withoutId_latestIsError_walksBackToUserAndRetracts() {
        loginAs(USER_ID);
        mockOwnedApp();
        // 第一次 getOne 取最新一条（error），第二次往前取第一条 user/retraction（user）
        doReturn(ChatHistory.builder().id(10L).messageType(MessageType.ERROR).build())
                .doReturn(ChatHistory.builder().id(9L).messageType(MessageType.USER).build())
                .when(service).getOne(any(QueryWrapper.class));
        doReturn(true).when(service).update(any(ChatHistory.class), any(QueryWrapper.class));

        assertTrue(service.retractUserPrompt(retractDto(null), request));

        ArgumentCaptor<ChatHistory> captor = ArgumentCaptor.forClass(ChatHistory.class);
        verify(service).update(captor.capture(), any(QueryWrapper.class));
        assertEquals(MessageType.RETRACTION, captor.getValue().getMessageType());
    }

    @Test
    void retract_withoutId_candidateAlreadyRetracted_returnsTrueWithoutUpdate() {
        loginAs(USER_ID);
        mockOwnedApp();
        doReturn(ChatHistory.builder().id(10L).messageType(MessageType.ERROR).build())
                .doReturn(ChatHistory.builder().id(9L).messageType(MessageType.RETRACTION).build())
                .when(service).getOne(any(QueryWrapper.class));

        assertTrue(service.retractUserPrompt(retractDto(null), request));

        verify(service, never()).update(any(ChatHistory.class), any(QueryWrapper.class));
    }

    @Test
    void retract_withoutId_noMessages_throwsNoRetractableMessage() {
        loginAs(USER_ID);
        mockOwnedApp();
        doReturn(null).when(service).getOne(any(QueryWrapper.class));

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.retractUserPrompt(retractDto(null), request));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
        assertEquals("无可撤销的消息", e.getMessage());
    }

    // endregion

    // region 鉴权与归属校验

    @Test
    void retract_notLoggedIn_throwsNotLoginError() {
        when(request.getSession(false)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.retractUserPrompt(retractDto(9L), request));

        assertEquals(ErrorCode.NOT_LOGIN_ERROR.getCode(), e.getCode());
    }

    @Test
    void retract_appNotOwned_throwsForbidden() {
        loginAs(USER_ID);
        when(appMapper.selectOneById(any())).thenReturn(
                App.builder().id(APP_ID).createUserId(999L).build());

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.retractUserPrompt(retractDto(9L), request));

        assertEquals(ErrorCode.FORBIDDEN_ERROR.getCode(), e.getCode());
        assertEquals("无权访问该应用", e.getMessage());
    }

    // endregion

    // region 辅助方法

    private RetractUserPromptRequestDTO retractDto(Long chatHistoryId) {
        RetractUserPromptRequestDTO dto = new RetractUserPromptRequestDTO();
        dto.setAppId(APP_ID);
        dto.setChatHistoryId(chatHistoryId);
        return dto;
    }

    private void loginAs(Long userId) {
        when(request.getSession(false)).thenReturn(session);
        UserVO vo = new UserVO();
        vo.setId(userId);
        when(session.getAttribute(USER_LOGIN_STATUS)).thenReturn(vo);
    }

    private void mockOwnedApp() {
        when(appMapper.selectOneById(any())).thenReturn(
                App.builder().id(APP_ID).createUserId(USER_ID).build());
    }

    // endregion
}
