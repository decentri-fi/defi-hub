package io.defitrack.protocol.compound.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessorConfig
import io.defitrack.evm.contract.ContractAccessorGateway
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
    private val contractAccessorGateway: ContractAccessorGateway,
    private val erC20Service: ERC20Resource
) : LendingService {

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

    override suspend fun getLendings(address: String): List<LendingElement> {
        val compoundTokenContracts = getTokenContracts()

        return erC20Service.getBalancesFor(address, compoundTokenContracts.map { it.address }, gateway)
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val tokenContract = compoundTokenContracts[index]
                    val underlyingBalance = tokenContract.underlyingBalanceOf(address)
                    val underlying = tokenContract.underlyingAddress?.let { tokenAddress ->
                        erC20Service.getTokenInformation(getNetwork(), tokenAddress)
                    }
                    val token = underlying?.toFungibleToken() ?: erC20Service.getTokenInformation(getNetwork(), "0x0")
                        .toFungibleToken()
                    LendingElement(
                        id = "compound-ethereum-${tokenContract.address}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        name = token.name,
                        rate = getSupplyRate(compoundTokenContract = tokenContract).toDouble(),
                        amount = underlyingBalance,
                        token = token
                    )
                } else {
                    null
                }
            }.filterNotNull()
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

    override fun getProtocol(): Protocol = Protocol.COMPOUND

    override fun getNetwork(): Network = Network.ETHEREUM
}