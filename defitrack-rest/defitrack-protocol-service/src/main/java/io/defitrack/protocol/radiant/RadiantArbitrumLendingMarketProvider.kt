package io.defitrack.protocol.radiant

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.protocol.compound.lending.invest.CompoundLendingInvestmentPreparer
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.RADIANT)
class RadiantArbitrumLendingMarketProvider : LendingMarketProvider() {


    override suspend fun produceMarkets(): Flow<LendingMarket> = channelFlow {
        val lendingPoolAddressesProviderContract = LendingPoolAddressProviderContract(
            getBlockchainGateway(),
            "0x091d52cace1edc5527c99cdcfa6937c1635330e4"
        )
        val contract = LendingPoolContract(
            getBlockchainGateway(),
            lendingPoolAddressesProviderContract.lendingPoolAddress()
        )

        contract.getReservesList()
            .parMapNotNull(concurrency = 8) { market ->
                createMarket(market, contract)
            }.forEach {
                send(it)
            }
    }

    private suspend fun createMarket(market: String, contract: LendingPoolContract): LendingMarket {
        val reserve = contract.getReserveData(market)
        val ctokenContract = CompoundTokenContract(
            getBlockchainGateway(),
            reserve.aTokenAddress
        )
        val lendingToken = getToken(market)
        return create(
            identifier = ctokenContract.address,
            name = ctokenContract.readName(),
            token = lendingToken,
            poolType = "compound-lendingpool",
            positionFetcher = PositionFetcher(
                ctokenContract::scaledBalanceOfFn,
            ),
            investmentPreparer = CompoundLendingInvestmentPreparer(
                ctokenContract,
                getERC20Resource()
            ),
            marketToken = getToken(ctokenContract.address),
            erc20Compatible = true,
            totalSupply = refreshable {
                with(getToken(ctokenContract.address)) {
                    totalDecimalSupply()
                }
            }
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.RADIANT
    }

    override fun getNetwork(): Network = Network.ARBITRUM
}