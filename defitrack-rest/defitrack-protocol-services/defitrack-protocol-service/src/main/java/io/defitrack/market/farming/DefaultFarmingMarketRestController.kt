package io.defitrack.market.farming

import io.defitrack.common.network.Network
import io.defitrack.exit.ExitPositionCommand
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.DefaultMarketRestController
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.mapper.FarmingMarketVOMapper
import io.defitrack.market.farming.vo.FarmingMarketVO
import io.defitrack.market.farming.vo.TransactionPreparationVO
import io.defitrack.token.DecentrifiERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(*["/{protocol}/staking", "/{protocol}/farming"])
class DefaultFarmingMarketRestController(
    private val farmingMarketProviders: List<FarmingMarketProvider>,
    private val erC20Resource: DecentrifiERC20Resource,
    private val farmingMarketVOMapper: FarmingMarketVOMapper
) : DefaultMarketRestController<FarmingMarket>(
    farmingMarketProviders, farmingMarketVOMapper
) {

    @GetMapping(value = ["/markets"], params = ["token", "network"])
    fun searchByToken(
        @PathVariable("protocol") protocol: String,
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<FarmingMarketVO> = runBlocking {
        farmingMarketProviders.filter {
            it.getProtocol().slug == protocol
        }.filter {
            it.getNetwork() == network
        }.flatMap {
            it.getMarkets()
        }.filter {
            val token = erC20Resource.getTokenInformation(it.network, it.stakedToken.address.lowercase())
            if (token.type != TokenType.SINGLE) {
                it.stakedToken.address.lowercase() == tokenAddress.lowercase() || token.underlyingTokens.any { underlyingToken ->
                    underlyingToken.address.lowercase() == tokenAddress.lowercase()
                }
            } else {
                it.stakedToken.address.lowercase() == tokenAddress.lowercase()
            }
        }.map(farmingMarketVOMapper::map)
    }

    @GetMapping(value = ["/markets/{marketId}"])
    fun getById(
        @PathVariable("marketId") id: String,
    ): ResponseEntity<FarmingMarketVO> {
        return getStakingMarketById(id)?.let {
            ResponseEntity.ok(
                farmingMarketVOMapper.map(it)
            )
        } ?: ResponseEntity.notFound().build()
    }

    private fun getStakingMarketById(
        id: String
    ) = farmingMarketProviders.flatMap {
        it.getMarkets()
    }.firstOrNull {
        it.id == id
    }

    @PostMapping(value = ["/markets/{id}/enter"])
    suspend fun prepareInvestment(
        @PathVariable("id") id: String, @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> {
        return getStakingMarketById(id)?.investmentPreparer?.prepare(prepareInvestmentCommand)?.let { transactions ->
            ResponseEntity.ok(
                TransactionPreparationVO(
                    transactions
                )
            )
        } ?: ResponseEntity.badRequest().build()
    }

    @PostMapping(value = ["/markets/{id}/exit"])
    suspend fun prepareExit(
        @PathVariable("id") id: String, @RequestBody exitPositionCommand: ExitPositionCommand
    ) {
        getStakingMarketById(id)?.exitPositionPreparer?.prepare(exitPositionCommand)?.let { transactions ->
            ResponseEntity.ok(
                TransactionPreparationVO(
                    transactions
                )
            )
        } ?: ResponseEntity.badRequest().build()
    }
}