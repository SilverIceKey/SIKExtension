package com.sik.sikcore.date

import android.annotation.SuppressLint
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间相关工具类
 */
object TimeUtils {

    private val logger = LoggerFactory.getLogger(TimeUtils::class.java)

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
     * 日期格式化器缓存，避免重复创建
     */
    private val dateFormatMap = mutableMapOf<String, ThreadLocal<SimpleDateFormat>>()

    /**
     * 获取指定格式的 SimpleDateFormat，使用 ThreadLocal 保证线程安全
     */
    private fun getDateFormat(pattern: String): SimpleDateFormat {
        val threadLocal = dateFormatMap.getOrPut(pattern) {
            ThreadLocal<SimpleDateFormat>()
        }
        return threadLocal.get() ?: run {
            val sdf = SimpleDateFormat(pattern, Locale.CHINA)
            threadLocal.set(sdf)
            sdf
        }
    }

    /**
     * 计算指定时间与当前时间的间隔描述
     * @param time 时间字符串
     * @param timeFormatter 日期格式化器，默认为 "yyyy-MM-dd HH:mm:ss"
     * @return 间隔描述，例如 "5分钟前"
     */
    @JvmOverloads
    fun getTimeIntervalOfCur(
        time: String,
        timeFormatter: DateFormat = getDateFormat(DEFAULT_DATE_HOUR_MIN_SEC_FORMAT)
    ): String {
        val date = timeFormatter.parse(time, ParsePosition(0))
        return if (date != null) {
            val timeSecond = date.time / 1000
            getTimeIntervalOfCur(timeSecond)
        } else {
            "时间格式错误"
        }
    }

    /**
     * 计算指定时间戳与当前时间的间隔描述
     * @param time 时间戳（秒）
     * @return 间隔描述
     */
    fun getTimeIntervalOfCur(time: Long): String {
        val curTime = System.currentTimeMillis() / 1000
        val timeInterval = curTime - time

        return when {
            timeInterval < 300 -> "刚刚"
            timeInterval < 3600 -> "${timeInterval / 60}分钟前"
            timeInterval < 86400 -> "${timeInterval / 3600}小时前"
            timeInterval < 2592000 -> "${timeInterval / 86400}天前"
            timeInterval < 31536000 -> "${timeInterval / 2592000}个月前"
            else -> "${timeInterval / 31536000}年前"
        }
    }

    /**
     * 判断指定日期字符串是否是今天
     * @param date 日期字符串
     * @param dateFormat 日期格式，默认为 "yyyy-MM-dd"
     * @return 是否是今天
     */
    @JvmOverloads
    fun isToday(date: String, dateFormat: String = DEFAULT_DATE_FORMAT): Boolean {
        val formatter = getDateFormat(dateFormat)
        val inputDate = formatter.parse(date, ParsePosition(0))
        val today = getDateOnly(Date())
        return inputDate?.let { getDateOnly(it) == today } ?: false
    }

    /**
     * 对日期进行偏移
     * @param field 偏移的时间单位，例如 Calendar.DAY_OF_MONTH
     * @param offsetValue 偏移量，正数表示未来，负数表示过去
     * @param date 基准日期，默认为当前日期
     * @return 偏移后的日期
     */
    private fun offsetDate(field: Int, offsetValue: Int, date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(field, offsetValue)
        return calendar.time
    }

    /**
     * 日期偏移
     */
    fun offsetDay(offsetValue: Int, date: Date = Date()): Date =
        offsetDate(Calendar.DAY_OF_MONTH, offsetValue, date)

    fun offsetHour(offsetValue: Int, date: Date = Date()): Date =
        offsetDate(Calendar.HOUR_OF_DAY, offsetValue, date)

    fun offsetMin(offsetValue: Int, date: Date = Date()): Date =
        offsetDate(Calendar.MINUTE, offsetValue, date)

    fun offsetSec(offsetValue: Int, date: Date = Date()): Date =
        offsetDate(Calendar.SECOND, offsetValue, date)

    /**
     * 获取当前时间
     */
    fun now(): Date = Date()

    /**
     * 获取当前时间字符串
     * @param dateFormat 日期格式，默认为 "yyyy-MM-dd"
     * @return 格式化后的日期字符串
     */
    fun nowString(dateFormat: String = DEFAULT_DATE_FORMAT): String =
        formatDate(now(), dateFormat)

    /**
     * 获取今天的日期（仅保留日期部分，时间设为 00:00:00）
     */
    fun today(): Date = getDateOnly(Date())

    /**
     * 判断日期是否在参考日期之前
     * @param realDate 实际日期
     * @param referenceDate 参考日期
     * @return 是否在参考日期之前
     */
    fun isTimeBefore(realDate: Date, referenceDate: Date): Boolean = realDate.before(referenceDate)

    /**
     * 判断日期字符串是否在参考日期字符串之前
     * @param realDate 实际日期字符串
     * @param referenceDate 参考日期字符串
     * @param dateFormat 日期格式，默认为 "yyyy-MM-dd"
     * @return 是否在参考日期之前
     */
    fun isTimeBefore(
        realDate: String,
        referenceDate: String,
        dateFormat: String = DEFAULT_DATE_FORMAT
    ): Boolean {
        val formatter = getDateFormat(dateFormat)
        val real = formatter.parse(realDate, ParsePosition(0))
        val reference = formatter.parse(referenceDate, ParsePosition(0))
        return if (real != null && reference != null) {
            real.before(reference)
        } else {
            false
        }
    }

    /**
     * 判断日期是否在今天之前
     * @param realDate 实际日期
     * @return 是否在今天之前
     */
    fun isTimeBeforeToday(realDate: Date): Boolean = isTimeBefore(realDate, today())

    /**
     * 判断日期字符串是否在今天之前
     * @param realDateStr 实际日期字符串
     * @param dateFormat 日期格式，默认为 "yyyy-MM-dd"
     * @return 是否在今天之前
     */
    fun isTimeBeforeToday(realDateStr: String, dateFormat: String = DEFAULT_DATE_FORMAT): Boolean {
        val formatter = getDateFormat(dateFormat)
        val realDate = formatter.parse(realDateStr, ParsePosition(0))
        return realDate?.let { isTimeBeforeToday(it) } ?: false
    }

    /**
     * 获取日期的日期部分（时间设为 00:00:00）
     * @param date 原始日期
     * @return 仅保留日期部分的日期
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
     * 计算两个日期之间的天数差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    fun calcDayNum(startDate: Date, endDate: Date): Long {
        val start = getDateOnly(startDate).time
        val end = getDateOnly(endDate).time
        val diff = end - start
        return diff / (24 * 60 * 60 * 1000)
    }

    /**
     * 计算两个日期字符串之间的天数差
     * @param startDateStr 开始日期字符串
     * @param endDateStr 结束日期字符串
     * @param dateFormat 日期格式，默认为 "yyyy-MM-dd"
     * @return 天数差
     */
    fun calcDayNum(
        startDateStr: String,
        endDateStr: String,
        dateFormat: String = DEFAULT_DATE_FORMAT
    ): Long {
        return try {
            val formatter = getDateFormat(dateFormat)
            val startDate = formatter.parse(startDateStr, ParsePosition(0))
            val endDate = formatter.parse(endDateStr, ParsePosition(0))
            if (startDate != null && endDate != null) {
                calcDayNum(startDate, endDate)
            } else {
                0
            }
        } catch (e: Exception) {
            logger.error("时间转换错误", e)
            0
        }
    }

    /**
     * 将日期字符串从一种格式转换为另一种格式
     * @param sourceDateFormat 源日期格式
     * @param targetDateFormat 目标日期格式
     * @param date 日期字符串
     * @return 转换后的日期字符串
     */
    fun dateFormatToDateFormat(
        sourceDateFormat: String,
        targetDateFormat: String,
        date: String
    ): String {
        return try {
            val sourceFormat = getDateFormat(sourceDateFormat)
            val targetFormat = getDateFormat(targetDateFormat)
            val parseDate = sourceFormat.parse(date, ParsePosition(0))
            if (parseDate != null) {
                targetFormat.format(parseDate)
            } else {
                ""
            }
        } catch (e: Exception) {
            logger.error("日期格式转换错误", e)
            ""
        }
    }

    /**
     * 将毫秒数转换为倒计时字符串，格式为 "mm:ss"
     * @param timeMillis 毫秒数
     * @return 倒计时字符串
     */
    fun getTimeStr(timeMillis: Long): String {
        val totalSeconds = timeMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    /**
     * 计算指定时间与当前时间的偏移（秒）
     * @param sourceTime 源时间字符串
     * @param timeDateFormat 时间格式
     * @return 时间偏移（秒）
     */
    fun calcOffsetTime(sourceTime: String, timeDateFormat: String): Int {
        return try {
            val formatter = getDateFormat(timeDateFormat)
            val sourceDate = formatter.parse(sourceTime, ParsePosition(0))
            val deviceTime = now().time
            if (sourceDate != null) {
                ((sourceDate.time - deviceTime) / 1000L).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            logger.error("时间计算错误", e)
            0
        }
    }

    /**
     * 将日期格式化为字符串
     * @param date 日期
     * @param format 日期格式，默认为 "yyyy-MM-dd"
     * @return 格式化后的日期字符串
     */
    fun formatDate(date: Date, format: String = DEFAULT_DATE_FORMAT): String {
        val formatter = getDateFormat(format)
        return formatter.format(date)
    }

    /**
     * 将日期字符串解析为日期对象
     * @param dateStr 日期字符串
     * @param format 日期格式，默认为 "yyyy-MM-dd"
     * @return 日期对象，解析失败时返回 null
     */
    fun parseDate(dateStr: String, format: String = DEFAULT_DATE_FORMAT): Date? {
        val formatter = getDateFormat(format)
        return formatter.parse(dateStr, ParsePosition(0))
    }

    /**
     * 获取当前时间的时间戳（毫秒）
     */
    fun currentTimeMillis(): Long = System.currentTimeMillis()

    /**
     * 计算两个日期之间的毫秒差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 毫秒差
     */
    fun diffInMillis(startDate: Date, endDate: Date): Long = endDate.time - startDate.time

    /**
     * 计算两个日期之间的秒差
     */
    fun diffInSeconds(startDate: Date, endDate: Date): Long = diffInMillis(startDate, endDate) / 1000

    /**
     * 计算两个日期之间的分钟差
     */
    fun diffInMinutes(startDate: Date, endDate: Date): Long =
        diffInMillis(startDate, endDate) / (60 * 1000)

    /**
     * 计算两个日期之间的小时差
     */
    fun diffInHours(startDate: Date, endDate: Date): Long =
        diffInMillis(startDate, endDate) / (60 * 60 * 1000)

    /**
     * 计算两个日期之间的天数差
     */
    fun diffInDays(startDate: Date, endDate: Date): Long =
        diffInMillis(startDate, endDate) / (24 * 60 * 60 * 1000)

    /**
     * 将时间戳（毫秒）转换为日期对象
     * @param timestamp 时间戳（毫秒）
     * @return 日期对象
     */
    fun timestampToDate(timestamp: Long): Date {
        return Date(timestamp)
    }

    /**
     * 将日期对象转换为时间戳（毫秒）
     * @param date 日期对象
     * @return 时间戳（毫秒）
     */
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }

    /**
     * 获取日期的年份
     */
    fun getYear(date: Date = Date()): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.YEAR)
    }

    /**
     * 获取日期的月份（1-12）
     */
    fun getMonth(date: Date = Date()): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.MONTH) + 1
    }

    /**
     * 获取日期的日（1-31）
     */
    fun getDayOfMonth(date: Date = Date()): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * 获取日期的小时（0-23）
     */
    fun getHour(date: Date = Date()): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    /**
     * 获取日期的分钟（0-59）
     */
    fun getMinute(date: Date = Date()): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.MINUTE)
    }

    /**
     * 获取日期的秒（0-59）
     */
    fun getSecond(date: Date = Date()): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.SECOND)
    }

    /**
     * 获取日期是星期几（1-7，周日为1）
     */
    fun getDayOfWeek(date: Date = Date()): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * 获取星期的中文名称
     */
    fun getDayOfWeekChinese(date: Date = Date()): String {
        return when (getDayOfWeek(date)) {
            Calendar.SUNDAY -> "星期日"
            Calendar.MONDAY -> "星期一"
            Calendar.TUESDAY -> "星期二"
            Calendar.WEDNESDAY -> "星期三"
            Calendar.THURSDAY -> "星期四"
            Calendar.FRIDAY -> "星期五"
            Calendar.SATURDAY -> "星期六"
            else -> ""
        }
    }

    /**
     * 将持续时间（毫秒）格式化为可读的字符串
     * @param durationMillis 持续时间（毫秒）
     * @return 格式化后的字符串
     */
    fun formatDuration(durationMillis: Long): String {
        val seconds = durationMillis / 1000 % 60
        val minutes = durationMillis / (1000 * 60) % 60
        val hours = durationMillis / (1000 * 60 * 60) % 24
        val days = durationMillis / (1000 * 60 * 60 * 24)

        val builder = StringBuilder()
        if (days > 0) {
            builder.append("${days}天")
        }
        if (hours > 0) {
            builder.append("${hours}小时")
        }
        if (minutes > 0) {
            builder.append("${minutes}分钟")
        }
        if (seconds > 0) {
            builder.append("${seconds}秒")
        }
        return if (builder.isNotEmpty()) builder.toString() else "0秒"
    }

    /**
     * 验证日期字符串是否符合指定的格式
     * @param dateStr 日期字符串
     * @param format 日期格式，默认为 "yyyy-MM-dd"
     * @return 是否符合格式
     */
    fun isValidDateFormat(dateStr: String, format: String = DEFAULT_DATE_FORMAT): Boolean {
        val formatter = getDateFormat(format)
        return try {
            val date = formatter.parse(dateStr, ParsePosition(0))
            date != null && dateStr == formatter.format(date)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取指定年月的天数
     * @param year 年份
     * @param month 月份（1-12）
     * @return 天数
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // 月份从0开始
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * 判断某年是否为闰年
     * @param year 年份
     * @return 是否为闰年
     */
    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * 获取当前时间是上午还是下午
     * @param date 日期对象，默认为当前时间
     * @return "AM" 表示上午，"PM" 表示下午
     */
    fun getAmOrPm(date: Date = Date()): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val amPm = calendar.get(Calendar.AM_PM)
        return if (amPm == Calendar.AM) "AM" else "PM"
    }

    /**
     * 将日期转换为相对时间描述
     * @param date 日期
     * @return 相对时间描述
     */
    fun getRelativeTimeDescription(date: Date): String {
        val diff = System.currentTimeMillis() - date.time
        val seconds = diff / 1000
        return when {
            seconds < 60 -> "刚刚"
            seconds < 3600 -> "${seconds / 60}分钟前"
            seconds < 86400 -> "${seconds / 3600}小时前"
            seconds < 172800 -> "昨天"
            seconds < 2592000 -> "${seconds / 86400}天前"
            else -> formatDate(date, DEFAULT_DATE_FORMAT)
        }
    }

    /**
     * 生成两个日期之间的所有日期列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期列表
     */
    fun getDateListBetween(startDate: Date, endDate: Date): List<Date> {
        val dateList = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time = getDateOnly(startDate)
        val end = getDateOnly(endDate).time
        while (calendar.time.time <= end) {
            dateList.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dateList
    }

    /**
     * 获取某天的开始时间
     * @param date 指定的日期，默认为当前日期
     * @return 日期的开始时间
     */
    fun getStartOfDay(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * 获取某天的结束时间
     * @param date 指定的日期，默认为当前日期
     * @return 日期的结束时间
     */
    fun getEndOfDay(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    /**
     * 获取本周的开始日期（周一）
     */
    fun getStartOfWeek(): Date {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return getDateOnly(calendar.time)
    }

    /**
     * 获取本周的结束日期（周日）
     */
    fun getEndOfWeek(): Date {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        return getDateOnly(calendar.time)
    }

    /**
     * 获取本月的开始日期
     */
    fun getStartOfMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return getDateOnly(calendar.time)
    }

    /**
     * 获取本月的结束日期
     */
    fun getEndOfMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return getDateOnly(calendar.time)
    }

    /**
     * 获取本年的开始日期
     */
    fun getStartOfYear(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        return getDateOnly(calendar.time)
    }

    /**
     * 获取本年的结束日期
     */
    fun getEndOfYear(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        return getDateOnly(calendar.time)
    }

    /**
     * 获取指定日期所在周的日期列表（周一到周日）
     * @param date 指定日期，默认为当前日期
     * @return 日期列表
     */
    fun getDatesOfWeek(date: Date = Date()): List<Date> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val dates = mutableListOf<Date>()
        for (i in 0..6) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dates
    }

}
