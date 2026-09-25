package com.ruoyi.demo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 敏感词类型对应的脱敏掩码。
 * <p>
 * 采用类型化掩码：脱敏后文档仍可读（如“【机构名】承建【项目名】”），
 * 具体实体内容已被抹掉，只保留“这里是什么类型”的结构信息。
 * 若后续要改成统一掩码（如 ***），只改本枚举的 mask 值即可。
 */
@Getter
@RequiredArgsConstructor
public enum ReviewMaskEnum {

    PERSON("人名", "【人名】"),

    LOCATION("地名", "【地名】"),

    ORGANIZATION("机构名", "【机构名】"),

    PROJECT("项目名", "【项目名】"),

    PHONE("电话", "【电话】"),

    DATE("日期", "【日期】"),

    QUANTITY("数量", "【数量】"),

    OTHER("其他", "【其他】");

    /**
     * 敏感词类型
     */
    private final String type;

    /**
     * 替换文本
     */
    private final String mask;

    /**
     * 按类型取掩码：先精确匹配，再兼容“单位/企业/组织”这类同义类型，都没命中时返回【其他】
     */
    public static String maskOf(String type) {
        if (type == null || type.trim().isEmpty()) {
            return OTHER.mask;
        }
        String value = type.trim();
        for (ReviewMaskEnum item : values()) {
            if (item.type.equals(value)) {
                return item.mask;
            }
        }
        if (value.contains("人名") || value.contains("姓名")) {
            return PERSON.mask;
        }
        if (value.contains("地名") || value.contains("地址")) {
            return LOCATION.mask;
        }
        if (value.contains("机构") || value.contains("公司") || value.contains("单位") || value.contains("组织")) {
            return ORGANIZATION.mask;
        }
        if (value.contains("项目") || value.contains("工程")) {
            return PROJECT.mask;
        }
        if (value.contains("电话") || value.contains("手机")) {
            return PHONE.mask;
        }
        if (value.contains("日期") || value.contains("时间")) {
            return DATE.mask;
        }
        if (value.contains("数量") || value.contains("金额") || value.contains("面积") || value.contains("数字")) {
            return QUANTITY.mask;
        }
        return OTHER.mask;
    }

}
