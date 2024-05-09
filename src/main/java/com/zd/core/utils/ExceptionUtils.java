package com.zd.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ExceptionUtils {

    /**
     * 从异常中读取堆栈信息
     *
     * @param iex
     * @return
     */
    public static String getStack(Throwable iex) {
        if (iex == null) {
            return "";
        }
        ByteArrayOutputStream buf = null;
        try {
            buf = new ByteArrayOutputStream();
            iex.printStackTrace(new java.io.PrintWriter(buf, true));
            return buf.toString();
        } finally {
            try {
                if (buf != null) {
                    buf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从异常中读取堆栈信息并进行截取
     * @param iex
     * @param length
     */
    public static String getStack(Throwable iex, int length) {
        if (iex == null) {
            return "";
        }
        ByteArrayOutputStream buf = null;
        try {
            buf = new ByteArrayOutputStream();
            iex.printStackTrace(new java.io.PrintWriter(buf, true));
            String fullStack = buf.toString();
            //对堆栈进行截取
            StringBuffer result = new StringBuffer();
            int currentLength = 0;
            int currentIndex = 0;
            while (currentLength < length) {
                char c = fullStack.charAt(currentIndex);
                currentIndex ++;
                if (VosStringUtils.isChinese(c)) {
                    currentLength += 2;
                } else {
                    currentLength += 1;
                }
                if (currentLength > length || currentIndex == fullStack.length()) {
                    break;
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        } finally {
            try {
                if (buf != null) {
                    buf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(getStack(new RuntimeException("ssas"), 100));
    }

}
