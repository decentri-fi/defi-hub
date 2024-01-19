package io.defitrack.market.adapter.`in`.rest

import io.defitrack.market.adapter.`in`.mapper.BorrowingPositionVOMapper
import io.defitrack.market.adapter.`in`.resource.BorrowPositionVO
import io.defitrack.market.port.`in`.BorrowPositions
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{protocol}/borrowing")
class DefaultBorrowingRestController(
    private val borrowPositions: BorrowPositions,
    private val borrowingPositionVOMapper: BorrowingPositionVOMapper
) {

    @GetMapping("/{userId}/positions")
    suspend fun getPositions(
        @PathVariable("protocol") protocol: String,
        @PathVariable("userId") address: String
    ): List<BorrowPositionVO> {
        return borrowPositions.getPositions(protocol, address)
            .map { borrowingPositionVOMapper.toVO(it) }
    }
}