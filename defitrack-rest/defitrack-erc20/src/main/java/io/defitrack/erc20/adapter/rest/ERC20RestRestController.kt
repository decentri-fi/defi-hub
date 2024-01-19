package io.defitrack.erc20.adapter.rest

import arrow.core.getOrElse
import io.defitrack.common.network.Network
import io.defitrack.domain.FungibleToken
import io.defitrack.erc20.adapter.rest.vo.UserBalanceVO
import io.defitrack.erc20.adapter.rest.vo.WrappedTokenVO
import io.defitrack.erc20.application.ERC20BalanceService
import io.defitrack.erc20.application.repository.NATIVE_WRAP_MAPPING
import io.defitrack.erc20.domain.toVO
import io.defitrack.erc20.port.input.AllowanceUseCase
import io.defitrack.erc20.port.input.TokenInformationUseCase
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
import java.math.BigInteger

@RestController
class ERC20RestRestController(
    private val allowanceUseCase: AllowanceUseCase,
    private val erC20BalanceService: ERC20BalanceService,
    private val tokenInformationUseCase: TokenInformationUseCase,
    private val observationRegistry: ObservationRegistry,
) : ERC20RestDocumentation {

    val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{network}")
    override suspend fun getAllTokensForNetwork(
        @PathVariable("network") networkName: String,
        @RequestParam("verified", defaultValue = "false") verified: Boolean
    ): ResponseEntity<List<FungibleToken>> {
        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(
            tokenInformationUseCase.getAllSingleTokens(network, verified)
                .map {
                    it.toVO()
                }
        )
    }

    @GetMapping("/{network}/wrapped")
    override fun getWrappedToken(@PathVariable("network") networkName: String): ResponseEntity<WrappedTokenVO> {
        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(
            WrappedTokenVO(
                NATIVE_WRAP_MAPPING[network]!!
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
            tokenInformationUseCase.getTokenInformation(
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
    ): ResponseEntity<UserBalanceVO> {

        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()

        if (!WalletUtils.isValidAddress(address)) {
            return ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(userAddress)) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(
            UserBalanceVO(erC20BalanceService.getBalance(network, address, userAddress).toString())
        )
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

        return ResponseEntity.ok(
            erC20BalanceService.getBalance(network, address, userAddress)
        )
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
            ResponseEntity.ok(allowanceUseCase.getAllowance(network, token, userAddress, spenderAddress))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(BigInteger.ZERO)
        }
    }
}