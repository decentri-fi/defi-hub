package io.defitrack.erc20.adapter.rest

import arrow.core.getOrElse
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.erc20.FungibleTokenInformationVO
import io.defitrack.erc20.adapter.rest.vo.WrappedTokenVO
import io.defitrack.erc20.adapter.tokens.NATIVE_WRAP_MAPPING
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
    private val tokenInformationUseCase: TokenInformationUseCase,
    private val observationRegistry: ObservationRegistry,
) : ERC20RestDocumentation {

    val logger = LoggerFactory.getLogger(this::class.java)

    val stableCoins = AsyncUtils.lazyAsync {
        mapOf(
            Network.OPTIMISM to listOf(
                tokenInformationUseCase.getTokenInformation(
                    "0x94b008aa00579c1307b0ef2c499ad98a8ce58e58",
                    Network.OPTIMISM,
                    true
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x0b2c639c533813f4aa9d7837caf62653d097ff85",
                    Network.OPTIMISM,
                    true
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x7f5c764cbc14f9669b88837ca1490cca17c31607",
                    Network.OPTIMISM,
                    true
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0xda10009cbd5d07dd0cecc66161fc93d7c9000da1",
                    Network.OPTIMISM,
                    true
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x2e3d870790dc77a83dd1d18184acc7439a53f475",
                    Network.OPTIMISM,
                    true
                ),
            ),
            Network.ARBITRUM to listOf(
                tokenInformationUseCase.getTokenInformation(
                    "0xaf88d065e77c8cc2239327c5edb3a432268e5831",
                    Network.ARBITRUM,
                    true
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0xfd086bc7cd5c481dcc9c85ebe478a1c0b69fcbb9",
                    Network.ARBITRUM,
                    true
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0xff970a61a04b1ca14834a43f5de4533ebddb5cc8",
                    Network.ARBITRUM,
                    true
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0xda10009cbd5d07dd0cecc66161fc93d7c9000da1",
                    Network.ARBITRUM,
                    true
                ),
            ),
            Network.ETHEREUM to listOf(
                tokenInformationUseCase.getTokenInformation(
                    "0xdac17f958d2ee523a2206206994597c13d831ec7",
                    Network.ETHEREUM
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48",
                    Network.ETHEREUM
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x6b175474e89094c44da98b954eedeac495271d0f",
                    Network.ETHEREUM
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x0000000000085d4780B73119b644AE5ecd22b376",
                    Network.ETHEREUM
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x4fabb145d64652a948d72533023f6e7a623c7c53",
                    Network.ETHEREUM
                ),
            ),
            Network.BASE to listOf(
                tokenInformationUseCase.getTokenInformation("0x833589fcd6edb6e08f4c7c32d4f71b54bda02913", Network.BASE),
                tokenInformationUseCase.getTokenInformation("0x50c5725949a6f0c72e6c4a641f24049a917db0cb", Network.BASE),
                tokenInformationUseCase.getTokenInformation("0xda3de145054ed30ee937865d31b500505c4bdfe7", Network.BASE),
                tokenInformationUseCase.getTokenInformation("0xd9aaec86b65d86f6a7b5b1b0c42ffa531710b6ca", Network.BASE),
            ),
            Network.POLYGON_ZKEVM to listOf(
                tokenInformationUseCase.getTokenInformation(
                    "0x1e4a5963abfd975d8c9021ce480b42188849d41d",
                    Network.POLYGON_ZKEVM
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0xa8ce8aee21bc2a48a5ef670afcc9274c7bbbc035",
                    Network.POLYGON_ZKEVM
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0xc5015b9d9161dca7e18e32f6f25c4ad850731fd4",
                    Network.POLYGON_ZKEVM
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0xFf8544feD5379D9ffa8D47a74cE6b91e632AC44D",
                    Network.POLYGON_ZKEVM
                ),
            ),
            Network.POLYGON to listOf(
                tokenInformationUseCase.getTokenInformation(
                    "0xc2132d05d31c914a87c6611c10748aeb04b58e8f",
                    Network.POLYGON
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x3c499c542cEF5E3811e1192ce70d8cC03d5c3359",
                    Network.POLYGON
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x2791bca1f2de4661ed88a30c99a7a9449aa84174",
                    Network.POLYGON
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0xdab529f40e671a1d4bf91361c21bf9f0c9712ab7",
                    Network.POLYGON
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x8f3cf7ad23cd3cadbd9735aff958023239c6a063",
                    Network.POLYGON
                ),
                tokenInformationUseCase.getTokenInformation(
                    "0x2e1ad108ff1d8c782fcbbb89aad783ac49586756",
                    Network.POLYGON
                ),
            )
        )
    }


    @GetMapping("{network}/stables/usd")
    suspend fun stables(@PathVariable("network") networkAsString: String): ResponseEntity<List<FungibleTokenInformationVO>> {
        val network = Network.fromString(networkAsString) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(
            stableCoins.await()[network]?.mapNotNull {
                it.getOrNull()?.toVO()
            } ?: emptyList()
        )
    }

    @GetMapping("/{network}")
    override suspend fun getAllTokensForNetwork(
        @PathVariable("network") networkName: String,
        @RequestParam("verified", defaultValue = "false") verified: Boolean
    ): ResponseEntity<List<FungibleTokenInformationVO>> {
        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()
        try {
            return ResponseEntity.ok(
                tokenInformationUseCase.getAllSingleTokens(network, verified)
                    .map {
                        it.toVO()
                    }
            )
        } catch (ex: Exception) {
            logger.debug("Error while getting token information", ex)
            return ResponseEntity.internalServerError().build()
        }
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
    ): ResponseEntity<FungibleTokenInformationVO> = coroutineScope {
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