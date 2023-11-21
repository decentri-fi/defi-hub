package io.defitrack.protocol.curve.lending

import arrow.core.Either
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.stablecoin.CurveControllerFactoryContract
import io.defitrack.protocol.crv.contract.stablecoin.LendingControllerContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import java.math.BigDecimal

@ConditionalOnCompany(Company.CURVE)
@Component
class CurveStablecoinLendingMarketProvider : LendingMarketProvider() {

    val factoryAddress = "0xC9332fdCB1C491Dcc683bAe86Fe3cb70360738BC"

    override suspend fun produceMarkets(): Flow<LendingMarket> = channelFlow {
        val factory = CurveControllerFactoryContract(
            getBlockchainGateway(),
            factoryAddress
        )

        factory.controllers().parMapNotNull { controller ->
            Either.catch {
                createMarket(controller)
            }.fold(
                {
                    logger.error("Failed to create market for $controller", it)
                    null
                },
                { it }
            )
        }.forEach {
            send(it)
        }
    }

    suspend fun createMarket(address: String): LendingMarket {
        val controller = LendingControllerContract(getBlockchainGateway(), address)
        val collateral = getToken(controller.collateral.await())

        return create(
            identifier = controller.address,
            name = collateral.name + " Lending",
            poolType = "crv",
            marketToken = null,
            token = collateral,
            totalSupply = refreshable { BigDecimal.ZERO }
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}