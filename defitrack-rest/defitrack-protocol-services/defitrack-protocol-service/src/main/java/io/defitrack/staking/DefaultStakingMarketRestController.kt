package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.network.toVO
import io.defitrack.protocol.toVO
import io.defitrack.staking.command.PrepareInvestmentCommand
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.staking.vo.StakingMarketVO
import io.defitrack.staking.vo.TransactionPreparationVO
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/staking")
class DefaultStakingMarketRestController(
    private val stakingMarketServices: List<StakingMarketService>,
    private val erC20Resource: ERC20Resource
) {

    @GetMapping("/all-markets", params = ["network"])
    fun stakingMarketsByNetwork(
        @RequestParam("network") network: Network
    ): List<StakingMarketVO> {
        return stakingMarketServices
            .filter {
                it.getNetwork() == network
            }.flatMap {
                it.getStakingMarkets()
            }.map {
                toVO(it)
            }
    }

    @GetMapping(value = ["/markets"], params = ["token", "network"])
    fun searchByToken(
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<StakingMarketVO> {
        return stakingMarketServices
            .filter {
                it.getNetwork() == network
            }.flatMap {
                it.getStakingMarkets()
            }.filter {
                val token = erC20Resource.getTokenInformation(it.network, it.stakedToken.address.lowercase())
                if (token.type != TokenType.SINGLE) {
                    it.stakedToken.address.lowercase() == tokenAddress.lowercase() ||
                            token.underlyingTokens.any { underlyingToken ->
                                underlyingToken.address.lowercase() == tokenAddress.lowercase()
                            }
                } else {
                    it.stakedToken.address.lowercase() == tokenAddress.lowercase()
                }
            }.map {
                toVO(it)
            }
    }

    @GetMapping(value = ["/markets/{id}"], params = ["network"])
    fun getById(
        @PathVariable("id") id: String,
        @RequestParam("network") network: Network
    ): ResponseEntity<StakingMarketVO> {
        return getStakingMarketById(network, id)?.let {
            ResponseEntity.ok(toVO(it))
        } ?: ResponseEntity.notFound().build()
    }

    private fun getStakingMarketById(
        network: Network,
        id: String
    ) = stakingMarketServices
        .filter {
            it.getNetwork() == network
        }.flatMap {
            it.getStakingMarkets()
        }.firstOrNull {
            it.id == id
        }

    @PostMapping(value = ["/markets/{id}"], params = ["network"])
    fun prepareStakingMarket(
        @PathVariable("id") id: String,
        @RequestParam("network") network: Network,
        @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> = runBlocking {
        getStakingMarketById(
            network, id
        )?.investmentPreparer?.prepare(prepareInvestmentCommand)?.let { transactions ->
            ResponseEntity.ok(
                TransactionPreparationVO(
                    transactions
                )
            )
        } ?: ResponseEntity.badRequest().build()
    }

    private fun toVO(it: StakingMarket) = StakingMarketVO(
        id = it.id,
        network = it.network.toVO(),
        protocol = it.protocol.toVO(),
        name = it.name,
        stakedToken = it.stakedToken,
        reward = it.rewardTokens,
        contractAddress = it.contractAddress,
        vaultType = it.vaultType,
        marketSize = it.marketSize,
        apr = it.apr,
        prepareInvestmentSupported = it.investmentPreparer != null
    )
}