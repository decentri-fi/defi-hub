package io.defitrack.protocol.compound.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarketElement
import io.defitrack.lending.domain.LendingToken
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundComptrollerContract
import io.defitrack.protocol.compound.CompoundService
import io.defitrack.protocol.compound.CompoundTokenContract
import io.defitrack.token.TokenType
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class CompoundLendingMarketService(
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val compoundService: CompoundService,
    private val priceResource: PriceResource
) : LendingMarketService() {

    val comptrollerABI by lazy {
        abiResource.getABI("compound/comptroller.json")
    }

    val cTokenABI by lazy {
        abiResource.getABI("compound/ctoken.json")
    }

    override suspend fun fetchLendingMarkets(): List<LendingMarketElement> {
        return getTokenContracts().mapNotNull {
            it.underlyingAddress?.let { tokenAddress ->
                erC20Resource.getERC20(getNetwork(), tokenAddress)
            }?.let { underlyingToken ->
                LendingMarketElement(
                    id = "compound-ethereum-${it.address}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = it.name,
                    rate = getSupplyRate(compoundTokenContract = it).toDouble(),
                    address = it.address,
                    token = LendingToken(
                        underlyingToken.name,
                        underlyingToken.symbol,
                        underlyingToken.address
                    ),
                    marketSize = priceResource.calculatePrice(
                        PriceRequest(
                            underlyingToken.address,
                            getNetwork(),
                            it.cash.add(it.totalBorrows).toBigDecimal().divide(
                                BigDecimal.TEN.pow(underlyingToken.decimals),
                                18,
                                RoundingMode.HALF_UP
                            ),
                            TokenType.SINGLE
                        )
                    ),
                    poolType = "compound-lendingpool"
                )
            }
        }
    }

    fun getSupplyRate(compoundTokenContract: CompoundTokenContract): BigDecimal {
        val blocksPerDay = 6463
        val dailyRate =
            (compoundTokenContract.supplyRatePerBlock.toBigDecimal().divide(BigDecimal.TEN.pow(18)) * BigDecimal(
                blocksPerDay
            )) + BigDecimal.ONE

        return dailyRate.pow(365).minus(BigDecimal.ONE).times(BigDecimal.TEN.pow(4))
            .divide(BigDecimal.TEN.pow(4), 4, RoundingMode.HALF_UP)
    }

    override fun getProtocol(): Protocol {
        return Protocol.COMPOUND;
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

    private fun getTokenContracts() = CompoundComptrollerContract(
        ethereumContractAccessor,
        comptrollerABI,
        compoundService.getComptroller()
    ).getMarkets().map {
        CompoundTokenContract(
            ethereumContractAccessor,
            cTokenABI,
            it
        )
    }
}