package com.wwk.wwk_z_code.constant;

import java.util.regex.Pattern;

public interface AiConstant {

    /**
     * HTML 代码段正则（匹配 ```html 围栏代码块）
     */
    Pattern HTML_CODE_PATTERN = Pattern.compile(
            "```html\\s*[\\r\\n]+([\\s\\S]*?)[\\r\\n]*```", Pattern.CASE_INSENSITIVE);

    /**
     * CSS 代码段正则（匹配 ```css 围栏代码块）
     */
    Pattern CSS_CODE_PATTERN = Pattern.compile(
            "```css\\s*[\\r\\n]+([\\s\\S]*?)[\\r\\n]*```", Pattern.CASE_INSENSITIVE);

    /**
     * JavaScript 代码段正则（匹配 ```javascript 或 ```js 围栏代码块）
     */
    Pattern JS_CODE_PATTERN = Pattern.compile(
            "```(?:javascript|js)\\s*[\\r\\n]+([\\s\\S]*?)[\\r\\n]*```", Pattern.CASE_INSENSITIVE);
}
