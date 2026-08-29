package com.wwk.wwk_z_code.constant;

public interface AppConstant {

    /**
     * 预览 URL 基础地址
     */
    String LOCAL_RREVIEW_BASE_URL = "http://localhost:80/preview";

    /**
     * 部署 URL 基础地址
     */
    String LOCAL_DEPLOY_BASE_URL = "http://localhost:80/deploy";

    /**
     * 生成代码文件保存的根目录
     */
    String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_outputs";

    /**
     * 部署根目录
     */
    String CODE_DEPLOY_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";
}
