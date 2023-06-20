package com.sik.sikcore.data

import android.view.Gravity
import org.junit.Test


internal class BeanUtilsTest {

    data class TempA(val name: String = "", val age: Int = 0)
    data class TempB(val name: String = "", val age: Int = 0, val birth: String = "1990-01-01")

    @Test
    fun copyData() {
        println(BeanUtils.copyData<TempA>(TempB("张三", 20, "1995-02-01"), TempA()))
        val tempA = TempA()
        BeanUtils.copyData<TempA>(TempB("李四", 20, "1995-02-01"), tempA)
        println(tempA)
    }
}