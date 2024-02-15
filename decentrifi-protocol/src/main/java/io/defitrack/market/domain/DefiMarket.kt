package io.defitrack.market.domain

import arrow.core.Either
import arrow.core.Either.Companion.catch
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.refreshable
import io.defitrack.protocol.Protocol
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh
import java.time.Duration
import java.time.LocalDateTime
import kotlin.reflect.full.declaredMemberProperties

abstract class DefiMarket(
    open val id: String,
    val type: String,
    open val protocol: Protocol,
    open val network: Network,
    open val deprecated: Boolean,
    val updatedAt: Refreshable<LocalDateTime> = refreshable(LocalDateTime.now()) {
        LocalDateTime.now()
    },
    val refreshRate: Duration = Duration.ofMinutes(59)
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun refresh(): DefiMarket {
        this::class.declaredMemberProperties
            .filter {
                it.returnType.classifier == Refreshable::class
            }
            .map {
                it.getter.call(this) as Refreshable<*>
            }.forEach {
                catch {
                    it.refresh()
                }.mapLeft {
                    logger.error("Unable to refresh $id", it)
                }
            }
        return this
    }
}