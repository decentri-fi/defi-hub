package io.defitrack.protocol.application.morpho

import arrow.core.Either
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.morpho.MorphoContract
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.MORPHO)
@Component
class MorphoAaveV3LendingMarketProvider : LendingMarketProvider() {

    val morphoAddress = "0x33333aea097c193e66081e930c33020272b33333"


    override suspend fun fetchMarkets(): List<LendingMarket> {
        val contract = MorphoContract(getBlockchainGateway(), morphoAddress)
        return contract.marketsCreated().mapNotNull { market ->
            Either.catch {
                val underlying = getToken(market)
                create(
                    identifier = market,
                    name = "${underlying.name} morpho aave v3",
                    token = underlying,
                    poolType = "morpho aave v3",
                    positionFetcher = PositionFetcher(contract.collateralBalance(market))
                )
            }.mapLeft {
                logger.error("Error fetching market $market", it)
            }.getOrNull()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.MORPHO_AAVE_V3
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}