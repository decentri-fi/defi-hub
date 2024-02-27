package io.defitrack.protocol.application.radiant

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.radiant.MasterChefContract
import io.defitrack.protocol.sushiswap.contract.MasterChefBasedContract
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.RADIANT)
@ConditionalOnNetwork(Network.ARBITRUM)
class RadiantMasterChefMarketProvider : FarmingMarketProvider() {

    val masterchefAddress = "0xc963ef7d977ecb0ab71d835c4cb1bf737f28d010"
    val rdntAddress = "0x0c4681e6c0235179ec3d4f4fc4df3d14fdd96017"

    override suspend fun produceMarkets(): Flow<FarmingMarket> {
        val reward = getToken(rdntAddress)
        val contract = MasterChefContract(
            getBlockchainGateway(), masterchefAddress
        )

        contract.getPoolInfo().mapIndexed { index, it ->

            val underlying = getToken(it.underlying)

            create(
                name = "MasterChef ${underlying.name}",
                identifier = "$masterchefAddress-$index",
                stakedToken = underlying,
                rewardToken = reward,
                type = "radiant.masterchef"
                //positionFetcher = defaultPositionFetcher(it.poolAddress)
            )

        }
        return super.produceMarkets()
    }

    override fun getProtocol(): Protocol {
        return Protocol.RADIANT
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}