package io.defitrack.market

import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

abstract class DefiMarket(
    open val id: String
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    val updatedAt = refreshable(LocalDateTime.now()) {
        LocalDateTime.now()
    }

    val refreshables = mutableListOf<Refreshable<*>>()

    init {
        addRefetchableValue(updatedAt)
    }

    fun addRefetchableValue(refreshable: Refreshable<*>?) {
        if (refreshable != null) {
            refreshables.add(refreshable)
        }
    }

    suspend fun refresh() {
        refreshables.forEach {
            try {
                it.refresh()
            } catch (ex: Exception) {
                logger.error("Unable to refresh ${id}", ex)
                ex.printStackTrace()
            }
        }
    }
}