package io.defitrack.protocol.aura.staking

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aura.AuraDepositContract
import io.defitrack.protocol.aura.RewardPoolFactoryContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AURA)
class AuraRewardPoolMarketProvider : FarmingMarketProvider() {

    val rewardFactoryAddress = "0xbc8d9caf4b6bf34773976c5707ad1f2778332dca"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = RewardPoolFactoryContract(getBlockchainGateway(), rewardFactoryAddress)
        return resolve(
            contract.getCreatedPools("16176243", null)
                .map {
                    AuraDepositContract(getBlockchainGateway(), it)
                })
            .parMapNotNull {
                catch {
                    createMarket(it)
                }.mapLeft { ex ->
                    logger.error("Error fetching market for $it", ex)
                }.getOrNull()
            }
    }

    private suspend fun createMarket(contract: AuraDepositContract): FarmingMarket {
        val reward = getToken(contract.rewardToken.await())
        val stakingToken = getToken(contract.stakingToken.await())
        return create(
            name = "Aura Deposit Vault",
            identifier = contract.address,
            stakedToken = stakingToken,
            rewardToken = reward,
            positionFetcher = defaultPositionFetcher(contract.address),
            token = getToken(contract.address)
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.AURA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}