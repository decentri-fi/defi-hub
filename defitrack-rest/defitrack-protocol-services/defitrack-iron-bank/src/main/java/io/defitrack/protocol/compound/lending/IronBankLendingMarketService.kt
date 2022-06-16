package io.defitrack.protocol.compound.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.LendingMarketService
import io.defitrack.market.lending.domain.BalanceFetcher
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.IronBankComptrollerContract
import io.defitrack.protocol.compound.IronBankService
import io.defitrack.protocol.compound.IronbankTokenContract
import io.defitrack.protocol.compound.lending.invest.CompoundLendingInvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
abstract class IronBankLendingMarketService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val compoundEthereumService: IronBankService,
    private val priceResource: PriceResource
) : LendingMarketService() {

    val comptrollerABI by lazy {
        abiResource.getABI("compound/comptroller.json")
    }

    val cTokenABI by lazy {
        abiResource.getABI("compound/ctoken.json")
    }

    val gateway = blockchainGatewayProvider.getGateway(getNetwork())

    override suspend fun fetchLendingMarkets(): List<LendingMarket> =
        withContext(Dispatchers.IO.limitedParallelism(5)) {
            getTokenContracts().map {
                async {
                    toLendingMarket(it)
                }
            }.awaitAll().filterNotNull()
        }

    private suspend fun toLendingMarket(ctokenContract: IronbankTokenContract): LendingMarket? {
        return try {
            ctokenContract.underlyingAddress.let { tokenAddress ->
                erC20Resource.getTokenInformation(getNetwork(), tokenAddress)
            }.let { underlyingToken ->
                LendingMarket(
                    id = "ironbank-ethereum-${ctokenContract.address}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = ctokenContract.name,
                    rate = getSupplyRate(compoundTokenContract = ctokenContract),
                    address = ctokenContract.address,
                    token = underlyingToken.toFungibleToken(),
                    marketSize = priceResource.calculatePrice(
                        PriceRequest(
                            underlyingToken.address,
                            getNetwork(),
                            ctokenContract.cash.add(ctokenContract.totalBorrows).toBigDecimal().divide(
                                BigDecimal.TEN.pow(underlyingToken.decimals),
                                18,
                                RoundingMode.HALF_UP
                            ),
                            TokenType.SINGLE
                        )
                    ).toBigDecimal(),
                    poolType = "iron-bank-lendingpool",
                    balanceFetcher = BalanceFetcher(
                        ctokenContract.address,
                        { user -> ctokenContract.balanceOfMethod(user) },
                        { retVal ->
                            val tokenBalance = retVal[0].value as BigInteger
                            tokenBalance.times(ctokenContract.exchangeRate).asEth(18).toBigInteger()
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

    fun getSupplyRate(compoundTokenContract: IronbankTokenContract): BigDecimal {
        val blocksPerDay = 6463
        val dailyRate =
            (compoundTokenContract.supplyRatePerBlock.toBigDecimal().divide(BigDecimal.TEN.pow(18)) * BigDecimal(
                blocksPerDay
            )) + BigDecimal.ONE

        return dailyRate.pow(365).minus(BigDecimal.ONE).times(BigDecimal.TEN.pow(4))
            .divide(BigDecimal.TEN.pow(4), 4, RoundingMode.HALF_UP)
    }

    override fun getProtocol(): Protocol {
        return Protocol.IRON_BANK
    }

    private fun getTokenContracts(): List<IronbankTokenContract> {
        return getComptroller().getMarkets().map { market ->
            IronbankTokenContract(
                gateway,
                cTokenABI,
                market
            )
        }
    }

    private fun getComptroller(): IronBankComptrollerContract {
        return IronBankComptrollerContract(
            gateway,
            comptrollerABI,
            compoundEthereumService.getComptroller()
        )
    }
}