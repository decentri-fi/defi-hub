package io.codechef.defitrack.balance.l1

import io.codechef.common.network.Network
import io.codechef.defitrack.abi.ABIResource
import io.codechef.defitrack.balance.BalanceService
import io.codechef.defitrack.balance.TokenBalance
import io.codechef.defitrack.erc20.ERC20Repository
import io.codechef.defitrack.token.ERC20Resource
import io.codechef.ethereum.config.AvalancheContractAccessor
import io.codechef.ethereum.config.AvalancheGateway
import io.codechef.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.codechef.ethereumbased.contract.multicall.MultiCallElement
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class AvaxBalanceService(
    private val abiResource: ABIResource,
    private val avalancheGateway: AvalancheGateway,
    private val erC20Repository: ERC20Repository,
    private val avalancheContractAccessor: AvalancheContractAccessor,
    private val erC20Service: ERC20Resource
) : BalanceService {

    val erc20ABI by lazy {
        abiResource.getABI("general/ERC20.json")
    }

    override fun getNetwork(): Network = Network.AVALANCHE

    override fun getNativeBalance(address: String): BigDecimal =
        avalancheGateway.web3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
            .toBigDecimal().divide(
                BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP
            )

    override fun getTokenBalances(user: String): List<TokenBalance> {
        val tokenAddresses = erC20Repository.allTokens(getNetwork()).map {
            it.address
        }

        return avalancheContractAccessor.readMultiCall(tokenAddresses.map { address ->
            MultiCallElement(
                avalancheContractAccessor.createFunction(
                    avalancheContractAccessor.getFunction(
                        erc20ABI, "balanceOf"
                    )!!,
                    listOf(user.toAddress()),
                    listOf(
                        TypeReference.create(Uint256::class.java)
                    )
                ),
                address
            )
        }).mapIndexed { i, result ->
            val balance = try {
                result[0].value as BigInteger
            } catch (ex: Exception) {
                BigInteger.ZERO
            }
            if (balance > BigInteger.ZERO) {
                val token = erC20Service.getERC20(getNetwork(), tokenAddresses[i])
                TokenBalance(
                    address = token.address,
                    amount = balance.toBigDecimal(),
                    decimals = token.decimals,
                    symbol = token.symbol,
                    name = token.name,
                    network = getNetwork(),
                )
            } else {
                null
            }
        }.filterNotNull()
    }


    override fun nativeTokenName(): String {
        return "AVAX"
    }
}