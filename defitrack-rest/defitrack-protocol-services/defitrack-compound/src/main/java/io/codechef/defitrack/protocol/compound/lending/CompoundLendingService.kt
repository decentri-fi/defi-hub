package io.codechef.defitrack.protocol.compound.lending

import io.codechef.defitrack.lending.LendingService
import io.codechef.defitrack.lending.domain.LendingElement
import io.codechef.defitrack.token.ERC20Resource
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundComptrollerContract
import io.defitrack.protocol.compound.CompoundService
import io.defitrack.protocol.compound.CompoundTokenContract
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*

@Service
class CompoundLendingService(
    private val compoundContractProvider: CompoundService,
    private val abiResource: ABIResource,
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val erC20Service: ERC20Resource
) : LendingService {

    val comptrollerABI by lazy {
        abiResource.getABI("compound/comptroller.json")
    }

    val cTokenABI by lazy {
        abiResource.getABI("compound/ctoken.json")
    }

    fun getBorrowRate(compoundTokenContract: CompoundTokenContract): BigDecimal {
        val blocksPerDay = 6463
        val dailyRate =
            (compoundTokenContract.borrowRatePerBlock.toBigDecimal().divide(BigDecimal.TEN.pow(18)) * BigDecimal(
                blocksPerDay
            ))

        return dailyRate.times(BigDecimal(365)).times(BigDecimal.TEN.pow(4))
            .divide(BigDecimal.TEN.pow(4), 4, RoundingMode.HALF_UP).times(BigDecimal(100))
    }

    fun getSupplyRate(compoundTokenContract: CompoundTokenContract): BigDecimal {
        val blocksPerDay = 6463
        val dailyRate =
            (compoundTokenContract.supplyRatePerBlock.toBigDecimal().divide(BigDecimal.TEN.pow(18)) * BigDecimal(
                blocksPerDay
            )) + BigDecimal.ONE

        return dailyRate.pow(365).minus(BigDecimal.ONE).times(BigDecimal.TEN.pow(4))
            .divide(BigDecimal.TEN.pow(4), 4, RoundingMode.HALF_UP).times(BigDecimal(100))
    }

    override fun getLendings(address: String): List<LendingElement> {
        val compoundTokenContracts = getTokenContracts()
        return compoundTokenContracts.mapNotNull {
            val cTokenBalance = it.balanceOf(address)
            if (cTokenBalance > BigInteger.ZERO) {

                val underlyingBalance = it.underlyingBalanceOf(address)
                val underlying = it.underlyingAddress?.let { tokenAddress ->
                    erC20Service.getERC20(getNetwork(), tokenAddress)
                }

                LendingElement(
                    id = UUID.randomUUID().toString(),
                    user = address.lowercase(),
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = it.name,
                    rate = getSupplyRate(compoundTokenContract = it).toDouble(),
                    amount = underlyingBalance.divide(
                        BigDecimal.TEN.pow(underlying?.decimals ?: 18), 4, RoundingMode.HALF_UP
                    ).toPlainString(),
                    symbol = underlying?.symbol ?: "ETH",
                )
            } else {
                null
            }
        }
    }

    private fun getTokenContracts() = CompoundComptrollerContract(
        ethereumContractAccessor,
        comptrollerABI,
        compoundContractProvider.getComptroller()
    ).getMarkets().map {
        CompoundTokenContract(
            ethereumContractAccessor,
            cTokenABI,
            it
        )
    }

    override fun getProtocol(): Protocol = Protocol.COMPOUND

    override fun getNetwork(): Network = Network.ETHEREUM
}