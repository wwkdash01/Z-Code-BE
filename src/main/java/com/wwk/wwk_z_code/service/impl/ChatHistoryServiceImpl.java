package com.wwk.wwk_z_code.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.wwk.wwk_z_code.annotation.AuthCheck;
import com.wwk.wwk_z_code.common.PageRequest;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import com.wwk.wwk_z_code.mapper.AppMapper;
import com.wwk.wwk_z_code.mapper.ChatHistoryMapper;
import com.wwk.wwk_z_code.model.dto.ChatHistoryAddRequestDTO;
import com.wwk.wwk_z_code.model.dto.ChatHistoryAdminQueryRequestDTO;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.model.entity.ChatHistory;
import com.wwk.wwk_z_code.model.enums.MessageType;
import com.wwk.wwk_z_code.model.enums.UserRoleEnum;
import com.wwk.wwk_z_code.model.vo.UserVO;
import com.wwk.wwk_z_code.service.ChatHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

import static com.wwk.wwk_z_code.constant.UserConstant.USER_LOGIN_STATUS;

/**
 *  服务层实现。
 *
 * @author wangwenkai
 * @since 2026-09-04
 */
@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    private final AppMapper appMapper;

    // region 用户接口

    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public Long addChatHistory(ChatHistoryAddRequestDTO dto, HttpServletRequest request) {
        // 1-获取当前登录用户
        UserVO currentUser = getUserVOFromSession(request);

        // 2-校验应用归属：current user must be creator of this app
        checkAppOwnership(dto.getAppId(), request);

        // 3-构建DB实体
        ChatHistory chatHistory = new ChatHistory();
        BeanUtils.copyProperties(dto, chatHistory);
        chatHistory.setUserId(currentUser.getId());

        // 4-枚举转换失败兜底（理论上 @NotNull 已拦截，双重保障）
        MessageType resolved = MessageType.getMessageType(dto.getMessageType().getCode());
        if (resolved == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "消息类型不合法");
        }
        chatHistory.setMessageType(resolved);

        LocalDateTime now = LocalDateTime.now();
        chatHistory.setCreateTime(now);
        chatHistory.setUpdateTime(now);
        chatHistory.setEditTime(now);
        chatHistory.setIsDelete(0);

        // 5-落库
        boolean saved = this.save(chatHistory);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增聊天记录失败");
        }
        return chatHistory.getId();
    }

    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.USER)
    public ChatHistory getChatHistoryById(Long id, HttpServletRequest request) {
        // 1-获取当前登录用户
        UserVO currentUser = getUserVOFromSession(request);

        // 2-查询记录
        ChatHistory chatHistory = this.getById(id);
        checkChatHistoryExists(chatHistory);

        // 3-校验归属：通过 appId 查 App，比对 createUserId
        App dbApp = appMapper.selectOneById(chatHistory.getAppId());
        if (dbApp == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无权访问该聊天记录");
        }
        if (!currentUser.getId().equals(dbApp.getCreateUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无权访问该聊天记录");
        }

        return chatHistory;
    }

    // endregion

    // region 管理员接口

    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public Boolean removeChatHistoryByAdmin(Long id) {
        // 1-校验存在
        ChatHistory chatHistory = this.getById(id);
        checkChatHistoryExists(chatHistory);

        // 2-删除
        return this.removeById(id);
    }

    @Override
    @AuthCheck(roleRequirement = UserRoleEnum.ADMIN)
    public Page<ChatHistory> getChatHistoryByAdminPage(ChatHistoryAdminQueryRequestDTO dto) {
        // 1-封装QueryWrapper
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", dto.getId(), dto.getId() != null)
                .eq("appId", dto.getAppId(), dto.getAppId() != null)
                .eq("userId", dto.getUserId(), dto.getUserId() != null)
                .eq("messageType", resolveMessageTypeValue(dto.getMessageType()), StrUtil.isNotBlank(dto.getMessageType()));

        // 2-排序（ascend=true / descend=false）
        if (StrUtil.isNotBlank(dto.getSortField()) && SORTABLE_FIELDS.contains(dto.getSortField())) {
            queryWrapper.orderBy(dto.getSortField(), "ascend".equals(dto.getSortOrder()));
        }

        // 3-分页查询
        Page<ChatHistory> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        return this.page(page, queryWrapper);
    }

    // endregion

    // region 私有工具方法

    /**
     * 解析MessageType字符串为数据库存储值；无效返回null
     */
    private static String resolveMessageTypeValue(String messageTypeCode) {
        if (StrUtil.isBlank(messageTypeCode)) return null;
        MessageType mt = MessageType.getMessageType(messageTypeCode);
        return mt != null ? mt.getCode() : null;
    }

    /**
     * 校验聊天记录是否存在
     */
    private void checkChatHistoryExists(ChatHistory chatHistory) {
        if (chatHistory == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "聊天记录不存在或已删除");
        }
    }

    /**
     * 校验应用归属（appId对应的App的createUserId等于当前用户）
     */
    private void checkAppOwnership(Long appId, HttpServletRequest request) {
        UserVO currentUser = getUserVOFromSession(request);
        App dbApp = appMapper.selectOneById(appId);
        if (dbApp == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "应用不存在或已删除");
        }
        if (!currentUser.getId().equals(dbApp.getCreateUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无权访问该应用");
        }
    }

    /**
     * 从Session中获取请求用户信息（未登录抛异常）
     */
    private UserVO getUserVOFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        UserVO currentUserVO = (UserVO) session.getAttribute(USER_LOGIN_STATUS);
        if (currentUserVO == null || currentUserVO.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        return currentUserVO;
    }

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "messageType", "createTime");

    // endregion
}
