package io.defitrack.market.pooling

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
@RequestMapping("/pooling")
class DefaultPoolingPositionRestController(
    private val poolingPositionProviders: List<PoolingPositionProvider>,
    private val poolingPositionVOMapper: PoolingPositionVOMapper,
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{userId}/positions")
    fun getUserPoolings(@PathVariable("userId") address: String): List<PoolingPositionVO> = runBlocking {
        poolingPositionProviders.map {
            async {
                try {
                    it.userPoolings(address)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    emptyList()
                }
            }
        }.awaitAll().flatMap {
            it.map {
                poolingPositionVOMapper.map(it)
            }
        }
    }
}