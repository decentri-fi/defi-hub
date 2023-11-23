package io.defitrack.protocol.compound.borrowing

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.borrowing.BorrowService
import io.defitrack.market.borrowing.domain.BorrowPosition
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v2.contract.CompoundComptrollerContract
import io.defitrack.protocol.compound.v2.contract.CompoundTokenContract
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundBorrowingService(
    private val erC20Resource: ERC20Resource,
    blockchainGatewayProvider: BlockchainGatewayProvider,
) : BorrowService {

    val gateway = blockchainGatewayProvider.getGateway(getNetwork())

    suspend fun getBorrowRate(compoundTokenContract: CompoundTokenContract): BigDecimal {
        val blocksPerDay = 6463
        val dailyRate =
            (compoundTokenContract.borrowRatePerBlock.await().toBigDecimal()
                .divide(BigDecimal.TEN.pow(18)) * BigDecimal(
                blocksPerDay
            ))

        return dailyRate.times(BigDecimal(365)).times(BigDecimal.TEN.pow(4))
            .divide(BigDecimal.TEN.pow(4), 4, RoundingMode.HALF_UP).times(BigDecimal(100))
    }

    private suspend fun getTokenContracts() = CompoundComptrollerContract(
        gateway,
        CompoundAddressesProvider.CONFIG[getNetwork()]!!.v2Controller!!
    ).getMarkets().map {
        CompoundTokenContract(
            gateway,
            it
        )
    }

    override suspend fun getBorrows(address: String): List<BorrowPosition> {
        val tokenContracts = getTokenContracts()
        return gateway.readMultiCall(
            tokenContracts.map {
                it.borrowBalanceStoredFunction(address)
            }
        ).mapIndexed { index, retVal ->
            val balance = retVal.data[0].value as BigInteger
            if (balance > BigInteger.ZERO) {
                val compoundTokenContract = tokenContracts[index]
                val underlying = compoundTokenContract.getUnderlyingAddress().let { tokenAddress ->
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