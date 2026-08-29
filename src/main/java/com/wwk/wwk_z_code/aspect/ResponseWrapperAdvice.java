package com.wwk.wwk_z_code.aspect;

import cn.hutool.json.JSONUtil;
import com.wwk.wwk_z_code.annotation.IgnoreResultWrapper;
import com.wwk.wwk_z_code.common.BaseResponse;
import com.wwk.wwk_z_code.common.ResponseUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 包装拦截器，controller正常返回 或 globalExceptionHandler异常 都会拦截
 */
@RestControllerAdvice
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    // 包装条件，编译判定符合条件的才会触发包装
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 注解不包装
        if (returnType.hasMethodAnnotation(IgnoreResultWrapper.class) || returnType.getContainingClass().isAnnotationPresent(IgnoreResultWrapper.class)) {
            return false;
        }

        // 非包装类型就包装
        return returnType.getParameterType() != BaseResponse.class;
    }

    // 包装逻辑，运行判定(防全局异常处理直接返回包装类)触发包装条件后在controller返回前自动执行
    @Nullable
    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType, @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType, @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
        // 文档相关接口不包装（doc.html是静态资源不会走到这里，重点是排除/v3/api-docs及其分组后缀）
        String path = request.getURI().getPath();
        if (path.contains("/v3/api-docs")) return body;

        // String类型先转json
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return JSONUtil.toJsonStr(ResponseUtils.success(body));
        }

        // 包装null类型
        if (body == null) return ResponseUtils.success(null);

        // 已经是BaseResponse就不懂
        if (body instanceof BaseResponse<?>) return body;

        // 其余情况包装后返回
        return ResponseUtils.success(body);
    }
}
