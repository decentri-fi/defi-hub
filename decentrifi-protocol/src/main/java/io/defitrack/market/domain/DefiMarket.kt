package io.defitrack.market.domain

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.refreshable
import io.defitrack.protocol.Protocol
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.reflect.full.declaredMemberProperties

abstract class DefiMarket(
    open val id: String,
    val type: String,
    open val protocol: Protocol,
    open val network: Network,
    open val deprecated: Boolean
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    val updatedAt = refreshable(LocalDateTime.now()) {
        LocalDateTime.now()
    }

    val refreshables = mutableListOf<Refreshable<*>>()

    init {
        this::class.declaredMemberProperties
            .filter {
                it.returnType.classifier == Refreshable::class
            }
            .forEach {
                val call = it.getter.call(this)
                if (call != null) {
                    addRefetchableValue(call as Refreshable<*>)
                }
            }
    }

    init {
        addRefetchableValue(updatedAt)
    }

    fun addRefetchableValue(refreshable: Refreshable<*>?) {
        if (refreshable != null) {
            refreshables.add(refreshable)
        }
    }

    suspend fun refresh(): DefiMarket {
        refreshables.forEach {
            try {
                it.refresh()
            } catch (ex: Exception) {
                logger.error("Unable to refresh $id", ex)
                ex.printStackTrace()
            }
        }
        return this
    }
}