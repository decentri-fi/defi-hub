package io.defitrack.staking

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.toVO
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.VaultStakedToken
import io.defitrack.staking.vo.StakingElementVO
import io.defitrack.staking.vo.VaultStakedTokenVO
import io.defitrack.staking.vo.toVO
import io.defitrack.price.PriceResource
import io.defitrack.common.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@RestController
@RequestMapping("/staking")
class DefaultUserStakingRestController(
    private val stakingServices: List<UserStakingService>,
    private val priceResource: PriceResource
) {

    @GetMapping("/{userId}/positions")
    fun getUserStakings(@PathVariable("userId") address: String): List<StakingElementVO> {
        if (WalletUtils.isValidAddress(address)) {
            return stakingServices.flatMap {
                try {
                    runBlocking(Dispatchers.IO) {
                        retry(limitAttempts(3)) {
                            it.getStakings(address).filter {
                                (it.stakedToken?.amount ?: BigInteger.ZERO) > BigInteger.ZERO
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
            return emptyList()
        }
    }

    @GetMapping(value = ["/{userId}/positions"], params = ["stakingElementId", "network"])
    fun getStakingById(
        @PathVariable("userId") address: String,
        @RequestParam("stakingElementId") stakingElementId: String,
        @RequestParam("network") network: Network
    ): StakingElementVO? {
        if (WalletUtils.isValidAddress(address)) {
            return stakingServices.filter {
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
            return null;
        }
    }


    fun StakingElement.toVO(): StakingElementVO {
        return StakingElementVO(
            id = id,
            network = network.toVO(),
            protocol = protocol.toVO(),
            dollarValue = priceResource.calculatePrice(
                stakedToken?.toPriceRequest()
            ),
            name = name,
            rate = rate,
            url = url,
            vaultType = vaultType,
            contractAddress = contractAddress,
            stakedToken = stakedToken?.toVO(),
            rewardTokens = rewardTokens.map {
                it.toVO()
            }
        )
    }


    fun VaultStakedToken.toVO(): VaultStakedTokenVO {
        return VaultStakedTokenVO(
            name = name,
            symbol = symbol,
            decimals = decimals,
            amount = amount.toBigDecimal().divide(BigDecimal.TEN.pow(decimals), 18, RoundingMode.HALF_UP),
        )
    }

    fun VaultStakedToken.toPriceRequest(): PriceRequest {
        return PriceRequest(
            address = address,
            network = network,
            amount = amount.toBigDecimal().divide(BigDecimal.TEN.pow(decimals), 18, RoundingMode.HALF_UP),
            type = type
        )
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }
}