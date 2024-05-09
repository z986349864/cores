package com.zd.core.utils;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class VosStringUtils {


    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 对字符串进行截取截取
     * @param message
     * @param lengthByte
     */
    public static String truncateString(String message, int lengthByte) {
        //对堆栈进行截取
        StringBuffer result = new StringBuffer();
        int currentLength = 0;
        int currentIndex = 0;
        while (currentLength < lengthByte) {
            char c = message.charAt(currentIndex);
            currentIndex ++;
            if (isChinese(c)) {
                currentLength += 2;
            } else {
                currentLength += 1;
            }
            if (currentLength > lengthByte || currentIndex == message.length()) {
                break;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 字符串分割后转换成list集合
     * @param originStr
     * @return
     */
    public static List<String> splitToList(String originStr, String splitPattern) {
        if (StringUtils.isEmpty(originStr)) {
            return null;
        }
        return Arrays.asList(originStr.split(splitPattern));
    }

}