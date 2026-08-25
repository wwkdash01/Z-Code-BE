package com.wwk.wwk_z_code.config;

import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import com.github.xiaoymin.knife4j.spring.extension.Knife4jOpenApiCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jFixConfig {

    @Bean
    public Knife4jOpenApiCustomizer knife4jOpenApiCustomizerSafe(Knife4jProperties knife4jProperties,
                                                                 SpringDocConfigProperties springDocConfigProperties) {
        return new Knife4jOpenApiCustomizer(knife4jProperties, springDocConfigProperties) {
            @Override
            public void customise(OpenAPI openApi) {
                // 跳过父类 addOrderExtension：springdoc 2.4.0+ 把 getGroupConfigs() 返回值改成 Set，
                // 而 knife4j 4.5.0 按 List 编译，调用即抛 NoSuchMethodError，导致 /v3/api-docs 500。
            }
        };
    }
}
