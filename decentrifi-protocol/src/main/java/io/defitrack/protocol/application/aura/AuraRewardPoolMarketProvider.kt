package io.defitrack.protocol.application.aura

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aura.AuraDepositContract
import io.defitrack.protocol.aura.RewardPoolFactoryContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AURA)
class AuraRewardPoolMarketProvider : FarmingMarketProvider() {

    val rewardFactoryAddress = "0xbc8d9caf4b6bf34773976c5707ad1f2778332dca"

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = RewardPoolFactoryContract(rewardFactoryAddress)
        return contract.getCreatedPools("16176243", null)
            .map {
                AuraDepositContract(it)
            }.resolve().parMapNotNull {
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
            token = getToken(contract.address),
            type = "aura.rewards"
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.AURA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}