package io.defitrack.erc20.adapter.rest

import io.defitrack.erc20.FungibleToken
import io.defitrack.erc20.adapter.rest.vo.UserBalanceVO
import io.defitrack.erc20.adapter.rest.vo.WrappedTokenVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import java.math.BigInteger

@Tag(
    name = "ERC20 Tokens",
    description = "ERC20 API, enabling us to easily fetch the information of various tokens."
)
interface ERC20RestDocumentation {
    @GetMapping("/{network}")
    @Operation(
        summary = "Get all tokens for a network",
    )
    @Parameters(
        Parameter(
            name = "network",
            example = "base",
            required = true
        ),
        Parameter(
            name = "verified",
            example = "false",
            required = false
        )
    )
    @ApiResponses(
        ApiResponse(
            description = "all tokens for a network",
            responseCode = "200",
            content = [
                Content(
                    mediaType = "application/json",
                    array = ArraySchema(
                        schema = Schema(
                            implementation = FungibleToken::class
                        )
                    )
                )
            ]
        )
    )
    suspend fun getAllTokensForNetwork(
        networkName: String,
        verified: Boolean = true
    ): ResponseEntity<List<FungibleToken>>

    @GetMapping("/{network}/wrapped")
    @Operation(
        summary = "Get the wrapped token represenation of the native token for a network",
        parameters = [
            Parameter(
                name = "network",
                description = "the network the token resides on",
                example = "base"
            )
        ]
    )
    @ApiResponses(
        ApiResponse(
            description = "all tokens for a network",
            responseCode = "200",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = WrappedTokenVO::class
                    )
                )
            ]
        )
    )
    fun getWrappedToken(networkName: String): ResponseEntity<WrappedTokenVO>

    @GetMapping("/{network}/{address}/token")
    @Operation(
        summary = "Get the token information for a specific token on a network",
        parameters = [
            Parameter(
                name = "network",
                description = "the network the token resides on",
                example = "base"
            ),
            Parameter(
                name = "address",
                description = "the token we want to fetch the information for",
                example = "0x833589fcd6edb6e08f4c7c32d4f71b54bda02913"
            ),
        ]
    )
    @ApiResponses(
        ApiResponse(
            description = "The token information for a specific token on a network",
            responseCode = "200",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = FungibleToken::class
                    )
                )
            ]
        )
    )
    suspend fun getTokenInformation(networkName: String, address: String): ResponseEntity<FungibleToken>

    @GetMapping("/{network}/{address}/{userAddress}")
    @Operation(
        summary = "Get the balance of a specific token for a specific user",
        parameters = [
            Parameter(
                name = "network",
                description = "the network the token resides on",
                example = "base"
            ),
            Parameter(
                name = "address",
                description = "the token we want to fetch the balance for",
                example = "0x833589fcd6edb6e08f4c7c32d4f71b54bda02913"
            ),
            Parameter(
                name = "userAddress",
                description = "the user we want to fetch the balance for",
                example = "0xf18adf71266411FF39FfC268843c9A64b3292d86"
            ),
        ]
    )
    @ApiResponses(
        ApiResponse(
            description = "The balance of a specific token for a specific user",
            responseCode = "200",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = BigInteger::class
                    )
                )
            ]
        )
    )
    suspend fun getBalance(networkName: String, address: String, userAddress: String): ResponseEntity<BigInteger>

    @GetMapping("/{network}/allowance/{token}/{userAddress}/{spenderAddress}")
    @Operation(
        summary = "Get the allowance of a specific token for a specific user for a specific spender",
        parameters = [
            Parameter(
                name = "network",
                description = "the network the token resides on",
                example = "base"
            ),
            Parameter(
                name = "token",
                description = "the token we want to fetch the allowance for"
            ),
            Parameter(
                name = "userAddress",
                description = "the user we want to fetch the allowance for"
            ),
            Parameter(
                name = "spenderAddress",
                description = "the spender we want to fetch the allowance for"
            ),
        ]
    )
    @ApiResponses(
        ApiResponse(
            description = "The allowance of a specific token for a specific user for a specific spender",
            responseCode = "200",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = BigInteger::class
                    )
                )
            ]
        )
    )
    suspend fun getApproval(
        networkName: String,
        token: String,
        userAddress: String,
        spenderAddress: String
    ): ResponseEntity<BigInteger>

    @GetMapping("/{network}/{address}/{userAddress}", params = ["v2"])
    suspend fun getBalanceV2(
        networkName: String,
        address: String,
        userAddress: String
    ): ResponseEntity<UserBalanceVO>
}