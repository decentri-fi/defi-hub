package io.defitrack.protocol.dinoswap.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.market.farming.FarmingPositionProvider
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DinoswapFarmingPositionProvider(
    private val dinoswapStakingMarketService: DinoswapFarmingMarketProvider,
    erC20Resource: ERC20Resource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : FarmingPositionProvider(erC20Resource) {


    override suspend fun getStakings(address: String): List<FarmingPosition> {
        val markets = dinoswapStakingMarketService.getStakingMarkets()

        return blockchainGatewayProvider.getGateway(getNetwork()).readMultiCall(
            markets.map {
                it.balanceFetcher!!.toMulticall(address)
            }
        ).mapIndexed { index, retVal ->
            val market = markets[index]
            val bal = market.balanceFetcher!!.extractBalance(retVal)
            if (bal > BigInteger.ONE) {
                FarmingPosition(
                    market,
                    bal
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}