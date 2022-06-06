package io.defitrack.staking

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.toVO
import io.defitrack.staking.domain.StakingPosition
import io.defitrack.staking.vo.StakingElementVO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.web3j.crypto.WalletUtils
import java.math.BigInteger

@RestController
@RequestMapping("/staking")
class DefaultUserStakingRestController(
    private val stakingServices: List<StakingPositionService>,
    private val priceResource: PriceResource
) {

    @GetMapping("/{userId}/positions")
    fun getUserStakings(@PathVariable("userId") address: String): List<StakingElementVO> = runBlocking {
        if (WalletUtils.isValidAddress(address)) {
            stakingServices.flatMap {
                try {
                    runBlocking(Dispatchers.IO) {
                        retry(limitAttempts(3)) {
                            it.getStakings(address).filter {
                                it.amount > BigInteger.ZERO
                            }
                        }
                    }
                } catch (ex: Exception) {
                    logger.error("Something went wrong trying to fetch the user stakings: ${ex.message}")
                    emptyList()
                }
            }.map {
                it.toVO()
            }
        } else {
            emptyList()
        }
    }

    @GetMapping(value = ["/{userId}/positions"], params = ["stakingElementId", "network"])
    fun getStakingById(
        @PathVariable("userId") address: String,
        @RequestParam("stakingElementId") stakingElementId: String,
        @RequestParam("network") network: Network
    ): StakingElementVO? = runBlocking {
        if (WalletUtils.isValidAddress(address)) {
            stakingServices.filter {
                it.getNetwork() == network
            }.firstNotNullOfOrNull {
                try {
                    runBlocking(Dispatchers.IO) {
                        retry(limitAttempts(3)) {
                            it.getStaking(address, stakingElementId)
                        }
                    }
                } catch (ex: Exception) {
                    logger.error("Something went wrong trying to fetch the user poolings: ${ex.message}")
                    null
                }
            }?.toVO()
        } else {
            null
        }
    }


    suspend fun StakingPosition.toVO(): StakingElementVO {

        val stakedInDollars = priceResource.calculatePrice(
            PriceRequest(
                address = market.stakedToken.address,
                network = market.network,
                amount = amount.asEth(market.stakedToken.decimals),
                type = market.stakedToken.type
            )
        )

        return StakingElementVO(
            id = market.id,
            network = market.network.toVO(),
            protocol = market.protocol.toVO(),
            dollarValue = stakedInDollars,
            name = market.name,
            apr = market.apr?.toDouble(),
            vaultType = market.vaultType,
            contractAddress = market.contractAddress,
            stakedToken = market.stakedToken,
            rewardTokens = market.rewardTokens,
            amount = amount.asEth(market.stakedToken.decimals).toDouble()
        )
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}