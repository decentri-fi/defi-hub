package io.defitrack.protocol.mstable

import io.codechef.defitrack.lending.LendingService
import io.codechef.defitrack.lending.domain.LendingElement
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*

@Service
class MStableEthereumLendingService(
    private val mStableService: MStableEthereumService,
    private val abiResource: ABIResource,
    private val ethereumContractAccessor: EthereumContractAccessor,
) : LendingService {

    val savingsContractABI by lazy {
        abiResource.getABI("mStable/SavingsContract.json")
    }

    override fun getProtocol(): Protocol {
        return Protocol.MSTABLE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

    override fun getLendings(address: String): List<LendingElement> =
        mStableService.getSavingsContracts().map {
            MStableEthereumSavingsContract(
                ethereumContractAccessor,
                savingsContractABI,
                it
            )
        }.mapNotNull {
            val balance = it.balanceOf(address)
            if (balance > BigInteger.ZERO) {
                LendingElement(
                    user = address,
                    id = UUID.randomUUID().toString(),
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = it.name,
                    amount = balance.toBigDecimal().divide(
                        BigDecimal.TEN.pow(it.decimals), 6, RoundingMode.HALF_UP
                    ).toPlainString(),
                    symbol = it.symbol,
                )
            } else {
                null
            }
        }
}