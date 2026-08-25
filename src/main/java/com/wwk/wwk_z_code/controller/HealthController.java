package com.wwk.wwk_z_code.controller;

import com.wwk.wwk_z_code.common.BaseResponse;
import com.wwk.wwk_z_code.common.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {
    @GetMapping
    public String getHealthStatus() {
        return "hello spring";
    }
}
