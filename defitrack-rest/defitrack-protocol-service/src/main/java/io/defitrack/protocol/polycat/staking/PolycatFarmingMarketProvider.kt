package io.defitrack.protocol.polycat.staking

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.polycat.PolycatService
import io.defitrack.protocol.polycat.contract.PolycatMasterChefContract
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.POLYCAT)
class PolycatFarmingMarketProvider(
    private val polycatService: PolycatService,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return polycatService.getPolycatFarms().map {
            PolycatMasterChefContract(
                getBlockchainGateway(),
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength.await().toInt()).parMapNotNull(EmptyCoroutineContext, 12) { poolId ->
                catch {
                    toStakingMarketElement(chef, poolId)
                }.mapLeft { error ->
                    logger.error("Error while fetching polycat market", error)
                    null
                }.getOrNull()
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.POLYCAT
    }

    private suspend fun toStakingMarketElement(
        chef: PolycatMasterChefContract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken = getToken(chef.poolInfo(poolId).lpToken)
        val rewardToken = getToken(chef.rewardToken.await())
        return create(
            identifier = "${chef.address}-${poolId}",
            name = stakedtoken.name + " Farm",
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            marketSize = Refreshable.refreshable {
                calculateMarketSize(stakedtoken, chef)
            },
            positionFetcher = PositionFetcher(
                address = chef.address,
                { user ->
                    chef.userInfoFunction(
                        user, poolId
                    )
                },
            ),
        )
    }

    private suspend fun calculateMarketSize(
        stakedtoken: TokenInformationVO,
        chef: PolycatMasterChefContract
    ): BigDecimal {

        val balance = getERC20Resource().getBalance(
            getNetwork(),
            stakedtoken.address,
            chef.address
        )

        return BigDecimal(
            getPriceResource().calculatePrice(
                PriceRequest(
                    stakedtoken.address,
                    getNetwork(),
                    balance.asEth(stakedtoken.decimals),
                    stakedtoken.type
                )
            )
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}