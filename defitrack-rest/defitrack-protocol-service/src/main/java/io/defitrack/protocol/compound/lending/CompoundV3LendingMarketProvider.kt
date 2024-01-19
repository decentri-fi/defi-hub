package io.defitrack.protocol.compound.lending

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
}