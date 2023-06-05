package com.sik.sikcore.eventbus

import com.sik.sikcore.eventbus.BusModel

/**
 * BusModel data类型为object
 */
class DefaultBusModel(type: String, code: Int) : BusModel<Any>(type, code) {
}