package com.zeks.jetbrains.task.stringsearch

import org.jetbrains.annotations.PropertyKey
import java.text.MessageFormat
import java.util.ResourceBundle

object TextSearchBundle {
    private val BUNDLE = ResourceBundle.getBundle("messages.TextSearchBundle")

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = "messages.TextSearchBundle") key: String, vararg params: Any): String =
        MessageFormat.format(BUNDLE.getString(key), *params)
}