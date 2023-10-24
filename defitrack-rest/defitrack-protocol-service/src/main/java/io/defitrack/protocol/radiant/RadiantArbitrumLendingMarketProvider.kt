package io.defitrack.protocol.radiant

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.protocol.compound.lending.invest.CompoundLendingInvestmentPreparer
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.RADIANT)
class RadiantArbitrumLendingMarketProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
) : LendingMarketProvider() {

    val lendingPoolAddressesProviderContract = lazyAsync {
        LendingPoolAddressProviderContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            "0x091d52cace1edc5527c99cdcfa6937c1635330e4"
        )
    }

    val lendingPoolContract = lazyAsync {
        LendingPoolContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            lendingPoolAddressesProviderContract.await().lendingPoolAddress()
        )
    }

    override suspend fun produceMarkets(): Flow<LendingMarket> = channelFlow {
        lendingPoolContract.await().getReservesList()
            .forEach { market ->
                launch {
                    throttled {
                        try {
                            val reserve = lendingPoolContract.await().getReserveData(market)
                            val ctokenContract = CompoundTokenContract(
                                getBlockchainGateway(),
                                reserve.aTokenAddress
                            )
                            val lendingToken = getToken(market)
                            send(
                                create(
                                    identifier = ctokenContract.address,
                                    name = ctokenContract.readName(),
                                    token = lendingToken.toFungibleToken(),
                                    poolType = "compound-lendingpool",
                                    positionFetcher = PositionFetcher(
                                        ctokenContract.address,
                                        { user -> ctokenContract.scaledBalanceOfFn(user) },
                                    ),
                                    investmentPreparer = CompoundLendingInvestmentPreparer(
                                        ctokenContract,
                                        getERC20Resource()
                                    ),
                                    marketToken = getToken(ctokenContract.address).toFungibleToken(),
                                    erc20Compatible = true,
                                    totalSupply = refreshable {
                                        with(getToken(ctokenContract.address)) {
                                            totalSupply.asEth(decimals)
                                        }
                                    }
                                )
                            )
                        } catch (ex: Exception) {
                            logger.error("Unable to fetch lending market with address $market", ex)
                        }
                    }
                }
            }
    }

    override fun getProtocol(): Protocol {
        return Protocol.RADIANT
    }

    override fun getNetwork(): Network = Network.ARBITRUM
}