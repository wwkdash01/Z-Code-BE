package com.wwk.wwk_z_code.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.wwk.wwk_z_code.mapper.ChatHistoryMapper;
import com.wwk.wwk_z_code.model.entity.ChatHistory;
import com.wwk.wwk_z_code.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author wangwenkai
 * @since 2026-09-04
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
