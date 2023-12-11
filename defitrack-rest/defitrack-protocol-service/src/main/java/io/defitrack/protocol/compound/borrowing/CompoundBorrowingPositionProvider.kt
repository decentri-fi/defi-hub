package io.defitrack.protocol.compound.borrowing

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.borrowing.BorrowPositionProvider
import io.defitrack.market.borrowing.domain.BorrowMarket
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
class CompoundBorrowingPositionProvider(
    private val erC20Resource: ERC20Resource,
    blockchainGatewayProvider: BlockchainGatewayProvider,
) : BorrowPositionProvider {

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

    override suspend fun getPositions(address: String): List<BorrowPosition> {
        val tokenContracts = getTokenContracts()
        return gateway.readMultiCall(
            tokenContracts.map {
                it.borrowBalanceStoredFunction(address)
            }
        ).mapIndexed { index, retVal ->
            //Todo: errors
            val balance = retVal.data[0].value as BigInteger
            if (balance > BigInteger.ZERO) {
                val compoundTokenContract = tokenContracts[index]
                val underlying = compoundTokenContract.getUnderlyingAddress().let { tokenAddress ->
                    erC20Resource.getTokenInformation(getNetwork(), tokenAddress)
                }
                val token = underlying
                BorrowPosition(
                    market = BorrowMarket(
                        id = "compound-${token.address}",
                        protocol = getProtocol(),
                        network = getNetwork(),
                        rate = getBorrowRate(compoundTokenContract),
                        deprecated = false,
                        name = "Compound ${token.name} Borrow",
                        token = token
                    ),
                    tokenAmount = balance,
                    underlyingAmount = balance,
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol = Protocol.COMPOUND

    override fun getNetwork(): Network = Network.ETHEREUM
}