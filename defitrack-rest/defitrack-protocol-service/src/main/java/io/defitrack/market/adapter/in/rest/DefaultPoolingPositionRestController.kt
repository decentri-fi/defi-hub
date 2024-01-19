package io.defitrack.market.adapter.`in`.rest

import arrow.fx.coroutines.parMap
import io.defitrack.market.port.out.PoolingPositionProvider
import io.defitrack.market.pooling.mapper.PoolingPositionVOMapper
import io.defitrack.market.pooling.vo.PoolingPositionVO
import io.defitrack.market.port.`in`.PoolingPositions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{protocol}/pooling")
class DefaultPoolingPositionRestController(
    private val poolingPositions: PoolingPositions,
    private val poolingPositionVOMapper: PoolingPositionVOMapper,
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{userId}/positions")
    suspend fun getUserPoolings(
        @PathVariable("protocol") protocol: String,
        @PathVariable("userId") address: String
    ): List<PoolingPositionVO> {
        return poolingPositions.getUserPoolings(protocol, address).map {
            poolingPositionVOMapper.map(it)
        }
    }
}