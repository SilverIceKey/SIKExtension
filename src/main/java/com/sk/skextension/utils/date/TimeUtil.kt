package com.sk.skextension.utils.date

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间相关
 */
class TimeUtil {
    companion object {
        /**
         * 默认日期格式
         */
        val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"

        /**
         * 默认日期格式带小时
         */
        val DEFAULT_DATE_HOUR_FORMAT = "yyyy-MM-dd HH"

        /**
         * 默认日期格式带分钟
         */
        val DEFAULT_DATE_HOUR_MIN_FORMAT = "yyyy-MM-dd HH:mm"
        /**
         * 默认日期格式带分钟秒毫秒
         */
        val DEFAULT_DATE_HOUR_MIN_SEC_MILL_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS"

        /**
         * 一分钟
         */
        val MIN_TIME = 60 * 1000L

        /**
         * 一小时
         */
        val HOUR_TIME = MIN_TIME * 60

        /**
         * 一天
         */
        val DAY_TIME = HOUR_TIME * 24

        /**
         * 日期转换器
         */
        val simpleDateDayFormat = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.CHINA)

        /**
         * 日期转换器小时
         */
        val simpleDateHourFormat = SimpleDateFormat(DEFAULT_DATE_HOUR_FORMAT, Locale.CHINA)

        /**
         * 日期转换器小时分钟
         */
        val simpleDateHourMinFormat = SimpleDateFormat(DEFAULT_DATE_HOUR_MIN_FORMAT, Locale.CHINA)

        /**
         * timeutil单例
         */
        val instance: TimeUtil by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            TimeUtil()
        }
    }

    /**
     * 计算到目前的时间
     */
    fun getTimeIntervalofCur(time: String): String {
        return getTimeIntervalofCur(
            time, SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault(Locale.Category.FORMAT)
            )
        )
    }

    /**
     * 计算到目前的时间
     */
    fun getTimeIntervalofCur(time: String, timeFormatter: DateFormat): String {
        val timeSecond: Long = timeFormatter.parse(time, ParsePosition(0))!!.getTime() / 1000
        return getTimeIntervalofCur(timeSecond)
    }

    /**
     * 计算到目前的时间
     */
    fun getTimeIntervalofCur(time: Long): String {
        val curTime = System.currentTimeMillis() / 1000
        val timeInterval = curTime - time
        if (timeInterval < 300) {
            return "刚刚"
        } else if (timeInterval < 60 * 60) {
            return "${timeInterval / 60}分钟前"
        } else if (timeInterval < 60 * 60 * 24) {
            return "${timeInterval / 60 / 60}小时前"
        } else if (timeInterval < 60 * 60 * 24 * 30) {
            return "${timeInterval / 60 / 60 / 24}天前"
        } else if (timeInterval < 60 * 60 * 24 * 30 * 12) {
            return "${timeInterval / 60 / 60 / 24 / 30}个月前"
        }
        return ""
    }

    /**
     * 判断是否是今天
     */
    @SuppressLint("SimpleDateFormat")
    fun isToday(date: String, dateFormat: String = DEFAULT_DATE_FORMAT): Boolean {
        val simpleDateFormat: SimpleDateFormat
        if (dateFormat == DEFAULT_DATE_FORMAT) {
            simpleDateFormat = simpleDateDayFormat
        } else {
            simpleDateFormat = SimpleDateFormat(dateFormat, Locale.CHINA)
        }
        val formatNowDate = simpleDateFormat.format(Date())
        return date.equals(formatNowDate)
    }

    /**
     * 时间偏移天数
     */
    fun offsetDay(offsetValue: Int, date: Date = Date()): Date {
        val formatDate = simpleDateDayFormat.format(date)
        val parseDate = simpleDateDayFormat.parse(formatDate)
        return Date(parseDate!!.time + offsetValue * DAY_TIME)
    }

    /**
     * 时间偏移小时
     */
    fun offsetHour(offsetValue: Int, date: Date = Date()): Date {
        val formatDate = simpleDateHourFormat.format(date)
        val parseDate = simpleDateHourFormat.parse(formatDate)
        return Date(parseDate!!.time + offsetValue * HOUR_TIME)
    }

    /**
     * 时间偏移分钟
     */
    fun offsetMin(offsetValue: Int, date: Date = Date()): Date {
        val formatDate = simpleDateHourMinFormat.format(date)
        val parseDate = simpleDateHourMinFormat.parse(formatDate)
        return Date(parseDate!!.time + offsetValue * MIN_TIME)
    }

    /**
     * 获取当前时间
     */
    fun now(): Date {
        return Date()
    }

    /**
     * 获取当前时间字符串
     */
    fun nowString(dateFormat: String = DEFAULT_DATE_FORMAT): String {
        return SimpleDateFormat(dateFormat, Locale.CHINA).format(now())
    }

    /**
     * 获取今天日期
     */
    fun today(): Date {
        return SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.CHINA).parse(nowString())!!
    }

    /**
     * 判断日期是否在某日期之前
     */
    fun isTimeBefore(realDate: Date, referenceDate: Date): Boolean {
        return realDate.before(referenceDate)
    }

    /**
     * 判断日期是否在某日期之前
     */
    fun isTimeBefore(realDate: String, referenceDate: String): Boolean {
        return simpleDateDayFormat.parse(realDate)!!
            .before(simpleDateDayFormat.parse(referenceDate))
    }

    /**
     * 判断日期是否在今天之前
     */
    fun isTimeBeforeToday(realDate: Date): Boolean {
        return realDate.before(today())
    }

    /**
     * 判断日期是否在今天之前
     */
    fun isTimeBeforeToday(realDate: String): Boolean {
        return simpleDateDayFormat.parse(realDate)!!.before(today())
    }

    /**
     * 时间仅保留日期返回Date
     */
    fun getDateOnly(date: Date): Date {
        return SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.CHINA).parse(
            SimpleDateFormat(
                DEFAULT_DATE_FORMAT,
                Locale.CHINA
            ).format(date)
        )!!
    }

    /**
     * 计算时间天数
     */
    fun calcDayNum(beforeDate: Date, afterDate: Date): Long {
        return (afterDate.time - beforeDate.time) / DAY_TIME
    }

    /**
     * 计算时间天数
     */
    fun calcDayNum(beforeDate: String, afterDate: String): Long {
        return (simpleDateDayFormat.parse(beforeDate)!!.time - simpleDateDayFormat.parse(afterDate)!!.time) / DAY_TIME
    }

}