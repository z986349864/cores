package com.zd.core.cache;

import org.springframework.util.StringUtils;

public class CacheKey {

    public static final String keySplitStr = "!@#";

    private String[] keyValues;

    /**
     * 根据给定的多个字符串构建对象
     * @param keyValues
     */
    public CacheKey(String... keyValues) {
        this.keyValues = keyValues;
    }

    /**
     * 将多个字符串生成一个字符串
     * @return
     */
    public String generateKey(){
        if (keyValues == null || keyValues.length == 0) {
            return "";
        }
        StringBuffer key = new StringBuffer();
        for (int i = 0, length = length(); i < length; i++) {
            key.append(keyValues[i]).append(i == length - 1 ? "" : keySplitStr);
        }
        return key.toString();
    }

    /**
     * 从已将字符串转成成CacheKey对象
     * @param str
     * @return
     */
    public static CacheKey convertToCacheKey(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        return new CacheKey(str.split(keySplitStr));
    }

    /**
     * 获取该key对象的key组成字符串个数
     * @return
     */
    public int length(){
        return keyValues.length;
    }

    /**
     * 获取组成此字符串的第index个元素的字符串
     * @param index
     * @return
     */
    public String get(int index) {
        return keyValues[index];
    }

    @Override
    public int hashCode() {
        return this.generateKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof CacheKey) && this.generateKey().equals(((CacheKey) obj).generateKey());
    }

}