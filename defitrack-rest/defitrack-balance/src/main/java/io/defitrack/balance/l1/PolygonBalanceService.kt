package io.defitrack.balance.l1

import io.defitrack.abi.ABIResource
import io.defitrack.balance.BalanceService
import io.defitrack.balance.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.polygon.config.PolygonGateway
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class PolygonBalanceService(
    private val abiResource: ABIResource,
    private val maticGateway: PolygonGateway,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val erC20Service: ERC20Resource
) : BalanceService {

    val erc20ABI by lazy {
        abiResource.getABI("general/ERC20.json")
    }

    override fun getNetwork(): Network = Network.POLYGON

    override fun getNativeBalance(address: String): BigDecimal =
        maticGateway.web3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
            .toBigDecimal().divide(
                BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP
            )

    override fun getTokenBalances(user: String): List<TokenBalance> {
        val tokenAddresses = erC20Service.getAllTokens(getNetwork()).map {
            it.address
        }

        return polygonContractAccessor.readMultiCall(tokenAddresses.map { address ->
            MultiCallElement(
                polygonContractAccessor.createFunction(
                    polygonContractAccessor.getFunction(
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
        return "MATIC"
    }
}