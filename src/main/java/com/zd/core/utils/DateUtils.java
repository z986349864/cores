package com.zd.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public static final String DATE_SHORT_FORMAT = "yyyyMMdd";
    public static final String DATE_CH_FORMAT = "yyyy年MM月dd日";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    public static final String DAYTIME_START = "00:00:00.000";
    public static final String DAYTIME_END = "23:59:59.999";

    public static final String DC_DATE_FORMAT = "yyyy/MM/dd";
    public static final String DC_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public static final String ZONED_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String ZONED_DATE_TIME_FORMAT_MS = "yyyy-MM-dd'T'HH:mm:ss:SSSZ";

    public static final String NUMBERDATEFORMAT = "yyyyMMddHHmmss";

    private DateUtils() {
    }

    private static final String[] FORMATS = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd HH:mm:ss", "HH:mm", "HH:mm:ss", "HH:mm:ss.SSS", "yyyy-MM",
            "yyyy-MM-dd HH:mm:ss.SSS", DC_DATE_FORMAT, DC_TIME_FORMAT};

    public static Date convert(String str) {
        if (str != null && str.length() > 0) {
            if (str.length() > 10 && str.charAt(10) == 'T') {
                str = str.replace('T', ' '); // 去掉json-lib加的T字母
            }
            for (String format : FORMATS) {
                if (str.length() == format.length()) {
                    try {
                        Date date = new SimpleDateFormat(format).parse(str);

                        return date;
                    } catch (ParseException e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn(e.getMessage());
                        }
                        // logger.warn(e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    public static Date convert(String str, String format) {
        if (!StringUtils.isEmpty(str)) {
            try {
                Date date = new SimpleDateFormat(format).parse(str);
                return date;
            } catch (ParseException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(e.getMessage());
                }
                // logger.warn(e.getMessage());
            }
        }
        return null;
    }

    /**
     * 时间拼接 将日期和实现拼接 ymd 如2012-05-15 hm 如0812
     */
    public static Date concat(String ymd, String hm) {
        if (!StringUtils.isEmpty(ymd) && !StringUtils.isEmpty(hm)) {
            try {
                String dateString = ymd.concat(" ").concat(
                        hm.substring(0, 2).concat(":")
                                .concat(hm.substring(2, 4)).concat(":00"));
                Date date = DateUtils.convert(dateString,
                        DateUtils.DATE_TIME_FORMAT);
                return date;
            } catch (NullPointerException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * 根据传入的日期返回年月日的6位字符串，例：20101203
     */
    public static String getDay(Date date) {
        return convert(date, DATE_SHORT_FORMAT);
    }

    /**
     * 根据传入的日期返回中文年月日字符串，例：2010年12月03日
     *
     * @param date
     * @return
     * @date 2015年8月19日
     * @update
     */
    public static String getChDate(Date date) {
        return convert(date, DATE_CH_FORMAT);
    }

    /**
     * 将传入的时间格式的字符串转成时间对象
     * <p>
     * 例：传入2012-12-03 23:21:24
     *
     * @param dateStr
     * @return
     * @date 2015年8月19日
     * @update
     */
    public static Date strToDate(String dateStr) {
        SimpleDateFormat formatDate = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date date = null;
        try {
            date = formatDate.parse(dateStr);
        } catch (Exception e) {

        }
        return date;
    }

    public static String convert(Date date) {
        return convert(date, DATE_TIME_FORMAT);
    }

    public static String convert(Date date, String dateFormat) {
        if (date == null) {
            return null;
        }

        if (null == dateFormat) {
            dateFormat = DATE_TIME_FORMAT;
        }

        return new SimpleDateFormat(dateFormat).format(date);
    }

    /**
     * 返回该天从00:00:00开始的日期
     *
     * @param date
     * @return
     */
    public static Date getStartDatetime(Date date) {
        String thisdate = convert(date, DATE_FORMAT);
        return convert(thisdate + " " + DAYTIME_START);

    }

    /**
     * 返回n天后从00:00:00开始的日期
     *
     * @param date
     * @return
     */
    public static Date getStartDatetime(Date date, Integer diffDays) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        String thisdate = df.format(date.getTime() + 1000l * 24 * 60 * 60
                * diffDays);
        return convert(thisdate + " " + DAYTIME_START);
    }

    /**
     * 返回该天到23:59:59结束的日期
     *
     * @param date
     * @return
     */
    public static Date getEndDatetime(Date date) {
        String thisdate = convert(date, DATE_FORMAT);
        return convert(thisdate + " " + DAYTIME_END);

    }

    /**
     * 返回n天到23:59:59结束的日期
     *
     * @param date
     * @return
     */
    public static Date getEndDatetime(Date date, Integer diffDays) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        String thisdate = df.format(date.getTime() + 1000l * 24 * 60 * 60
                * diffDays);
        return convert(thisdate + " " + DAYTIME_END);

    }

    /**
     * 返回该日期的最后一刻，精确到纳秒
     *
     * @param endTime
     * @return
     */
    public static Timestamp getLastEndDatetime(Date endTime) {
        Timestamp ts = new Timestamp(endTime.getTime());
        ts.setNanos(999999999);
        return ts;
    }

    /**
     * 返回该日期加1秒
     *
     * @param endTime
     * @return
     */
    public static Timestamp getEndTimeAdd(Date endTime) {
        Timestamp ts = new Timestamp(endTime.getTime());
        Calendar c = Calendar.getInstance();
        c.setTime(ts);
        c.add(Calendar.MILLISECOND, 1000);
        c.set(Calendar.MILLISECOND, 0);
        return new Timestamp(c.getTimeInMillis());
    }

    /**
     * 返回该日期加 millisecond 毫秒，正数为加，负数为减
     *
     * @param date
     * @param millisecond
     * @return
     */
    public static Timestamp getDateAdd(Date date, int millisecond) {
        Timestamp ts = new Timestamp(date.getTime());
        Calendar c = Calendar.getInstance();
        c.setTime(ts);
        c.add(Calendar.MILLISECOND, millisecond);
        c.set(Calendar.MILLISECOND, 0);
        return new Timestamp(c.getTimeInMillis());
    }

    /**
     * 相对当前日期，增加或减少天数
     *
     * @param date
     * @param day
     * @return
     */
    public static String addDay(Date date, int day) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

        return df.format(new Date(date.getTime() + 1000L * 24 * 60 * 60 * day));
    }

    /**
     * 相对当前日期，增加或减少天数
     *
     * @param date
     * @param day
     * @return
     */
    public static Date addDayToDate(Date date, int day) {
        return new Date(date.getTime() + 1000L * 24 * 60 * 60 * day);
    }

    /**
     * 相对当前日期，增加或减少秒数
     *
     * @param date
     * @param second
     * @return
     */
    public static Date addSecondToDate(Date date, long second) {
        return new Date(date.getTime() + 1000L * second);
    }

    /**
     * 返回两个时间的相差天数
     *
     * @param startTime 对比的开始时间
     * @param endTime   对比的结束时间
     * @return 相差天数
     */

    public static Long getTimeDiff(String startTime, String endTime) {
        Long days = null;
        Date startDate = null;
        Date endDate = null;
        try {
            if (startTime.length() == 10 && endTime.length() == 10) {
                startDate = new SimpleDateFormat(DATE_FORMAT).parse(startTime);
                endDate = new SimpleDateFormat(DATE_FORMAT).parse(endTime);
            } else {
                startDate = new SimpleDateFormat(DATE_TIME_FORMAT)
                        .parse(startTime);
                endDate = new SimpleDateFormat(DATE_TIME_FORMAT).parse(endTime);
            }

            days = getTimeDiff(startDate, endDate);
        } catch (ParseException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage());
            }
            days = null;
        }
        return days;
    }

    /**
     * 返回两个时间的相差天数
     *
     * @param startTime 对比的开始时间
     * @param endTime   对比的结束时间
     * @return 相差天数
     */
    public static Long getTimeDiff(Date startTime, Date endTime) {
        Long days = null;

        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        long l_s = c.getTimeInMillis();
        c.setTime(endTime);
        long l_e = c.getTimeInMillis();
        days = (l_e - l_s) / 86400000;
        return days;
    }

    /**
     * 返回两个时间的相差分钟数
     *
     * @param startTime 对比的开始时间
     * @param endTime   对比的结束时间
     * @return 相差分钟数
     */
    public static Long getMinuteDiff(Date startTime, Date endTime) {
        Long minutes = null;

        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        long l_s = c.getTimeInMillis();
        c.setTime(endTime);
        long l_e = c.getTimeInMillis();
        minutes = (l_e - l_s) / (1000l * 60);
        return minutes;
    }

    /**
     * 返回两个时间的相差秒数
     *
     * @param startTime 对比的开始时间
     * @param endTime   对比的结束时间
     * @return 相差秒数
     */
    public static Long getSecondDiff(Date startTime, Date endTime) {

        return (endTime.getTime() - startTime.getTime()) / 1000;
    }

    public static String getPidFromDate(Date date) {
        if (date == null) {
            return "";
        }

        String m = convert(date, "yyyyMM");
        String d = convert(date, "dd");

        if (Integer.valueOf(d) <= 10) {
            d = "01";
        } else if (Integer.valueOf(d) <= 20) {
            d = "02";
        } else {
            d = "03";
        }

        return m.concat(d);
    }

    /**
     * 判断当前时间是否在给定的两个时间段内,允许结束时间为空
     *
     * @param startTime
     * @param endTime
     * @return 开始时间不为空, 且当前时间大于开始时间, 且(截止时间为空或当前时间小于截止时间)--返回true
     * 其他--返回false
     */
    public static boolean nowIsBetween(Date startTime, Date endTime) {
        if (startTime == null) {
            return false;
        }
        Date now = new Date();
        if (now.compareTo(startTime) < 0) {
            return false;
        }
        if (endTime != null && now.compareTo(endTime) > 0) {
            return false;
        }
        return true;
    }

    /**
     * 判断当前时间是否在给定的两个时间段内,允许结束时间为空
     *
     * @param startTime
     * @param endTime
     * @return （开始时间为空，或开始时间不为空,且当前时间大于开始时间）,且(截止时间为空或当前时间小于截止时间)，但是不能同时为空--返回true
     * 其他--返回false
     */
    public static boolean isBetween(Date startTime, Date endTime) {
        if (startTime == null && endTime == null) {
            return false;
        }
        Date now = new Date();
        if (startTime != null && now.compareTo(startTime) < 0) {
            return false;
        }
        if (endTime != null && now.compareTo(endTime) > 0) {
            return false;
        }
        return true;
    }

    /**
     * 判断两个时间是否同一天
     *
     * @param dateA
     * @param dateB
     * @return
     */
    public static boolean isDiffDay(Date dateA, Date dateB) {
        if (dateA == null || dateB == null) {
            return false;
        }
        if (convert(dateA, DATE_SHORT_FORMAT).compareTo(convert(dateB, DATE_SHORT_FORMAT)) != 0) {
            return false;
        }
        return true;
    }

    /**
     * 获取日期指定的天数
     *
     * @param date      日期
     * @param hmsFormat 十分秒格式，ex：00:00:00
     * @return
     */
    public final static Date dateAssignHMS(Date date, String hmsFormat) {
        String thisdate = convert(date, DATE_FORMAT);
        return convert(thisdate + " " + hmsFormat);
    }

    /**
     * 判断两个日期是否相等
     * 若两个日期同为null，也认为日期相等
     *
     * @param dateA 参数A
     * @param dateB 参数B
     * @return 若2个日期相等则返回true，否则返回false
     */
    public final static boolean dateEqual(Date dateA, Date dateB) {
        if (dateA == null && dateB == null) {
            return true;
        }
        if (dateA == null || dateB == null) {
            return false;
        }
        return dateA.compareTo(dateB) == 0;
    }

    /**
     * @return
     * @description: 获取上一个月的同一天.
     * @author: Dy
     */
    public static Date getUpMonthWithCurrDay(Date sDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sDate);
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }

    /**
     * 将yyyy-MM-dd'T'HH:mm:ssZ格式的时间字符串转换成yyyy-MM-dd HH:mm:ss格式
     *
     * @param dateStr yyyy-MM-dd'T'HH:mm:ssZ 	时间格式字符串
     * @return
     * @author 陈宇霖
     * @date 2017年09月08日16:30:58
     */
    public static String getZonedDateTime(String dateStr) throws ParseException {
        //先尝试转换时间，如果转换报错，说明格式不对
        SimpleDateFormat sdf = new SimpleDateFormat(ZONED_DATE_TIME_FORMAT);
        sdf.parse(dateStr);
        String dateTimeStr = dateStr.substring(0, 19);
        String[] dateTime = dateTimeStr.split("T");
        return dateTime[0] + " " + dateTime[1].substring(0, 8);
    }

    /**
     * 从yyyy-MM-dd'T'HH:mm:ssZ格式的时间中获取时区信息
     *
     * @param dateStr yyyy-MM-dd'T'HH:mm:ssZ 	时间格式字符串
     * @return -12~12之间的数字
     * @throws ParseException
     * @author 陈宇霖
     * @date 2017年09月07日19:41:31
     */
    public static Integer getTimeZone(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(ZONED_DATE_TIME_FORMAT);
        sdf.parse(dateStr);
        String zoneFlag = dateStr.substring(19, 20);
        Integer zoneValue = Integer.parseInt(dateStr.substring(20, 22));
        if (zoneValue > 12 || zoneValue < -12) {
            throw new ParseException("error ZoneValue!", zoneValue);
        }
        return Integer.parseInt(zoneFlag + zoneValue);
    }

    /**
     * 将通过yyyy-MM-dd'T'HH:mm:ss解析出来的yyyy-MM-dd HH:mm:ss和zoneValue转换回去
     *
     * @param dateStr   yyyy-MM-dd HH:mm:ss格式日期字符串
     * @param zoneValue -12~12时区
     * @return
     * @author 陈宇霖
     * @date 2017年09月26日23:17:17
     */
    public static String revertGetTime(String dateStr, Integer zoneValue) {
        return revertGetZonedDateTime(dateStr) + revertGetTimeZone(zoneValue);
    }

    /**
     * 反向将yyyy-MM-dd HH:mm:ss格式的时间字符串转换成yyyy-MM-dd'T'HH:mm:ss格式
     *
     * @param dateStr
     * @return
     * @author 陈宇霖
     * @date 2017年09月26日23:14:30
     */
    public static String revertGetZonedDateTime(String dateStr) {
        String[] dateTime = dateStr.split(" ");
        return dateTime[0] + "T" + dateTime[1];
    }

    /**
     * 根据时区的值返回格式化的时区字符串
     *
     * @param zoneValue
     * @return
     * @author 陈宇霖
     * @date 2017年09月26日22:58:21
     */
    public static String revertGetTimeZone(Integer zoneValue) {
        if (zoneValue == null || zoneValue > 12 || zoneValue < -12) {
            return String.valueOf(zoneValue);
        }
        if (zoneValue >= 0) {
            if (zoneValue >= 10) {
                return "+" + String.valueOf(zoneValue) + "00";
            } else {
                return "+0" + String.valueOf(zoneValue) + "00";
            }
        } else {
            if (zoneValue <= -10) {
                return String.valueOf(zoneValue) + "00";
            } else {
                return "-0" + String.valueOf(-1 * zoneValue) + "00";
            }
        }
    }

    /**
     * 获取 yyyy-MM-dd'T'HH:mm:ssZ 	时间格式字符串对应的unix时间戳
     *
     * @param dateStr yyyy-MM-dd'T'HH:mm:ssZ 	时间格式字符串
     * @return unix时间戳
     * @throws ParseException
     * @author 陈宇霖
     * @date 2017年09月07日20:56:26
     */
    public static Long getTimeStamp(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(ZONED_DATE_TIME_FORMAT);
        return sdf.parse(dateStr).getTime();
    }

    /**
     * 根据时区获取时间戳之间的 天数
     *
     * @param start 开始时间
     * @param end 结束时间
     * @param timezone 对应的时区
     * @return long
     * @author clf
     * @date 2020/12/1
     */
    public static long getDaysDiffWithTimezone(Long start, Long end, Integer timezone) {
        SimpleDateFormat format = new SimpleDateFormat(DateUtils.DATE_SHORT_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("GMT" + DateUtils.revertGetTimeZone(timezone)));
        return DateUtils.getDaysDiffWithTimezone(format.format(new Date(start)), end, timezone);
    }

    public static long getDaysDiffWithTimezone(String start, long end, Integer timezone) {
        SimpleDateFormat format = new SimpleDateFormat(DateUtils.DATE_SHORT_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("GMT" + DateUtils.revertGetTimeZone(timezone)));
        return getDaysDiff(start, format.format(new Date(end)));
    }

    private static long getDaysDiff(String start, String end) {
        return DateUtils.getTimeDiff(DateUtils.convert(start, DateUtils.DATE_SHORT_FORMAT), DateUtils.convert(end, DateUtils.DATE_SHORT_FORMAT));
    }
}