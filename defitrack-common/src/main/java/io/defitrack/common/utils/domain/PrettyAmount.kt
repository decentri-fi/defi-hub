package io.defitrack.common.utils.domain

import java.text.DecimalFormat

class PrettyAmount(
    val value: String? = null,
    val unit: String? = null
) {

    companion object {
        val decimalFormatter = DecimalFormat("#0.0000")
    }

    private val isRounded: Boolean by lazy {
        value != null && value != format()
    }

    private fun format(): String {
        return try {
            decimalFormatter.format(java.lang.Double.valueOf(value))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

    override fun toString(): String {
        val returnValue = StringBuilder()
        if (isRounded) {
            returnValue.append("~")
        }
        return returnValue.append(format()).append(" ").append(unit).toString()
    }
}