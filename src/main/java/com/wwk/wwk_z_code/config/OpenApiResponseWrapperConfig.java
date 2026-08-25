package com.wwk.wwk_z_code.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 让 OpenAPI 文档里的 200 响应显示 BaseResponse<T> 包装结构。
 *
 * <h3>背景</h3>
 * Controller 方法声明返回的是原始类型（如 User、Page<User>、UserVO、String），
 * 运行时由 ResponseWrapperAdvice 用 ResponseBodyAdvice 统一包装成
 * { code, message, data } 三字段的 BaseResponse<T>。
 * 但 springdoc 生成 OpenAPI 文档时读的是【方法声明的返回类型】，并不知道运行时会被包装，
 * 所以文档里每个接口的响应模型都显示成原始类型，和真实返回体对不上。
 *
 * <h3>思路</h3>
 * springdoc 允许注册一个 GlobalOpenApiCustomizer：
 * 文档内容生成完成后、对外返回前，会把整个 OpenAPI 对象（可以理解为"文档的 Java 内存模型"）回调给我们改。
 * 我们就在这里把所有接口的 200 响应 schema 重写成 BaseResponse<T> 复合结构，
 * 让文档与实际返回体一致。Controller 代码一行都不用改。
 *
 * <h3>OpenAPI 内存模型（看懂代码先要懂它）</h3>
 * OpenAPI（文档根）─> paths: Map<路径, PathItem>         路径 /users 等
 *   PathItem ─> 各 HTTP 方法对应的 Operation             GET/POST/PUT/DELETE...
 *   Operation ─> responses: Map<状态码, ApiResponse>     "200" -> 响应
 *   ApiResponse ─> content: Map<媒体类型, MediaType>     "application/json" -> 内容
 *   MediaType ─> schema: Schema<?>                       响应体结构（可能是 $ref 引用）
 * 本类遍历的就是这一整条链，最后把最内层的 schema 替换掉。
 */
@Configuration
public class OpenApiResponseWrapperConfig {

    /**
     * 注册文档自定义器 Bean。
     * springdoc 会自动收集容器里所有 GlobalOpenApiCustomizer 类型的 Bean，
     * 在文档生成后依次调用它们的 customise(OpenAPI) 方法。
     *
     * 整个逻辑就是：遍历每个接口的 200 响应 -> 取出原始返回 schema ->
     * 套一层 {code, message, data} -> 写回文档并指向新生成的包装 schema。
     */
    @Bean
    public GlobalOpenApiCustomizer baseResponseWrapperCustomizer() {
        // GlobalOpenApiCustomizer 是函数式接口，只有一个方法 customise(OpenAPI)，所以能用 lambda 简写
        return openApi -> {
            // openApi 就是整个文档的内存对象，paths 是"接口路径 -> 路径详情"的映射
            // 如果连接口都没有（比如扫描范围为空），直接结束
            if (openApi.getPaths() == null) {
                return;
            }

            // components.schemas 是文档里所有"模型定义"的集合（如 User、PageUser），
            // 后面我们要把包装结构 BaseResponseXxx 作为新模型加进去，所以先拿到 components
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }
            // 注意：Java 的 lambda 只能捕获"effectively final"（不再被赋值）的变量，
            // 上面 if 分支里我们重新给 components 赋过值，lambda 里直接用会编译报错，
            // 所以用一个 final 变量把它固定下来供 lambda 内部使用
            Components finalComponents = components;

            // 遍历每个接口路径
            openApi.getPaths().values().forEach(pathItem ->
                    // 一个路径可能有多个方法（GET/POST...），readOperations() 把所有方法的 Operation 都拿出来
                    pathItem.readOperations().forEach(operation -> {
                        // 1) 找到这个接口的"200 成功响应"定义；springdoc 用 "200" 作为状态码 key
                        var apiResponse = operation.getResponses().get("200");
                        if (apiResponse == null) {
                            return;
                        }
                        // 2) 响应里可能有多种媒体类型的返回（json/xml...），我们要改的是 json
                        var content = apiResponse.getContent();
                        if (content == null) {
                            return;
                        }
                        // 3) 拿到 application/json 这种媒体类型的定义
                        var json = content.get("application/json");
                        if (json == null) {
                            return;
                        }
                        // 4) 取到原始返回类型的 schema。
                        //    它通常是个 $ref 引用，例如 #/components/schemas/User，
                        //    意思是"响应体就是 User 这个模型"。这一步就是我们要替换掉的东西。
                        Schema<?> dataSchema = json.getSchema();
                        if (dataSchema == null) {
                            return;
                        }

                        // 5) 幂等保护：如果原始 schema 本身已经指向 BaseResponseXxx，
                        //    说明已经被处理过（或本来就是包装类型），直接跳过，避免重复包装
                        String dataRef = dataSchema.get$ref();
                        if (dataRef != null && dataRef.contains("BaseResponse")) {
                            return;
                        }

                        // 6) 给包装结构起个名字，规则：BaseResponse + 原类型名
                        //    优先从 $ref 里取：如 #/components/schemas/User -> User -> BaseResponseUser
                        String baseName = dataRef != null
                                ? dataRef.substring(dataRef.lastIndexOf('/') + 1)
                                : // 没有 $ref 说明是内联类型（如 String 直接返回），用类型名首字母大写：string -> String
                                (dataSchema.getType() != null && !dataSchema.getType().isEmpty()
                                        ? Character.toUpperCase(dataSchema.getType().charAt(0)) + dataSchema.getType().substring(1)
                                        : // 连类型名都没有（极端情况），退而用接口名兜底
                                        (operation.getOperationId() != null ? operation.getOperationId() : "Object"));
                        String wrapperName = "BaseResponse" + baseName;

                        // 7) 构造包装 schema：一个 object，包含 code / message / data 三个字段。
                        //    data 字段直接复用原来的 schema（第4步那个 $ref 或内联类型），
                        //    这样 data 就能正确指向真实返回类型
                        Schema<?> wrapper = new Schema<>()
                                .type("object")
                                .addProperty("code", new Schema<>().type("integer").format("int32"))
                                .addProperty("message", new Schema<>().type("string"))
                                .addProperty("data", dataSchema);

                        // 8) 把包装 schema 注册到 components.schemas 里，成为文档里的一个模型定义。
                        //    containsKey 判断是为了复用：多个接口返回同一个类型时，只生成一份 BaseResponseXxx
                        if (finalComponents.getSchemas() == null || !finalComponents.getSchemas().containsKey(wrapperName)) {
                            finalComponents.addSchemas(wrapperName, wrapper);
                        }

                        // 9) 最后把接口响应的 schema 改成指向这个包装模型，
                        //    文档里该接口的响应就显示成 {code, message, data}，data 指向真实类型了
                        json.setSchema(new Schema<>().$ref("#/components/schemas/" + wrapperName));
                    })
            );
        };
    }
}
