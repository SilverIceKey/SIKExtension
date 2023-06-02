package com.sk.skextension.utils.date

import org.junit.Assert.*

import org.junit.Test
import java.util.*

class TimeUtilTest {

    @Test
    fun isToday() {
        assert(TimeUtil.instance.isToday(TimeUtil.simpleDateDayFormat.format(Date()),"yyyy-MM-dd"))
        assert(!TimeUtil.instance.isToday("2021-01-01","yyyy-MM-dd"))
    }

    @Test
    fun offsetDay() {
        val dateTime = Date(Date().time.minus(1000.times(60).times(60).times(24)))
        val dateDay = TimeUtil.simpleDateDayFormat.format(dateTime)
        val dateDayTime = TimeUtil.simpleDateDayFormat.parse(dateDay)?.time
        println(TimeUtil.instance.offsetDay(-1,Date()).time)
        println(dateDayTime)
        assert(TimeUtil.instance.offsetDay(-1,Date()).time==dateDayTime)
    }

    @Test
    fun offsetHour() {
        val dateTime = Date(Date().time.minus(1000.times(60).times(60).times(1)))
        val dateDayHour = TimeUtil.simpleDateHourFormat.format(dateTime)
        val dateDayHourTime = TimeUtil.simpleDateHourFormat.parse(dateDayHour)?.time
        println(TimeUtil.instance.offsetHour(-1,Date()).time)
        println(dateDayHourTime)
        assert(TimeUtil.instance.offsetHour(-1,Date()).time==dateDayHourTime)
    }

    @Test
    fun offsetMin() {
        val dateTime = Date(Date().time.minus(1000.times(60).times(1)))
        val dateDayMin = TimeUtil.simpleDateHourMinFormat.format(dateTime)
        val dateDayMinTime = TimeUtil.simpleDateHourMinFormat.parse(dateDayMin)?.time
        println(TimeUtil.instance.offsetMin(-1,Date()).time)
        println(dateDayMinTime)
        assert(TimeUtil.instance.offsetMin(-1,Date()).time==dateDayMinTime)
    }

    @Test
    fun now() {
        assert(TimeUtil.instance.now().time==Date().time)
    }

    @Test
    fun nowString() {
        assert(TimeUtil.instance.nowString().equals(TimeUtil.simpleDateDayFormat.format(Date())))
    }

    @Test
    fun today() {
        val dateDay = TimeUtil.simpleDateDayFormat.format(Date())
        assert(TimeUtil.instance.today().time==TimeUtil.simpleDateDayFormat.parse(dateDay)?.time)
    }
}