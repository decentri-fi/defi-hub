package io.defitrack.protocol.application.compound.lending

import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v3.contract.CompoundV3AssetContract
import org.springframework.beans.factory.annotation.Autowired

@ConditionalOnCompany(Company.COMPOUND)
abstract class CompoundV3LendingMarketProvider : LendingMarketProvider() {

    @Autowired
    private lateinit var compoundAddressesProvider: CompoundAddressesProvider

    override suspend fun fetchMarkets(): List<LendingMarket> {
        val v3Tokens = compoundAddressesProvider.getV3Tokens(getNetwork())

        return v3Tokens.flatMap { cTokenAddress ->
            val assetContract = getAssetContract(cTokenAddress)

            assetContract.getAssetInfos().map { assetInfo ->
                val lendingToken = getToken(assetInfo.asset)
                val cToken = getToken(cTokenAddress)
                create(
                    identifier = "compoundv3-${lendingToken.symbol}",
                    name = "Compound V3 ${lendingToken.symbol}",
                    token = lendingToken,
                    marketToken = cToken,
                    erc20Compatible = true,
                    poolType = "compound.v3.lending",
                    totalSupply = refreshable {
                        getToken(cTokenAddress).totalDecimalSupply()
                    }
                )
            }
        }
    }

    private fun getAssetContract(cTokenAddress: String): CompoundV3AssetContract = with(getBlockchainGateway()) {
        return CompoundV3AssetContract(cTokenAddress)
    }

    override fun getProtocol(): Protocol {
        return Protocol.COMPOUND
    }
}