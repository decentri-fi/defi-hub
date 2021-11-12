package io.codechef.price

import java.math.BigDecimal

interface ExternalPriceService {
    fun getOracleName(): String
    fun getPrice(): BigDecimal

    fun appliesTo(name: String): Boolean {
        return getOracleName() == name.lowercase()
    }
}