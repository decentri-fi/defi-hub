package io.defitrack.protocol.compound.lending

import io.defitrack.common.network.Network
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundEthereumService
import io.defitrack.protocol.compound.v3.contract.CompoundV3AssetContract
import org.springframework.stereotype.Component

@Component
class CompoundV3LendingMarketProvider(
    private val compoundEthereumService: CompoundEthereumService
) : LendingMarketProvider() {
    override suspend fun fetchMarkets(): List<LendingMarket> {
        return compoundEthereumService.getV3Tokens().map {
            val assetContract = CompoundV3AssetContract(
                getBlockchainGateway(), it
            )

            return assetContract.getAssetInfos().map { assetInfo ->
                val lendingToken = getToken(assetInfo.asset)
                create(
                    identifier = "compoundv3-${lendingToken.symbol}",
                    name = "Compound V3 ${lendingToken.symbol}",
                    token = lendingToken.toFungibleToken(),
                    poolType = "compoundv3"
                )
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.COMPOUND
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}