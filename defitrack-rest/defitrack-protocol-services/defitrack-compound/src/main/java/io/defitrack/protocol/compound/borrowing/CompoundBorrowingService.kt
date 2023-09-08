package io.defitrack.protocol.compound.borrowing

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.market.borrowing.BorrowService
import io.defitrack.market.borrowing.domain.BorrowPosition
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class CompoundBorrowingService(
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    blockchainGatewayProvider: BlockchainGatewayProvider,
) : BorrowService {

    val comptrollerABI by lazy {
        runBlocking { abiResource.getABI("compound/comptroller.json") }
    }

    val cTokenABI by lazy {
        runBlocking {
            abiResource.getABI("compound/ctoken.json")
        }
    }

    val gateway = blockchainGatewayProvider.getGateway(getNetwork())

    suspend fun getBorrowRate(compoundTokenContract: CompoundTokenContract): BigDecimal {
        val blocksPerDay = 6463
        val dailyRate =
            (compoundTokenContract.borrowRatePerBlock().toBigDecimal().divide(BigDecimal.TEN.pow(18)) * BigDecimal(
                blocksPerDay
            ))

        return dailyRate.times(BigDecimal(365)).times(BigDecimal.TEN.pow(4))
            .divide(BigDecimal.TEN.pow(4), 4, RoundingMode.HALF_UP).times(BigDecimal(100))
    }

    private suspend fun getTokenContracts() = CompoundComptrollerContract(
        gateway,
        comptrollerABI,
        CompoundAddressesProvider.CONFIG[getNetwork()]!!.v2Controller!!
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
                val underlying = compoundTokenContract.underlyingAddress().let { tokenAddress ->
                    erC20Resource.getTokenInformation(getNetwork(), tokenAddress)
                }
                val token = underlying.toFungibleToken()
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