package io.defitrack.claimables.adapters

import io.defitrack.claimables.adapters.rest.resource.ExceptionResult
import io.defitrack.claimables.domain.ClaimableMarketDTO
import io.defitrack.claimables.domain.UserClaimableDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping

@Tag(
    name = "Claimables",
    description = "Claimables API, enabling us to easily fetch all types of claimables from different protocols"
)
interface ClaimableAggregateRestControllerDocumentation {
    @GetMapping
    @Operation(
        summary = "Get all claimables markets",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Found Claimables",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = (ArraySchema(schema = Schema(implementation = ClaimableMarketDTO::class)))
                    )
                ]
            ),
            ApiResponse(
                description = "validation error",
                responseCode = "403",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ExceptionResult::class),
                    )
                ]
            )]
    )
    suspend fun getPagedMarkets(
        includes: List<String> = emptyList(),
        excludes: List<String> = emptyList(),
        pageable: Pageable
    ): ResponseEntity<Any>

    @GetMapping("/{address}")
    @Operation(
        summary = "Get all claimables for a specific address",
        parameters = [
            Parameter(
                name = "address",
                description = "the account we want to find claimables for",
                example = "0xf18adf71266411FF39FfC268843c9A64b3292d86"
            ),
            Parameter(
                name = "include",
                description = "What protocols to include. If empty, all protocols will be included. Both slug and name can be used. Ex: 'gmx' or 'GMX'",
                required = false,
                array = ArraySchema(schema = Schema(implementation = String::class))
            ),
            Parameter(
                name = "exclude",
                description = "What protocols to exclude. If empty, no protocols will be excluded. Both slug and name can be used. Ex: 'gmx' or 'GMX'",
                required = false,
                array = ArraySchema(schema = Schema(implementation = String::class))
            )
        ]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Found Claimables",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = (ArraySchema(schema = Schema(implementation = UserClaimableDTO::class)))
                    )
                ]
            ),
            ApiResponse(
                description = "validation error",
                responseCode = "403",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ExceptionResult::class),
                    )
                ]
            )]
    )
    suspend fun aggregate(
        address: String,
        includes: List<String> = emptyList(),
        excludes: List<String> = emptyList()
    ): ResponseEntity<Any>
}