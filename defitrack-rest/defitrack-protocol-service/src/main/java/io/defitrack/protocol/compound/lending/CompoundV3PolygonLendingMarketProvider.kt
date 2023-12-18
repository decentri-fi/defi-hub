package io.defitrack.protocol.compound.lending

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v3.contract.CompoundV3AssetContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundV3PolygonLendingMarketProvidero : LendingMarketProvider() {
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
                    token = lendingToken,
                    poolType = "compoundv3",
                    marketToken = cToken,
                    erc20Compatible = true,
                    positionFetcher = PositionFetcher(
                        cToken.asERC20Contract(getBlockchainGateway())::balanceOfFunction
                    ),
                    totalSupply = refreshable {
                        getToken(cTokenAddress).totalDecimalSupply()
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