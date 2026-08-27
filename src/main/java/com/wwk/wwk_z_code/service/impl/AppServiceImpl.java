package com.wwk.wwk_z_code.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wwk.wwk_z_code.model.entity.App;
import com.wwk.wwk_z_code.mapper.AppMapper;
import com.wwk.wwk_z_code.service.AppService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author wwk
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
