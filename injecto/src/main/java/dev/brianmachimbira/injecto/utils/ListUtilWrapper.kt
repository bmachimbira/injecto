package dev.brianmachimbira.injecto.utils

import java.lang.StringBuilder

class ListUtilWrapper<T>(private val list: List<T>) {
    fun toDelimitedString(delimiter: String?): String {
        var separator: String? = ""
        val sb = StringBuilder("")
        for (item in list) {
            sb.append(separator).append(item.toString())
            separator = delimiter
        }
        return sb.toString()
    }
}