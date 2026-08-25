package com.wwk.wwk_z_code.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;

import java.io.File;

public class CodeResultSaver {
    // 内部属性 文件保存的根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_outputs";

    // 外部接口 保存单文件代码
    public File saveSingletonCodeResult(SingletonHtmlCodeResult codeResult){
        String dirPath = buildUniqueSaveDir(CodeGenEnum.SINGLETON_HTML.getCodeGenMode());
        writeStringToFile(dirPath, "index.html", codeResult.getHtmlCode());

        return new File(dirPath);
    }

    // 外部接口 保存多文件代码
    public File saveMultiFileCodeResult(MultiFileHtmlCodeResult codeResult){
        String dirPath = buildUniqueSaveDir(CodeGenEnum.MULTIFILE_HTML.getCodeGenMode());
        writeStringToFile(dirPath, "index.html", codeResult.getHtmlCode());
        writeStringToFile(dirPath, "style.css", codeResult.getCssCode());
        writeStringToFile(dirPath, "script.js", codeResult.getJsCode());

        return new File(dirPath);
    }

    // 内部方法 构造唯一文件路径（根目录+文件类型+雪花id）
    private static String buildUniqueSaveDir(String codeType) {
        String uniqueSaveDirSuffix = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextIdStr());
        String uniqueSaveDir = FILE_SAVE_ROOT_DIR + File.separator + uniqueSaveDirSuffix;

        FileUtil.mkdir(uniqueSaveDir);
        return uniqueSaveDir;
    }

    // 保存单个文件
    private static void writeStringToFile(String content, String fileName, String dirPath) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, CharsetUtil.CHARSET_UTF_8);
    }
}
