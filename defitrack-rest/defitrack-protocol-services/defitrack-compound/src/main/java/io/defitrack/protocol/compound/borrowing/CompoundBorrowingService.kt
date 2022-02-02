package io.defitrack.protocol.compound.borrowing

import io.defitrack.borrowing.BorrowService
import io.defitrack.borrowing.domain.BorrowElement
import io.defitrack.token.ERC20Resource
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
class CompoundBorrowingService(
    private val compoundService: CompoundService,
    private val abiResource: ABIResource,
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val erC20Service: ERC20Resource
) : io.defitrack.borrowing.BorrowService {

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

    override suspend fun getBorrows(address: String): List<io.defitrack.borrowing.domain.BorrowElement> {
        return getTokenContracts().mapNotNull {
            val underlying = it.underlyingAddress?.let { tokenAddress ->
                erC20Service.getERC20(getNetwork(), tokenAddress)
            }

            val balance = it.borrowBalanceStored(address)
            if (balance > BigInteger.ZERO) {
                io.defitrack.borrowing.domain.BorrowElement(
                    id = UUID.randomUUID().toString(),
                    user = address.lowercase(Locale.getDefault()),
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = it.name,
                    rate = getBorrowRate(it).toDouble(),
                    amount = balance.toBigDecimal().divide(
                        BigDecimal.TEN.pow(underlying?.decimals ?: 18), 2, RoundingMode.HALF_UP
                    ).toPlainString(),
                    symbol = underlying?.symbol ?: "ETH",
                    tokenUrl = "https://etherscan.io/address/${it.address}"
                )
            } else {
                null
            }
        }
    }

    override fun getProtocol(): Protocol = Protocol.COMPOUND

    override fun getNetwork(): Network = Network.ETHEREUM
}