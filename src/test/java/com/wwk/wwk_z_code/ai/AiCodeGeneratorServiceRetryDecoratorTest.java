package com.wwk.wwk_z_code.ai;

import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AiCodeGeneratorServiceRetryDecorator 单元测试：非流式失败/空字段重试并附加纠正提示词，全失败抛业务异常；流式原样转发。
 */
class AiCodeGeneratorServiceRetryDecoratorTest {

    private static final String ORIGINAL_PROMPT = "做一个个人博客网站";

    @Mock
    private AiCodeGeneratorService delegate;

    private AiCodeGeneratorServiceRetryDecorator decorator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        decorator = new AiCodeGeneratorServiceRetryDecorator(delegate);
    }

    @Test
    void generateSingletonFileCode_firstFailsThenSucceeds_returnsResultWithCorrectivePrompt() {
        SingletonHtmlCodeResult success = singletonResult("<h1>博客</h1>");
        when(delegate.generateSingletonFileCode(anyString()))
                .thenThrow(new RuntimeException("模拟解析失败"))
                .thenReturn(success);

        SingletonHtmlCodeResult result = decorator.generateSingletonFileCode(ORIGINAL_PROMPT);

        assertSame(success, result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(delegate, org.mockito.Mockito.times(2)).generateSingletonFileCode(captor.capture());
        List<String> prompts = captor.getAllValues();
        assertEquals(ORIGINAL_PROMPT, prompts.get(0));
        assertTrue(prompts.get(1).contains("兜底重试提示"));
    }

    @Test
    void generateMultiFileCode_emptyFieldsThenValid_retries() {
        MultiFileHtmlCodeResult invalid = multifileResult(null, "h1 { }", "console.log(1)");
        MultiFileHtmlCodeResult valid = multifileResult("<h1>博客</h1>", "h1 { }", "console.log(1)");
        when(delegate.generateMultiFileCode(anyString()))
                .thenReturn(invalid)
                .thenReturn(valid);

        MultiFileHtmlCodeResult result = decorator.generateMultiFileCode(ORIGINAL_PROMPT);

        assertSame(valid, result);
        verify(delegate, org.mockito.Mockito.times(2)).generateMultiFileCode(anyString());
    }

    @Test
    void generateMultiFileCode_alwaysFails_throwsCodeGenerateError() {
        when(delegate.generateMultiFileCode(anyString()))
                .thenThrow(new RuntimeException("模拟持续失败"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> decorator.generateMultiFileCode(ORIGINAL_PROMPT));

        assertEquals(ErrorCode.CODE_GENERATE_ERROR.getCode(), e.getCode());
        verify(delegate, org.mockito.Mockito.times(3)).generateMultiFileCode(anyString());
    }

    @Test
    void streamMethods_delegateToRaw() {
        Flux<String> singletonFlux = Flux.just("<h1>博客</h1>");
        Flux<String> multifileFlux = Flux.just("```html\n<h1>博客</h1>\n```");
        when(delegate.generateSingletonFileCodeByStream(anyString())).thenReturn(singletonFlux);
        when(delegate.generateMultiFileCodeByStream(anyString())).thenReturn(multifileFlux);

        assertSame(singletonFlux, decorator.generateSingletonFileCodeByStream(ORIGINAL_PROMPT));
        assertSame(multifileFlux, decorator.generateMultiFileCodeByStream(ORIGINAL_PROMPT));

        verify(delegate).generateSingletonFileCodeByStream(ORIGINAL_PROMPT);
        verify(delegate).generateMultiFileCodeByStream(ORIGINAL_PROMPT);
    }

    private SingletonHtmlCodeResult singletonResult(String htmlCode) {
        SingletonHtmlCodeResult result = new SingletonHtmlCodeResult();
        result.setHtmlCode(htmlCode);
        return result;
    }

    private MultiFileHtmlCodeResult multifileResult(String htmlCode, String cssCode, String jsCode) {
        MultiFileHtmlCodeResult result = new MultiFileHtmlCodeResult();
        result.setHtmlCode(htmlCode);
        result.setCssCode(cssCode);
        result.setJsCode(jsCode);
        return result;
    }
}
