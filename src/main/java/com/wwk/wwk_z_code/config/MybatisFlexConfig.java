package com.wwk.wwk_z_code.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Flex 配置。
 * <p>把 @MapperScan 独立出来，避免 @WebMvcTest 切片测试加载主类时注册 Mapper bean（切片无 SqlSessionFactory 会失败）。</p>
 */
@Configuration
@MapperScan("com.wwk.wwk_z_code.mapper")
public class MybatisFlexConfig {
}
