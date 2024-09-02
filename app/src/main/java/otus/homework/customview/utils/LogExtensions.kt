package otus.homework.customview.utils

val Any.TAG: String
    get() {
        val tagMaxAllowedLimit = 23
        val tag = javaClass.simpleName
        return if (tag.length <= tagMaxAllowedLimit) tag else tag.substring(0, tagMaxAllowedLimit)
    }