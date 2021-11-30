package io.codechef.defitrack.balance.l1

import io.codechef.bsc.BscGateway
import io.codechef.common.network.Network
import io.codechef.defitrack.balance.BalanceService
import io.codechef.defitrack.balance.TokenBalance
import org.springframework.stereotype.Service
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class BscBalanceService(private val bscGateway: BscGateway) : BalanceService {

    override fun getNetwork(): Network = Network.BSC

    override fun getNativeBalance(address: String): BigDecimal =
        bscGateway.web3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
            .toBigDecimal().divide(
                BigDecimal.TEN.pow(18), 2, RoundingMode.HALF_UP
            )

    override fun getTokenBalances(user: String): List<TokenBalance> {
        return emptyList()
    }

    override fun nativeTokenName(): String {
        return "BNB"
    }
}