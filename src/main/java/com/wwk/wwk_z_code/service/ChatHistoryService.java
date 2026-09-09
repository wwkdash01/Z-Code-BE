package com.wwk.wwk_z_code.service;

import com.mybatisflex.core.paginate.Page;
import com.wwk.wwk_z_code.model.dto.ChatHistoryAddRequestDTO;
import com.wwk.wwk_z_code.model.dto.ChatHistoryAdminQueryRequestDTO;
import com.wwk.wwk_z_code.model.entity.ChatHistory;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;


/**
 *  服务层。
 *
 * @author wangwenkai
 * @since 2026-09-04
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 新增聊天记录（USER，需登录并校验应用归属）
     * @param chatHistoryAddRequestDTO 新增请求DTO
     * @param request HTTP请求
     * @return 新创建记录的主键ID
     */
    Long addChatHistory(ChatHistoryAddRequestDTO chatHistoryAddRequestDTO, HttpServletRequest request);

    /**
     * 根据主键获取详情（USER，需校验应用归属）
     * @param id 主键
     * @param request HTTP请求
     * @return 聊天记录实体
     */
    ChatHistory getChatHistoryById(Long id, HttpServletRequest request);

    /**
     * 根据主键删除记录（ADMIN）
     * @param id 主键
     * @return {@code true} 删除成功
     */
    Boolean removeChatHistoryByAdmin(Long id);

    /**
     * 分页查询聊天记录（ADMIN，全条件检索）
     * @param chatHistoryAdminQueryRequestDTO 管理分页查询DTO
     * @return 聊天记录分页对象
     */
    Page<ChatHistory> getChatHistoryByAdminPage(ChatHistoryAdminQueryRequestDTO chatHistoryAdminQueryRequestDTO);
}
