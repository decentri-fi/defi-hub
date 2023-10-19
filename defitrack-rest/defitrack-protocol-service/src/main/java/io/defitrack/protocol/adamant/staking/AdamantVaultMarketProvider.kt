package io.defitrack.protocol.adamant.staking

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.AdamantService
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.ADAMANT)
class AdamantVaultMarketProvider(
    private val adamantService: AdamantService,
) : FarmingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.ADAMANT
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val addy = getToken("0xc3fdbadc7c795ef1d6ba111e06ff8f16a20ea539")

        return getVaults().parMap(EmptyCoroutineContext, 12) { vault ->
              catch {
                  val token = getToken(vault.token())
                  val rewardToken = addy.toFungibleToken()
                  create(
                      name = "${token.name} vault",
                      identifier = vault.address,
                      stakedToken = token.toFungibleToken(),
                      rewardTokens = listOf(
                          rewardToken
                      ),
                      positionFetcher = PositionFetcher(
                          vault.address,
                          { user -> balanceOfFunction(user) }
                      ),
                      claimableRewardFetcher = ClaimableRewardFetcher(
                          Reward(
                              token = rewardToken,
                              contractAddress = vault.address,
                              getRewardFunction = vault::getPendingRewardFunction
                          ),
                          preparedTransaction = selfExecutingTransaction(vault::getClaimFunction)
                      )
                  )
              }
        }.mapNotNull {
            it.mapLeft {
                logger.error("Error fetching vault", it)
            }.getOrNull()
        }
    }

    private suspend fun getVaults() = adamantService.adamantGenericVaults().map {
        AdamantVaultContract(
            getBlockchainGateway(),
            it.vaultAddress
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}