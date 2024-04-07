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
        val simpleDateHourMinSecFormat =
            SimpleDateFormat(DEFAULT_DATE_HOUR_MIN_SEC_FORMAT, Locale.CHINA)

        /**
         * timeutil单例
         */
        @JvmStatic
        val instance: TimeUtils by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            TimeUtils()
        }
    }

    /**
     * 计算到目前的时间
     */
    @JvmOverloads
    fun getTimeIntervalOfCur(
        time: String,
        timeFormatter: DateFormat = SimpleDateFormat(
            DEFAULT_DATE_HOUR_MIN_SEC_FORMAT, // 使用你提供的默认格式
            Locale.getDefault(Locale.Category.FORMAT)
        )
    ): String {
        // 尝试解析传入的时间字符串
        val date = timeFormatter.parse(time, ParsePosition(0))
        // 如果解析成功，计算时间间隔并返回描述
        date?.let {
            val timeSecond: Long = it.time / 1000
            return getTimeIntervalOfCur(timeSecond)
        }
        // 如果解析失败，返回一个默认字符串或抛出异常
        return "时间格式错误" // 或考虑抛出一个异常，取决于你希望如何处理这种情况
    }


    /**
     * 计算到目前的时间
     */
    fun getTimeIntervalOfCur(time: Long): String {
        val curTime = System.currentTimeMillis() / 1000 // 获取当前时间的时间戳（秒）
        val timeInterval = curTime - time // 计算时间差（秒）

        return when {
            timeInterval < 300 -> "刚刚" // 假定300秒（5分钟）内为“刚刚”
            timeInterval < 60 * 60 -> "${timeInterval / 60}分钟前" // 少于1小时
            timeInterval < 60 * 60 * 24 -> "${timeInterval / (60 * 60)}小时前" // 少于1天
            timeInterval < 60 * 60 * 24 * 30 -> "${timeInterval / (60 * 60 * 24)}天前" // 少于1个月
            timeInterval < 60 * 60 * 24 * 365 -> "${timeInterval / (60 * 60 * 24 * 30)}个月前" // 少于1年
            else -> "${timeInterval / (60 * 60 * 24 * 365)}年前" // 超过1年
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
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, offsetValue)
        return calendar.time
    }


    /**
     * 时间偏移小时
     */
    @JvmOverloads
    fun offsetHour(offsetValue: Int, date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, offsetValue)
        return calendar.time
    }

    /**
     * 时间偏移分钟
     */
    @JvmOverloads
    fun offsetMin(offsetValue: Int, date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, offsetValue)
        return calendar.time
    }

    /**
     * 时间偏移秒
     */
    @JvmOverloads
    fun offsetSec(offsetValue: Int, date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.SECOND, offsetValue)
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
        return SimpleDateFormat(dateFormat, Locale.CHINA).format(now())
    }

    /**
     * 获取今天日期
     */
    fun today(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
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
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        return realDate.before(startOfToday)
    }


    /**
     * 判断日期是否在今天之前
     */
    fun isTimeBeforeToday(realDateStr: String): Boolean {
        val formatter = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
        val realDate = formatter.parse(realDateStr)
        val today = Calendar.getInstance()

        val realDateCal = Calendar.getInstance().apply {
            time = realDate ?: return false
        }

        return when {
            realDateCal[Calendar.YEAR] < today[Calendar.YEAR] -> true
            realDateCal[Calendar.YEAR] == today[Calendar.YEAR] && realDateCal[Calendar.DAY_OF_YEAR] < today[Calendar.DAY_OF_YEAR] -> true
            else -> {
                false
            }
        }
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
            LogUtils.logger.e("时间转换错误")
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
        val min = "0${timeMillis / 1000 / 60}"
        val secL = timeMillis / 1000 % 60
        var secS = ""
        if (secL < 10) {
            secS = "0${secL}"
        } else {
            secS = "$secL"
        }
        return "${min}:${secS}"
    }

    /**
     * 计算指定时间到现在的时间差
     */
    @SuppressLint("SimpleDateFormat")
    fun calcOffsetTime(sourceTime: String, timeDateFormat: String): Int {
        val deviceTime = now().time
        return ((SimpleDateFormat(timeDateFormat).parse(sourceTime)!!.time-deviceTime)/1000L).toInt()
    }
}