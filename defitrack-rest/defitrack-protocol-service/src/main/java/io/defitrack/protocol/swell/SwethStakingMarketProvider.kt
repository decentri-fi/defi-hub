package io.defitrack.protocol.swell

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.SWELL)
class SwethStakingMarketProvider : FarmingMarketProvider() {

    val swethAddress = "0xf951e335afb289353dc249e82926178eac7ded78"

    val deferredContract = lazyAsync {
        SwethContract(getBlockchainGateway(), swethAddress)
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val ether = getToken("0x0")
        val contract = deferredContract.await()
        val rate = contract.rate.await()

        send(
            create(
                name = "swETH",
                identifier = swethAddress,
                stakedToken = ether.toFungibleToken(),
                rewardTokens = listOf(ether.toFungibleToken()),
                farmType = ContractType.STAKING,
                balanceFetcher = PositionFetcher(
                    swethAddress,
                    ::balanceOfFunction
                ) { result ->
                    val bal = result[0].value as BigInteger
                    if (bal > BigInteger.ZERO) {
                        Position(bal.times(rate).asEth().toBigInteger(), bal)
                    } else {
                        Position.ZERO
                    }
                }
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.SWELL
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}