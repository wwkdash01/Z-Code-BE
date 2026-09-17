package com.wwk.wwk_z_code.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wwk.wwk_z_code.common.CursorHelper;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.mapper.AppMapper;
import com.wwk.wwk_z_code.model.dto.ChatHistoryUserCursorQueryRequestDTO;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.model.entity.ChatHistory;
import com.wwk.wwk_z_code.model.enums.MessageType;
import com.wwk.wwk_z_code.model.vo.ChatHistoryUserCursorPageVO;
import com.wwk.wwk_z_code.model.vo.ChatHistoryVO;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.wwk.wwk_z_code.constant.UserConstant.USER_LOGIN_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ChatHistoryServiceImpl 游标分页单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ChatHistoryCursorTest {

    @Mock
    private AppMapper appMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private ChatHistoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = Mockito.spy(new ChatHistoryServiceImpl(appMapper));
    }

    // region CursorHelper 编解码测试

    @Test
    void cursor_encodeDecode_roundTrip() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 10, 12, 0);
        CursorHelper.CursorEntry entry = new CursorHelper.CursorEntry(now, 100L);
        String encoded = CursorHelper.encode(entry);

        assertNotNull(encoded);

        CursorHelper.CursorEntry decoded = CursorHelper.decode(encoded);
        assertEquals(now, decoded.getCreateTime());
        assertEquals(Long.valueOf(100L), decoded.getId());
    }

    @Test
    void cursor_decode_blank_returnsNull() {
        assertNull(CursorHelper.decode(""));
        assertNull(CursorHelper.decode(null));
    }

    @Test
    void cursor_decode_invalidFormat_throwsParamError() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> CursorHelper.decode("not-base64!!!invalid"));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), e.getCode());
    }

    // endregion

    // region queryCursor 测试

    @Test
    void queryCursor_noCursor_firstLoad_returnsMessages_withHasMoreTrue() {
        loginAs(1L);

        LocalDateTime t1 = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 9, 10, 11, 0);
        ChatHistory h1 = ChatHistory.builder().id(1L).appId(100L).userId(1L).message("msg1").createTime(t1).build();
        ChatHistory h2 = ChatHistory.builder().id(2L).appId(100L).userId(1L).message("msg2").createTime(t2).build();
        ChatHistory h3 = ChatHistory.builder().id(3L).appId(100L).userId(1L).message("msg3").createTime(t2.plusMinutes(1)).build();

        Page<ChatHistory> dbPage = new Page<>(List.of(h3, h2, h1), 1, 11, 3);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));

        ChatHistoryUserCursorQueryRequestDTO dto = new ChatHistoryUserCursorQueryRequestDTO();
        dto.setAppId(100L);
        dto.setPageSize(2);

        ChatHistoryUserCursorPageVO vo = service.queryChatHistoryByCursor(dto, request);

        assertEquals(2, ((List<?>) vo.getRecords()).size());
        assertTrue(vo.getHasMore());
        assertNotNull(vo.getNextCursor());
    }

    @Test
    void queryCursor_noCursor_exhausted_returnsHasMoreFalse() {
        loginAs(1L);

        LocalDateTime t = LocalDateTime.of(2026, 9, 10, 10, 0);
        ChatHistory h1 = ChatHistory.builder().id(1L).appId(100L).userId(1L).message("msg1").createTime(t).build();

        Page<ChatHistory> dbPage = new Page<>(List.of(h1), 1, 10, 1);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));

        ChatHistoryUserCursorQueryRequestDTO dto = new ChatHistoryUserCursorQueryRequestDTO();
        dto.setAppId(100L);
        dto.setPageSize(10);

        ChatHistoryUserCursorPageVO vo = service.queryChatHistoryByCursor(dto, request);

        assertEquals(1, ((List<?>) vo.getRecords()).size());
        assertFalse(vo.getHasMore());
        assertNull(vo.getNextCursor());
    }

    @Test
    void queryCursor_withCursor_paginateBackward() {
        loginAs(1L);

        LocalDateTime tOld = LocalDateTime.of(2026, 9, 9, 10, 0);
        ChatHistory h = ChatHistory.builder().id(5L).appId(100L).userId(1L).message("old").createTime(tOld).build();

        Page<ChatHistory> dbPage = new Page<>(List.of(h), 1, 11, 1);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));

        ChatHistoryUserCursorQueryRequestDTO dto = new ChatHistoryUserCursorQueryRequestDTO();
        dto.setAppId(100L);
        dto.setPageSize(10);
        dto.setCursor(CursorHelper.encode(new CursorHelper.CursorEntry(LocalDateTime.of(2026, 9, 10, 0, 0), 100L)));

        ChatHistoryUserCursorPageVO vo = service.queryChatHistoryByCursor(dto, request);

        assertEquals(1, ((List<?>) vo.getRecords()).size());
        assertFalse(vo.getHasMore());
        assertNull(vo.getNextCursor());
    }

    @Test
    void queryCursor_messageTypeFilter_filtersCorrectly() {
        loginAs(1L);

        ChatHistory h = ChatHistory.builder().id(1L).appId(100L).userId(1L).message("ai msg").messageType(MessageType.AI).build();

        Page<ChatHistory> dbPage = new Page<>(List.of(h), 1, 11, 1);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));

        ChatHistoryUserCursorQueryRequestDTO dto = new ChatHistoryUserCursorQueryRequestDTO();
        dto.setAppId(100L);
        dto.setMessageType("ai");
        dto.setPageSize(10);

        ChatHistoryUserCursorPageVO vo = service.queryChatHistoryByCursor(dto, request);

        assertEquals(1, ((List<?>) vo.getRecords()).size());
        assertEquals(MessageType.AI, ((ChatHistoryVO) ((List<?>) vo.getRecords()).get(0)).getMessageType());
        // 主键 id 需一并透出，前端据此定位单条消息
        assertEquals(Long.valueOf(1L), ((ChatHistoryVO) ((List<?>) vo.getRecords()).get(0)).getId());
    }

    @Test
    void queryCursor_notLoggedIn_throwsNotLoginError() {
        when(request.getSession(false)).thenReturn(null);

        ChatHistoryUserCursorQueryRequestDTO dto = new ChatHistoryUserCursorQueryRequestDTO();
        dto.setAppId(100L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.queryChatHistoryByCursor(dto, request));

        assertEquals(ErrorCode.NOT_LOGIN_ERROR.getCode(), e.getCode());
    }

    @Test
    void queryCursor_emptyResult_noNextCursor() {
        loginAs(1L);

        Page<ChatHistory> dbPage = new Page<>(List.<ChatHistory>of(), 1, 11, 0);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));

        ChatHistoryUserCursorQueryRequestDTO dto = new ChatHistoryUserCursorQueryRequestDTO();
        dto.setAppId(100L);
        dto.setPageSize(10);

        ChatHistoryUserCursorPageVO vo = service.queryChatHistoryByCursor(dto, request);

        assertEquals(0, ((List<?>) vo.getRecords()).size());
        assertFalse(vo.getHasMore());
        assertNull(vo.getNextCursor());
    }

    @Test
    void queryCursor_pageIsConstructedCorrectly() {
        loginAs(1L);

        ChatHistory h = ChatHistory.builder().id(1L).appId(100L).userId(1L).createTime(LocalDateTime.now()).build();
        Page<ChatHistory> dbPage = new Page<>(List.of(h), 1, 11, 1);
        doReturn(dbPage).when(service).page(any(Page.class), any(QueryWrapper.class));

        ChatHistoryUserCursorQueryRequestDTO dto = new ChatHistoryUserCursorQueryRequestDTO();
        dto.setAppId(100L);
        dto.setPageSize(10);

        ArgumentCaptor<Page<ChatHistory>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        service.queryChatHistoryByCursor(dto, request);

        verify(service).page(pageCaptor.capture(), any(QueryWrapper.class));
        // Page size should be pageSize + 1 for hasMore check
        assertEquals(11, pageCaptor.getValue().getPageSize());
    }

    // endregion

    // region 辅助方法

    private void loginAs(Long userId) {
        when(request.getSession(false)).thenReturn(session);
        UserVO vo = new UserVO();
        vo.setId(userId);
        when(session.getAttribute(USER_LOGIN_STATUS)).thenReturn(vo);
    }

    // endregion
}
