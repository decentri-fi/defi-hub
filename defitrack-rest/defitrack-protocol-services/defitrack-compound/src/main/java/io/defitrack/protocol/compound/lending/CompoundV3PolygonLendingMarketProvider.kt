package io.defitrack.protocol.compound.lending

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v3.contract.CompoundV3AssetContract
import org.springframework.stereotype.Component

@Component
class CompoundV3PolygonLendingMarketProvider(
) : LendingMarketProvider() {
    override suspend fun fetchMarkets(): List<LendingMarket> {
        val compoundAddresses = CompoundAddressesProvider.CONFIG[getNetwork()] ?: return emptyList()

        return compoundAddresses.v3Tokens.flatMap { cTokenAddress ->
            val assetContract = CompoundV3AssetContract(
                getBlockchainGateway(), cTokenAddress
            )

            assetContract.getAssetInfos().map { assetInfo ->
                val lendingToken = getToken(assetInfo.asset)
                val cToken = getToken(cTokenAddress)
                create(
                    identifier = "compoundv3-${lendingToken.symbol}",
                    name = "Compound V3 ${lendingToken.symbol}",
                    token = lendingToken.toFungibleToken(),
                    poolType = "compoundv3",
                    marketToken = cToken.toFungibleToken(),
                    erc20Compatible = true,
                    positionFetcher = PositionFetcher(
                        address = cTokenAddress,
                        function = { user ->
                            assetContract.collateralBalanceOfFunction(user, lendingToken.address)
                        },
                    ),
                    totalSupply = refreshable(cToken.totalSupply.asEth()) {
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
        return Network.POLYGON
    }
}