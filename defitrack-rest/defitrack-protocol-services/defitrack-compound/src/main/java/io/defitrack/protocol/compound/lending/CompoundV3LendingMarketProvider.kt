package io.defitrack.protocol.compound.lending

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.RefetchableValue.Companion.refetchable
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
        return compoundEthereumService.getV3Tokens().map { cTokenAddress ->
            val assetContract = CompoundV3AssetContract(
                getBlockchainGateway(), cTokenAddress
            )

            return assetContract.getAssetInfos().map { assetInfo ->
                val lendingToken = getToken(assetInfo.asset)
                val cToken = getToken(cTokenAddress)
                create(
                    identifier = "compoundv3-${lendingToken.symbol}",
                    name = "Compound V3 ${lendingToken.symbol}",
                    token = lendingToken.toFungibleToken(),
                    poolType = "compoundv3",
                    marketToken = cToken.toFungibleToken(),
                    erc20Compatible = true,
                    totalSupply = refetchable(cToken.totalSupply.asEth()) {
                        val cToken = getToken(cTokenAddress)
                        cToken.totalSupply.asEth()
                    }
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