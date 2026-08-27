package com.wwk.wwk_z_code.saver;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wwk.wwk_z_code.exception.BusinessException;
import com.wwk.wwk_z_code.exception.ErrorCode;

import java.io.File;
import java.util.Date;

/**
 * 代码文件保存模板：定义将代码结果保存到本地唯一目录的通用流程（校验结果 → 构建目录 → 写入文件）
 *
 * @param <T> 代码结果类型（单文件/多文件代码结果）
 */
public abstract class CodeFileSaverTemplate<T> {
    /**
     * 内部属性 代码文件保存的根目录
     */
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_outputs";

    /**
     * 内部方法 构造唯一文件路径（根目录+时间戳+随机后缀）
     *
     * @return 唯一保存目录
     */
    private static String buildUniqueSaveDir() {
        String timestamp = DateUtil.format(new Date(), "yyyyMMdd_HHmmss");
        String uniqueSaveDirSuffix = StrUtil.format("{}_{}_{}", timestamp, RandomUtil.randomString(4));
        String uniqueSaveDir = FILE_SAVE_ROOT_DIR + File.separator + uniqueSaveDirSuffix;

        FileUtil.mkdir(uniqueSaveDir);
        return uniqueSaveDir;
    }

    /**
     * 外部接口 保存LLM输出到唯一目录
     *
     * @param result 代码结果
     * @return 代码保存目录
     */
    public final File saveCodeResult(T result) {
        // 1-参数校验
        validateResult(result);

        // 2-构建目录
        String saveDir = buildUniqueSaveDir();

        // 3-保存文件
        saveCodeResultToFile(result, saveDir);

        // 4-返回
        return new File(saveDir);
    }

    /**
     * 内部方法 验证LLM输出结果，校验不通过抛业务异常
     *
     * @param result 代码结果
     */
    protected void validateResult(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.CODE_GENERATE_ERROR, "代码生成异常");
        }
    }

    /**
     * 内部方法 将代码结果写入指定目录（子类实现具体文件写入）
     *
     * @param result  代码结果
     * @param saveDir 代码保存目录
     */
    protected abstract void saveCodeResultToFile(T result, String saveDir);
}
