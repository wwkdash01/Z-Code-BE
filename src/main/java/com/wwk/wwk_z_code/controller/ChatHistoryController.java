package com.wwk.wwk_z_code.controller;

import com.mybatisflex.core.paginate.Page;
import com.wwk.wwk_z_code.model.dto.ChatHistoryAddRequestDTO;
import com.wwk.wwk_z_code.model.dto.ChatHistoryAdminQueryRequestDTO;
import com.wwk.wwk_z_code.model.entity.ChatHistory;
import com.wwk.wwk_z_code.service.ChatHistoryService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  控制层。
 *
 * @author wangwenkai
 * @since 2026-09-04
 */
@Validated
@RestController
@RequestMapping("/chatHistories")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    /**
     * 新增消息体(USER，需登录并校验应用归属)
     *
     * @param chatHistoryAddRequestDTO 新增请求DTO
     * @param request HTTP请求
     * @return 新创建的主键ID
     */
    @PostMapping("/user")
    public Long addChatHistory(
            @RequestBody
            @Valid
            ChatHistoryAddRequestDTO chatHistoryAddRequestDTO,
            HttpServletRequest request) {
        return chatHistoryService.addChatHistory(chatHistoryAddRequestDTO, request);
    }

    /**
     * 根据主键获取详情(USER，校验应用归属)
     *
     * @param id 主键
     * @param request HTTP请求
     * @return 聊天记录实体
     */
    @GetMapping("/user/{id}")
    public ChatHistory getChatHistoryById(
            @Parameter(description = "聊天记录ID", schema = @Schema(type = "String"))
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "聊天记录id不能小于1")
            Long id,
            HttpServletRequest request) {
        return chatHistoryService.getChatHistoryById(id, request);
    }

    /**
     * 根据主键删除记录(ADMIN)
     *
     * @param id 主键
     * @return {@code true} 删除成功
     */
    @DeleteMapping("/admin/{id}")
    public Boolean removeChatHistoryByAdmin(
            @Parameter(description = "聊天记录ID", schema = @Schema(type = "String"))
            @PathVariable
            @NotNull
            @Min(value = 1L, message = "聊天记录id不能小于1")
            Long id) {
        return chatHistoryService.removeChatHistoryByAdmin(id);
    }

    /**
     * 分页查询聊天记录(ADMIN，全条件)
     *
     * @param chatHistoryAdminQueryRequestDTO 管理分页查询DTO（query 参数自动绑定）
     * @return 聊天记录分页对象
     */
    @GetMapping("/admin/page")
    public Page<ChatHistory> getChatHistoryByAdminPage(
            @ModelAttribute
            @Valid
            @ParameterObject
            ChatHistoryAdminQueryRequestDTO chatHistoryAdminQueryRequestDTO) {
        return chatHistoryService.getChatHistoryByAdminPage(chatHistoryAdminQueryRequestDTO);
    }
}
