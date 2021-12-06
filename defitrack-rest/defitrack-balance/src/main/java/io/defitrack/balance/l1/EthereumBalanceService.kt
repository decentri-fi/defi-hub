package io.defitrack.balance.l1

import io.defitrack.abi.ABIResource
import io.defitrack.balance.BalanceService
import io.defitrack.balance.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.ethereum.config.EthereumGateway
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class EthereumBalanceService(
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val ethereumGateway: EthereumGateway,
    private val erc20Resource: ERC20Resource,
    private val abiResource: ABIResource,
) : BalanceService {
    val erc20ABI by lazy {
        abiResource.getABI("general/ERC20.json")
    }

    override fun getNetwork(): Network = Network.ETHEREUM

    override fun getNativeBalance(address: String): BigDecimal =
        ethereumGateway.web3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
            .toBigDecimal().divide(
                BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP
            )

    override fun getTokenBalances(user: String): List<TokenBalance> {
        val tokenAddresses = erc20Resource.getAllTokens(getNetwork()).map {
            it.address
        }

        if (tokenAddresses.isEmpty()) {
            return emptyList()
        }

        return ethereumContractAccessor.readMultiCall(tokenAddresses.map { address ->
            MultiCallElement(
                ethereumContractAccessor.createFunction(
                    ethereumContractAccessor.getFunction(
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
                val token = erc20Resource.getERC20(getNetwork(), tokenAddresses[i])
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
        return "ETH"
    }
}