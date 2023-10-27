package io.defitrack.protocol.camelot.farming

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.camelot.NftPoolContract
import io.defitrack.protocol.camelot.PoolFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CAMELOT)
class CamelotNftStakingProvider : FarmingMarketProvider() {

    val nftPoolFactoryAddress = "0x6db1ef0df42e30acf139a70c1ed0b7e6c51dbf6d"
    val grailAddress = "0x3d9907f9a368ad0a51be60f7da3b97cf940982d8"

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val grail = getToken(grailAddress)
        val factory = PoolFactoryContract(getBlockchainGateway(), nftPoolFactoryAddress)

        factory.getStakingPools().parMap(concurrency = 12) { poolAddress ->

            val poolContract = NftPoolContract(getBlockchainGateway(), poolAddress)

            val token = getToken(poolAddress)
            val staked = getToken(poolContract.getLpToken())

            create(
                name = token.name + " staking rewards",
                identifier = poolAddress,
                rewardToken = grail,
                stakedToken = staked,
            )
        }.forEach {
            send(it)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CAMELOT
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}