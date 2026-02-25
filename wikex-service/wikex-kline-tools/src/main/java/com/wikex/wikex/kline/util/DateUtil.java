package com.wikex.wikex.kline.util;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static final DateFormat YYYY_MM_DD_MM_HH_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat HHMMSS = new SimpleDateFormat("HH:mm:ss");
    public static final DateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateFormat YYYYMMDDMMHHSSSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public static final DateFormat YYYY_MM_DD_MM_HH_SS_SSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static final DateFormat YYYYMMDDMMHHSSSS = new SimpleDateFormat("yyyyMMddHHmmssSS");

    public static final DateFormat YYYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final DateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");

    public static String dateToString(Date date) {
        return YYYY_MM_DD_MM_HH_SS.format(date);
    }

    public static String dateToStringDate(Date date) {
        return YYYY_MM_DD.format(date);
    }

	public static String dateToStringLengthIs17(Date date) {
		return YYYYMMDDMMHHSSSSS.format(date);
	}

    public static String dateTo3S(Date date){
        return YYYY_MM_DD_MM_HH_SS_SSS.format(date);
    }

    /**
     * Validate start and end date
     */
    public static void validateDate(Date startDate, Date endDate) {
        Date currentDate = DateUtil.getCurrentDate();
        int compare = compare(startDate, currentDate);
        int compare2 = compare(startDate, endDate);
        Assert.isTrue(compare != -1, "startDate cannot be less than currentDate!");
        Assert.isTrue(compare2 != 1, "startDate must be less than endDate!");
    }

    /**
     * Validate end date
     */
    public static void validateEndDate(Date endDate) {
        Date currentDate = DateUtil.getCurrentDate();
        int compare = compare(currentDate, endDate);
        Assert.isTrue(compare != 1, "currentDate must be less than endDate!");
    }

    public static int compare(Date date1, Date date2) {
        try {
            if (date1.getTime() > date2.getTime()) {
                return 1;
            } else if (date1.getTime() < date2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public static String getDateTime() {
        return YYYY_MM_DD_MM_HH_SS.format(new Date());
    }

    public static Date getStringToDate3S(String dateString){
        try {
            return YYYYMMDDMMHHSSSSS.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date getStringToDate2S(String dateString){
        try {
            return YYYYMMDDMMHHSSSS.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDate() {
        return YYYY_MM_DD.format(new Date());
    }

    public static String getDateYMD() {
        return YYYYMMDD.format(new Date());
    }

    public static String getDateYMD(Date date) {
        return YYYYMMDD.format(date);
    }

    public static Date strToDate(String dateString) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date strToYYMMDDDate(String dateString) {
        Date date = null;
        try {
            date = YYYY_MM_DD.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static long diffDays(Date startDate, Date endDate) {
        long days = 0L;
        long start = startDate.getTime();
        long end = endDate.getTime();
        days = (end - start) / 86400000L;
        return days;
    }

    public static Date dateAddMonth(Date date, int month) {
        return add(date, 2, month);
    }

    public static Date dateAddDay(Date date, int day) {
        return add(date, 6, day);
    }

    public static Date dateAddYear(Date date, int year) {
        return add(date, 1, year);
    }

    public static String dateAddDay(String dateString, int day) {
        Date date = strToYYMMDDDate(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(6, day);
        return YYYY_MM_DD.format(calendar.getTime());
    }

    public static String dateAddDay(int day) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(6, day);
        return YYYY_MM_DD.format(calendar.getTime());
    }

    public static String dateAddMonth(int month) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(2, month);
        return YYYY_MM_DD.format(calendar.getTime());
    }

    /**
     * Calculate remaining time between startDate and endDate and return as string
     */
    public static String remainDateToString(Date startDate, Date endDate) {
        StringBuilder result = new StringBuilder();
        if (endDate == null) {
            return "Expired";
        }
        long times = endDate.getTime() - startDate.getTime();
        if (times < -1L) {
            result.append("Expired");
        } else {
            long temp = 86400000L;

            long d = times / temp;

            times %= temp;
            temp /= 24L;
            long m = times / temp;

            times %= temp;
            temp /= 60L;
            long s = times / temp;

            result.append(d);
            result.append(" days ");
            result.append(m);
            result.append(" hours ");
            result.append(s);
            result.append(" minutes");
        }
        return result.toString();
    }

    private static Date add(Date date, int type, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(type, value);
        return calendar.getTime();
    }

    public static String getLinkUrl(boolean flag, String content, String id) {
        if (flag) {
            content = "<a href='finance.do?id=" + id + "'>" + content + "</a>";
        }
        return content;
    }

    public static long getTimeCur(String format, String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.parse(sf.format(date)).getTime();
    }

    public static long getTimeCur(String format, Date date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.parse(sf.format(date)).getTime();
    }

    public static String getStrTime(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        long lcc_time = Long.valueOf(cc_time).longValue();
        re_StrTime = sdf.format(new Date(lcc_time * 1000L));
        return re_StrTime;
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static String getFormatTime(DateFormat format, Date date) throws ParseException {
        return format.format(date);
    }

    public static long getTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Get week day string
     */
    public static String getWeekDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
            return "";
        }
    }

    public static String toGMTString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK);
        df.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    public static BigDecimal diffMinute(Date date) {
        return BigDecimalUtils.div(new BigDecimal(System.currentTimeMillis() - date.getTime()), new BigDecimal("60000"));
    }

    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        return result;
    }

    public static String getFetureDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        return result;
    }

    public static int getDatePart(Date date,int part) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(part);
    }

    public static Date getDate(Date date , int day){
        synchronized (YYYY_MM_DD){
            Calendar calendar = Calendar.getInstance() ;
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH,-day);
            date = calendar.getTime() ;
            try {
                return YYYY_MM_DD.parse(YYYY_MM_DD.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static String getDateRandom() {
    	return DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")+(int)((Math.random()*9+1)*10000);
    }

    public static Date getDateNoTime(Date curDate,int amount) {
    	Calendar cal = Calendar.getInstance();
		cal.setTime(curDate);
		cal.add(Calendar.DAY_OF_MONTH, amount);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
    }
}
