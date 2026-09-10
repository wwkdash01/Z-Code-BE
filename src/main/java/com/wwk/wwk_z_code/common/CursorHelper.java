package com.wwk.wwk_z_code.common;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 游标编解码工具类（Base64 JSON）
 */
public final class CursorHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    private CursorHelper() {}

    /**
     * 编码游标值为 Base64 JSON 字符串，格式为 [createTimeMillis, id]
     */
    public static String encode(CursorEntry entry) {
        try {
            long[] payload = new long[]{entry.createTime.toInstant(ZoneId.systemDefault().getRules().getOffset(entry.createTime)).toEpochMilli(), entry.id};
            return base64Encode(MAPPER.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "游标编码失败");
        }
    }

    /**
     * 解码 Base64 JSON 字符串为游标对象
     */
    public static CursorEntry decode(String encoded) {
        if (StrUtil.isBlank(encoded)) return null;
        try {
            String json = base64Decode(encoded);
            // 解析为数组 [createTimeMillis, id]
            long[] values = MAPPER.readValue(json, long[].class);
            LocalDateTime createTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(values[0]), ZoneId.systemDefault());
            return new CursorEntry(createTime, values[1]);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "游标格式不合法");
        }
    }

    private static String base64Encode(String input) {
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String base64Decode(String input) {
        return new String(java.util.Base64.getUrlDecoder().decode(input), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 游标数据持有类
     */
    public static final class CursorEntry {
        private final LocalDateTime createTime;
        private final Long id;

        public CursorEntry(LocalDateTime createTime, Long id) {
            this.createTime = createTime;
            this.id = id;
        }

        public LocalDateTime getCreateTime() { return createTime; }
        public Long getId() { return id; }
    }
}
