package io.defitrack.erc20.rest

import arrow.core.getOrElse
import io.defitrack.common.network.Network
import io.defitrack.erc20.*
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.token.TokenType
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import java.math.BigInteger

@RestController
class ERC20RestControllerImpl(
    private val erC20ContractReader: ERC20ContractReader,
    private val erc20Service: ERC20Service,
    private val observationRegistry: ObservationRegistry,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : ERC20RestController {

    val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{network}")
    override suspend fun getAllTokensForNetwork(
        @PathVariable("network") networkName: String,
        @RequestParam("verified") verified: Boolean?
    ): ResponseEntity<List<FungibleToken>> {
        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(
            erc20Service.getAllTokensForNetwork(network)
                .filter {
                    verified == null || it.verified == verified
                }.map {
                    it.toVO()
                }.filter {
                    it.type == TokenType.SINGLE
                }
        )
    }

    @GetMapping("/{network}/wrapped")
    override fun getWrappedToken(@PathVariable("network") networkName: String): ResponseEntity<WrappedToken> {
        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()

        return ResponseEntity.ok(
            WrappedToken(
                ERC20Repository.NATIVE_WRAP_MAPPING[network]!!
            )
        )
    }

    @GetMapping("/{network}/{address}/token")
    override suspend fun getTokenInformation(
        @PathVariable("network") networkName: String,
        @PathVariable("address") address: String
    ): ResponseEntity<FungibleToken> = coroutineScope {
        val network = Network.fromString(networkName) ?: return@coroutineScope ResponseEntity.badRequest().build()
        val observation = Observation.start("erc20.get-token-information", observationRegistry)
            .lowCardinalityKeyValue("network", networkName)
        try {
            erc20Service.getTokenInformation(
                address, network
            ).map {
                ResponseEntity.ok(
                    it.toVO()
                )
            }.getOrElse {
                ResponseEntity.notFound().build()
            }
        } catch (ex: Exception) {
            logger.debug("Error while getting token information", ex)
            ResponseEntity.notFound().build()
        } finally {
            observation.stop()
        }
    }

    @GetMapping("/{network}/{address}/{userAddress}", params = ["v2"])
    override suspend fun getBalanceV2(
        @PathVariable("network") networkName: String,
        @PathVariable("address") address: String,
        @PathVariable("userAddress") userAddress: String
    ): ResponseEntity<Map<String, String>> {

        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()

        if (!WalletUtils.isValidAddress(address)) {
            return ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(userAddress)) {
            return ResponseEntity.badRequest().build()
        }

        return try {
            if (address == "0x0") {
                ResponseEntity.ok(
                    mapOf(
                        "balance" to blockchainGatewayProvider.getGateway(network).getNativeBalance(userAddress)
                            .times(BigDecimal.TEN.pow(18)).toBigInteger().toString()
                    )
                )
            } else {
                ResponseEntity.ok(
                    mapOf(
                        "balance" to erC20ContractReader.getBalance(network, address, userAddress).toString()
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(
                mapOf("balance" to BigInteger.ZERO.toString())
            )
        }
    }

    @GetMapping("/{network}/{address}/{userAddress}")
    override suspend fun getBalance(
        @PathVariable("network") networkName: String,
        @PathVariable("address") address: String,
        @PathVariable("userAddress") userAddress: String
    ): ResponseEntity<BigInteger> {

        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()

        if (!WalletUtils.isValidAddress(address)) {
            return ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(userAddress)) {
            return ResponseEntity.badRequest().build()
        }

        return try {
            if (address == "0x0") {
                ResponseEntity.ok(
                    blockchainGatewayProvider.getGateway(network).getNativeBalance(userAddress)
                        .times(BigDecimal.TEN.pow(18)).toBigInteger()
                )
            } else {
                ResponseEntity.ok(erC20ContractReader.getBalance(network, address, userAddress))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(BigInteger.ZERO)
        }
    }

    @GetMapping("/{network}/allowance/{token}/{userAddress}/{spenderAddress}")
    override suspend fun getApproval(
        @PathVariable("network") networkName: String,
        @PathVariable("token") token: String,
        @PathVariable("userAddress") userAddress: String,
        @PathVariable("spenderAddress") spenderAddress: String,
    ): ResponseEntity<BigInteger> = coroutineScope {

        val network = Network.fromString(networkName) ?: return@coroutineScope ResponseEntity.badRequest().build()

        if (!WalletUtils.isValidAddress(token)) {
            return@coroutineScope ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(userAddress)) {
            return@coroutineScope ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(spenderAddress)) {
            return@coroutineScope ResponseEntity.badRequest().build()
        }

        return@coroutineScope try {
            ResponseEntity.ok(erC20ContractReader.getAllowance(network, token, userAddress, spenderAddress))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(BigInteger.ZERO)
        }
    }
}