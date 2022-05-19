package io.defitrack.protocol.compound.borrowing

import io.defitrack.abi.ABIResource
import io.defitrack.borrowing.domain.BorrowPosition
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.evm.contract.multicall.MultiCallElement
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
class CompoundBorrowingService(
    private val compoundService: CompoundService,
    private val abiResource: ABIResource,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val erC20Service: ERC20Resource
) : io.defitrack.borrowing.BorrowService {

    val comptrollerABI by lazy {
        abiResource.getABI("compound/comptroller.json")
    }

    val cTokenABI by lazy {
        abiResource.getABI("compound/ctoken.json")
    }

    val gateway = contractAccessorGateway.getGateway(getNetwork())

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
        gateway,
        comptrollerABI,
        compoundService.getComptroller()
    ).getMarkets().map {
        CompoundTokenContract(
            gateway,
            cTokenABI,
            it
        )
    }

    override suspend fun getBorrows(address: String): List<BorrowPosition> {
        val tokenContracts = getTokenContracts()
        return gateway.readMultiCall(
            tokenContracts.map {
                MultiCallElement(
                    it.borrowBalanceStoredFunction(address),
                    it.address
                )
            }
        ).mapIndexed { index, retVal ->
            val balance = retVal[0].value as BigInteger
            if (balance > BigInteger.ZERO) {
                val compoundTokenContract = tokenContracts[index]
                val underlying = compoundTokenContract.underlyingAddress?.let { tokenAddress ->
                    erC20Service.getTokenInformation(getNetwork(), tokenAddress)
                }
                val token = underlying?.toFungibleToken() ?: erC20Service.getTokenInformation(getNetwork(), "0x0")
                    .toFungibleToken()
                BorrowPosition(
                    id = "compound-ethereum-${token.address}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = token.name,
                    rate = getBorrowRate(compoundTokenContract).toDouble(),
                    amount = balance,
                    token = token
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol = Protocol.COMPOUND

    override fun getNetwork(): Network = Network.ETHEREUM
}