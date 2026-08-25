package com.wwk.wwk_z_code;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class WwkZCodeApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WwkZCodeApplication.class, args);
    }

}
