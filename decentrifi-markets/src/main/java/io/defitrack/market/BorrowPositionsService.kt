package io.defitrack.market

import io.defitrack.market.domain.borrow.BorrowPosition
import io.defitrack.market.port.`in`.BorrowPositions
import io.defitrack.market.port.out.BorrowPositionProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BorrowPositionsService(
    private val borrowingServices: List<BorrowPositionProvider>
) : BorrowPositions {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getPositions(
        protocol: String,
        address: String
    ): List<BorrowPosition> {
        return borrowingServices
            .filter {
                it.getProtocol().slug == protocol
            }
            .flatMap {
                try {
                    it.getPositions(address)
                } catch (ex: Exception) {
                    logger.error("Something went wrong trying to fetch the user lendings: ${ex.message}")
                    emptyList()
                }
            }
    }
}