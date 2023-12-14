package io.defitrack.protocol.curve.lending

import arrow.core.Either
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.stablecoin.CurveControllerFactoryContract
import io.defitrack.protocol.crv.contract.stablecoin.LendingControllerContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger

@ConditionalOnCompany(Company.CURVE)
@Component
class CurveStablecoinLendingMarketProvider : LendingMarketProvider() {

    val factoryAddress = "0xC9332fdCB1C491Dcc683bAe86Fe3cb70360738BC"

    override suspend fun produceMarkets(): Flow<LendingMarket> = channelFlow {
        val factory = CurveControllerFactoryContract(
            getBlockchainGateway(),
            factoryAddress
        )

        resolve(
            factory.controllers()
                .map {
                    LendingControllerContract(getBlockchainGateway(), it)
                }
        ).parMapNotNull { controller ->
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

    suspend fun createMarket(controller: LendingControllerContract): LendingMarket {
        val collateral = getToken(controller.collateral.await())

        return create(
            identifier = controller.address,
            name = collateral.name + " Lending",
            poolType = "crv",
            marketToken = null,
            token = collateral,
            totalSupply = refreshable { BigDecimal.ZERO },
            positionFetcher = PositionFetcher(
                controller::userState
            ) {
                val bal = (it.first().value as List<Uint256>).first().value as BigInteger
                if (bal > BigInteger.ZERO) {
                    Position(
                        bal,
                        bal
                    )
                } else {
                    Position.ZERO
                }
            }
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}