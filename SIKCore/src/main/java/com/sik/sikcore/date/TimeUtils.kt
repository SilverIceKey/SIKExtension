package com.sik.sikcore.date

import android.annotation.SuppressLint
import com.sik.sikcore.log.LogUtils
import java.text.DateFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间相关
 */
class TimeUtils {
    companion object {
        /**
         * 默认日期格式
         */
        const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"

        /**
         * 默认日期格式带小时
         */
        const val DEFAULT_DATE_HOUR_FORMAT = "yyyy-MM-dd HH"

        /**
         * 默认日期格式带分钟
         */
        const val DEFAULT_DATE_HOUR_MIN_FORMAT = "yyyy-MM-dd HH:mm"

        /**
         * 默认日期格式带分钟秒
         */
        const val DEFAULT_DATE_HOUR_MIN_SEC_FORMAT = "yyyy-MM-dd HH:mm:ss"

        /**
         * 默认日期格式带分钟秒毫秒
         */
        const val DEFAULT_DATE_HOUR_MIN_SEC_MILL_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS"

        /**
         * 日期转换器
         */
        @JvmStatic
        val simpleDateDayFormat = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.CHINA)

        /**
         * 日期转换器小时
         */
        @JvmStatic
        val simpleDateHourFormat = SimpleDateFormat(DEFAULT_DATE_HOUR_FORMAT, Locale.CHINA)

        /**
         * 日期转换器小时分钟
         */
        @JvmStatic
        val simpleDateHourMinFormat = SimpleDateFormat(DEFAULT_DATE_HOUR_MIN_FORMAT, Locale.CHINA)

        /**
         * 日期转换器小时分钟秒
         */
        @JvmStatic
        val simpleDateHourMinSecFormat = SimpleDateFormat(DEFAULT_DATE_HOUR_MIN_SEC_FORMAT, Locale.CHINA)

        /**
         * timeutil单例
         */
        @JvmStatic
        val instance: TimeUtils by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            TimeUtils()
        }
    }

    private val logger = LogUtils.getLogger(TimeUtils::class)

    /**
     * 计算到目前的时间
     */
    @JvmOverloads
    fun getTimeIntervalOfCur(
        time: String,
        timeFormatter: DateFormat = simpleDateHourMinSecFormat // 使用你提供的默认格式
    ): String {
        val date = timeFormatter.parse(time, ParsePosition(0))
        date?.let {
            val timeSecond: Long = it.time / 1000
            return getTimeIntervalOfCur(timeSecond)
        }
        return "时间格式错误"
    }

    /**
     * 计算到目前的时间
     */
    fun getTimeIntervalOfCur(time: Long): String {
        val curTime = System.currentTimeMillis() / 1000
        val timeInterval = curTime - time

        return when {
            timeInterval < 300 -> "刚刚"
            timeInterval < 60 * 60 -> "${timeInterval / 60}分钟前"
            timeInterval < 60 * 60 * 24 -> "${timeInterval / (60 * 60)}小时前"
            timeInterval < 60 * 60 * 24 * 30 -> "${timeInterval / (60 * 60 * 24)}天前"
            timeInterval < 60 * 60 * 24 * 365 -> "${timeInterval / (60 * 60 * 24 * 30)}个月前"
            else -> "${timeInterval / (60 * 60 * 24 * 365)}年前"
        }
    }

    /**
     * 判断是否是今天
     */
    @JvmOverloads
    fun isToday(date: String, dateFormat: String = DEFAULT_DATE_FORMAT): Boolean {
        val formatNowDate = SimpleDateFormat(dateFormat, Locale.CHINA).format(Date())
        return date == formatNowDate
    }

    /**
     * 时间偏移天数
     */
    @JvmOverloads
    fun offsetDay(offsetValue: Int, date: Date = Date()): Date {
        return offsetDate(Calendar.DAY_OF_MONTH, offsetValue, date)
    }

    /**
     * 时间偏移小时
     */
    @JvmOverloads
    fun offsetHour(offsetValue: Int, date: Date = Date()): Date {
        return offsetDate(Calendar.HOUR_OF_DAY, offsetValue, date)
    }

    /**
     * 时间偏移分钟
     */
    @JvmOverloads
    fun offsetMin(offsetValue: Int, date: Date = Date()): Date {
        return offsetDate(Calendar.MINUTE, offsetValue, date)
    }

    /**
     * 时间偏移秒
     */
    @JvmOverloads
    fun offsetSec(offsetValue: Int, date: Date = Date()): Date {
        return offsetDate(Calendar.SECOND, offsetValue, date)
    }

    private fun offsetDate(field: Int, offsetValue: Int, date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(field, offsetValue)
        return calendar.time
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
        return formatDate(now(), dateFormat)
    }

    /**
     * 获取今天日期
     */
    fun today(): Date {
        return getDateOnly(now())
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
        return isTimeBefore(realDate, today())
    }

    /**
     * 判断日期是否在今天之前
     */
    fun isTimeBeforeToday(realDateStr: String): Boolean {
        val formatter = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
        val realDate = formatter.parse(realDateStr)
        return realDate?.let { isTimeBeforeToday(it) } ?: false
    }

    /**
     * 时间仅保留日期返回Date
     */
    fun getDateOnly(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    /**
     * 计算时间天数
     */
    fun calcDayNum(startDate: Date, endDate: Date): Long {
        val startCal = Calendar.getInstance().apply {
            time = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            time = endDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diff = endCal.timeInMillis - startCal.timeInMillis
        return diff / (24 * 60 * 60 * 1000)
    }

    /**
     * 计算时间天数
     */
    fun calcDayNum(beforeDate: String, afterDate: String): Long {
        return try {
            calcDayNum(
                simpleDateDayFormat.parse(beforeDate),
                simpleDateDayFormat.parse(afterDate)
            )
        } catch (e: Exception) {
            logger.e("时间转换错误")
            0
        }
    }

    /**
     * 通过时间格式转化时间
     */
    fun dateFormatToDateFormat(
        sourceDateFormat: String,
        targetDateFormat: String,
        date: String
    ): String {
        val sourceSimpleDateFormat = SimpleDateFormat(sourceDateFormat, Locale.CHINA)
        val targetSimpleDateFormat = SimpleDateFormat(targetDateFormat, Locale.CHINA)
        val parseDate = sourceSimpleDateFormat.parse(date)
        return targetSimpleDateFormat.format(parseDate!!)
    }

    /**
     * 倒计时输出分钟和秒 例:01:30
     */
    fun getTimeStr(timeMillis: Long): String {
        val min = String.format("%02d", timeMillis / 1000 / 60)
        val sec = String.format("%02d", timeMillis / 1000 % 60)
        return "$min:$sec"
    }

    /**
     * 计算指定时间到现在的时间差
     */
    @SuppressLint("SimpleDateFormat")
    fun calcOffsetTime(sourceTime: String, timeDateFormat: String): Int {
        val deviceTime = now().time
        return ((SimpleDateFormat(timeDateFormat).parse(sourceTime)!!.time - deviceTime) / 1000L).toInt()
    }

    /**
     * 将日期格式化为字符串
     */
    fun formatDate(date: Date, format: String = DEFAULT_DATE_FORMAT): String {
        return SimpleDateFormat(format, Locale.CHINA).format(date)
    }

    /**
     * 将字符串解析为日期
     */
    fun parseDate(dateStr: String, format: String = DEFAULT_DATE_FORMAT): Date? {
        return SimpleDateFormat(format, Locale.CHINA).parse(dateStr)
    }

    /**
     * 获取当前时间的时间戳（毫秒）
     */
    fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    /**
     * 计算两个时间之间的差异（毫秒）
     */
    fun diffInMillis(startDate: Date, endDate: Date): Long {
        return endDate.time - startDate.time
    }

    /**
     * 计算两个时间之间的差异（秒）
     */
    fun diffInSeconds(startDate: Date, endDate: Date): Long {
        return (endDate.time - startDate.time) / 1000
    }

    /**
     * 计算两个时间之间的差异（分钟）
     */
    fun diffInMinutes(startDate: Date, endDate: Date): Long {
        return (endDate.time - startDate.time) / (60 * 1000)
    }

    /**
     * 计算两个时间之间的差异（小时）
     */
    fun diffInHours(startDate: Date, endDate: Date): Long {
        return (endDate.time - startDate.time) / (60 * 60 * 1000)
    }

    /**
     * 计算两个时间之间的差异（天）
     */
    fun diffInDays(startDate: Date, endDate: Date): Long {
        return (endDate.time - startDate.time) / (24 * 60 * 60 * 1000)
    }
}
