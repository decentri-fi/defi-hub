package io.defitrack.protocol.compound.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.lending.LendingService
import io.defitrack.lending.domain.LendingElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundComptrollerContract
import io.defitrack.protocol.compound.CompoundService
import io.defitrack.protocol.compound.CompoundTokenContract
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class CompoundLendingService(
    private val compoundService: CompoundService,
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

    override suspend fun getLendings(address: String): List<LendingElement> {
        val compoundTokenContracts = getTokenContracts()

        return erC20Service.getBalancesFor(address, compoundTokenContracts.map { it.address }, ethereumContractAccessor)
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val tokenContract = compoundTokenContracts[index]
                    val underlyingBalance = tokenContract.underlyingBalanceOf(address)
                    val underlying = tokenContract.underlyingAddress?.let { tokenAddress ->
                        erC20Service.getERC20(getNetwork(), tokenAddress)
                    }
                    LendingElement(
                        id = "compound-ethereum-${tokenContract.address}",
                        user = address.lowercase(),
                        network = getNetwork(),
                        protocol = getProtocol(),
                        name = tokenContract.name,
                        rate = getSupplyRate(compoundTokenContract = tokenContract).toDouble(),
                        amount = underlyingBalance.dividePrecisely(
                            BigDecimal.TEN.pow(underlying?.decimals ?: 18)
                        ).toPlainString(),
                        symbol = underlying?.symbol ?: "ETH",
                    )
                } else {
                    null
                }
            }.filterNotNull()
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

    override fun getProtocol(): Protocol = Protocol.COMPOUND

    override fun getNetwork(): Network = Network.ETHEREUM
}