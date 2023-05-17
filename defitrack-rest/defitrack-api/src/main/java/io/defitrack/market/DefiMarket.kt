package io.defitrack.market

import org.slf4j.LoggerFactory
import java.time.LocalDateTime

abstract class DefiMarket(
    open val id: String
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    val updatedAt = RefetchableValue.refetchable {
        LocalDateTime.now()
    }

    val refetchableValues = mutableListOf<RefetchableValue<*>>()

    init {
        addRefetchableValue(updatedAt)
    }

    fun addRefetchableValue(refetchableValue: RefetchableValue<*>?) {
        if (refetchableValue != null) {
            refetchableValues.add(refetchableValue)
        }
    }

    suspend fun refresh() {
        refetchableValues.forEach {
            try {
                it.refresh()
            } catch (ex: Exception) {
                logger.error("Unable to refresh ${id}", ex)
                ex.printStackTrace()
            }
        }
    }
}