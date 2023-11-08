package io.defitrack.market.pooling

import arrow.fx.coroutines.parMap
import io.defitrack.market.pooling.mapper.PoolingPositionVOMapper
import io.defitrack.market.pooling.vo.PoolingPositionVO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{protocol}/pooling")
class DefaultPoolingPositionRestController(
    private val poolingPositionProviders: List<PoolingPositionProvider>,
    private val poolingPositionVOMapper: PoolingPositionVOMapper,
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{userId}/positions")
    suspend fun getUserPoolings(
        @PathVariable("protocol") protocol: String,
        @PathVariable("userId") address: String
    ): List<PoolingPositionVO> {
        return poolingPositionProviders.parMap {
            try {
                it.userPoolings(protocol, address)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }
        }.flatMap {
            it.map {
                poolingPositionVOMapper.map(it)
            }
        }
    }
}