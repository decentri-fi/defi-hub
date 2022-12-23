package io.defitrack.protocol.compound.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundComptrollerContract
import io.defitrack.protocol.compound.CompoundEthereumService
import io.defitrack.protocol.compound.CompoundTokenContract
import io.defitrack.protocol.compound.lending.invest.CompoundLendingInvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
class CompoundLendingMarketProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val compoundEthereumService: CompoundEthereumService,
    private val priceResource: PriceResource
) : LendingMarketProvider() {

    val comptrollerABI by lazy {
        abiResource.getABI("compound/comptroller.json")
    }

    val cTokenABI by lazy {
        abiResource.getABI("compound/ctoken.json")
    }

    val gateway = blockchainGatewayProvider.getGateway(getNetwork())

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        getTokenContracts().map {
            async {
                toLendingMarket(it)
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun toLendingMarket(ctokenContract: CompoundTokenContract): LendingMarket? {
        return try {
            ctokenContract.underlyingAddress().let { tokenAddress ->
                erC20Resource.getTokenInformation(getNetwork(), tokenAddress)
            }.let { underlyingToken ->
                val exchangeRate = ctokenContract.exchangeRate()
                create(
                    identifier = ctokenContract.address,
                    name = ctokenContract.name(),
                    rate = getSupplyRate(compoundTokenContract = ctokenContract),
                    token = underlyingToken.toFungibleToken(),
                    marketSize = priceResource.calculatePrice(
                        PriceRequest(
                            underlyingToken.address,
                            getNetwork(),
                            ctokenContract.cash().add(ctokenContract.totalBorrows()).toBigDecimal().dividePrecisely(
                                BigDecimal.TEN.pow(underlyingToken.decimals)
                            ),
                            TokenType.SINGLE
                        )
                    ).toBigDecimal(),
                    poolType = "compound-lendingpool",
                    positionFetcher = PositionFetcher(
                        ctokenContract.address,
                        { user -> ctokenContract.balanceOfMethod(user) },
                        { retVal ->
                            val tokenBalance = retVal[0].value as BigInteger
                            tokenBalance.times(exchangeRate).asEth().toBigInteger()
                        }
                    ),
                    investmentPreparer = CompoundLendingInvestmentPreparer(
                        ctokenContract,
                        erC20Resource
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    suspend fun getSupplyRate(compoundTokenContract: CompoundTokenContract): BigDecimal {
        val blocksPerDay = 6463
        val dailyRate =
            (compoundTokenContract.supplyRatePerBlock().toBigDecimal().divide(BigDecimal.TEN.pow(18)) * BigDecimal(
                blocksPerDay
            )) + BigDecimal.ONE

        return dailyRate.pow(365).minus(BigDecimal.ONE).times(BigDecimal.TEN.pow(4))
            .divide(BigDecimal.TEN.pow(4), 4, RoundingMode.HALF_UP)
    }

    override fun getProtocol(): Protocol {
        return Protocol.COMPOUND
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

    private suspend fun getTokenContracts(): List<CompoundTokenContract> {
        return getComptroller().getMarkets().map { market ->
            CompoundTokenContract(
                gateway,
                cTokenABI,
                market
            )
        }
    }

    private fun getComptroller(): CompoundComptrollerContract {
        return CompoundComptrollerContract(
            gateway,
            comptrollerABI,
            compoundEthereumService.getComptroller()
        )
    }
}