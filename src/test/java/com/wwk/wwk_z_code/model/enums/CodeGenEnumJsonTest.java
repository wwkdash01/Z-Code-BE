package com.wwk.wwk_z_code.model.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CodeGenEnum 单元测试：验证 JSON 序列化/反序列化使用 codeGenMode 值（"singleton" / "multifile"）。
 */
class CodeGenEnumJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serialize_usesCodeGenModeValue() throws Exception {
        String json = objectMapper.writeValueAsString(CodeGenEnum.SINGLETON_HTML);

        assertEquals("\"singleton\"", json);
    }

    @Test
    void deserialize_readsCodeGenModeValue() throws Exception {
        CodeGenEnum codeGenEnum = objectMapper.readValue("\"multifile\"", CodeGenEnum.class);

        assertEquals(CodeGenEnum.MULTIFILE_HTML, codeGenEnum);
    }
}
