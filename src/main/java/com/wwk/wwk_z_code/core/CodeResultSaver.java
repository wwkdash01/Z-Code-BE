package com.wwk.wwk_z_code.core;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wwk.wwk_z_code.ai.model.MultiFileHtmlCodeResult;
import com.wwk.wwk_z_code.ai.model.SingletonHtmlCodeResult;
import com.wwk.wwk_z_code.model.enums.CodeGenEnum;

import java.io.File;
import java.util.Date;

public class CodeResultSaver {
    /**
     * 内部属性 代码文件保存的根目录
     */
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_outputs";

    /**
     * 外部接口 保存单文件代码到唯一目录
     *
     * @param codeResult 单文件代码生成结果
     * @return 代码保存目录
     */
    public static File saveSingletonCodeResult(SingletonHtmlCodeResult codeResult){
        String dirPath = buildUniqueSaveDir(CodeGenEnum.SINGLETON_HTML.getCodeGenMode());
        writeStringToFile(dirPath, "index.html", codeResult.getHtmlCode());

        return new File(dirPath);
    }

    /**
     * 外部接口 保存多文件代码到唯一目录
     *
     * @param codeResult 多文件代码生成结果
     * @return 代码保存目录
     */
    public static File saveMultiFileCodeResult(MultiFileHtmlCodeResult codeResult){
        String dirPath = buildUniqueSaveDir(CodeGenEnum.MULTIFILE_HTML.getCodeGenMode());
        writeStringToFile(dirPath, "index.html", codeResult.getHtmlCode());
        writeStringToFile(dirPath, "style.css", codeResult.getCssCode());
        writeStringToFile(dirPath, "script.js", codeResult.getJsCode());

        return new File(dirPath);
    }

    /**
     * 内部方法 构造唯一文件路径（根目录+文件类型+时间戳+随机后缀）
     *
     * @param codeType 代码生成类型
     * @return 唯一保存目录
     */
    private static String buildUniqueSaveDir(String codeType) {
        String timestamp = DateUtil.format(new Date(), "yyyyMMdd_HHmmss");
        String uniqueSaveDirSuffix = StrUtil.format("{}_{}_{}", codeType, timestamp, RandomUtil.randomString(4));
        String uniqueSaveDir = FILE_SAVE_ROOT_DIR + File.separator + uniqueSaveDirSuffix;

        FileUtil.mkdir(uniqueSaveDir);
        return uniqueSaveDir;
    }

    /**
     * 内部方法 保存单个文件到指定目录
     *
     * @param dirPath  代码保存目录
     * @param fileName 文件名
     * @param content  文件内容
     */
    private static void writeStringToFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, CharsetUtil.CHARSET_UTF_8);
    }
}
