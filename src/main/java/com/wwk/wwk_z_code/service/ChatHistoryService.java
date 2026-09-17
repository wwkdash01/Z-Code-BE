package com.wwk.wwk_z_code.service;

import com.mybatisflex.core.paginate.Page;
import com.wwk.wwk_z_code.model.dto.ChatHistoryAddRequestDTO;
import com.wwk.wwk_z_code.model.dto.ChatHistoryAdminQueryRequestDTO;
import com.wwk.wwk_z_code.model.dto.ChatHistoryUserCursorQueryRequestDTO;
import com.wwk.wwk_z_code.model.dto.RetractUserPromptRequestDTO;
import com.wwk.wwk_z_code.model.entity.ChatHistory;
import com.wwk.wwk_z_code.model.vo.ChatHistoryUserCursorPageVO;
import com.wwk.wwk_z_code.model.vo.ChatHistoryVO;
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
     * @return 聊天记录视图对象
     */
    ChatHistoryVO getChatHistoryById(Long id, HttpServletRequest request);

    /**
     * 根据主键删除记录（ADMIN）
     * @param id 主键
     * @return {@code true} 删除成功
     */
    Boolean removeChatHistoryByAdmin(Long id);

    /**
     * 按应用ID级联逻辑删除该应用下所有聊天记录
     * 仅供 AppService 删除应用时调用，框架自动转 UPDATE isDelete=1。
     * @param appId 应用ID
     * @return {@code true} 操作执行成功（无匹配记录也返回 true）
     */
    Boolean removeByAppId(Long appId);

    /**
     * 分页查询聊天记录（ADMIN，全条件检索）
     * @param chatHistoryAdminQueryRequestDTO 管理分页查询DTO
     * @return 聊天记录分页对象
     */
    Page<ChatHistory> getChatHistoryByAdminPage(ChatHistoryAdminQueryRequestDTO chatHistoryAdminQueryRequestDTO);

    /**
     * 游标分页查询聊天记录（USER，需登录并校验应用归属）
     * 用于 IM 无限屏场景：按 createTime DESC, id DESC 排序翻页。
     * @param dto 游标查询请求DTO
     * @param request HTTP请求
     * @return 游标分页结果VO
     */
    ChatHistoryUserCursorPageVO queryChatHistoryByCursor(ChatHistoryUserCursorQueryRequestDTO dto, HttpServletRequest request);

    /**
     * 撤销用户提示词（USER，需登录并校验应用归属）
     * <p>就地把原 user 行的 messageType 改写为 retraction，id 与 message 原文保留，
     * 供前端定位并丢弃被撤销的那一轮对话；该轮后续的 ai/error 行不做处理。</p>
     * <p>chatHistoryId 不传时按最新一轮失败对话定位：最新一条必须是 error（说明生成失败过），
     * 再往前取第一条 user/retraction；目标已是 retraction 或并发下已被撤销时幂等返回 true。</p>
     * @param retractUserPromptRequestDTO 撤销请求DTO
     * @param request HTTP请求
     * @return {@code true} 撤销成功（含重复撤销）
     */
    Boolean retractUserPrompt(RetractUserPromptRequestDTO retractUserPromptRequestDTO, HttpServletRequest request);
}
