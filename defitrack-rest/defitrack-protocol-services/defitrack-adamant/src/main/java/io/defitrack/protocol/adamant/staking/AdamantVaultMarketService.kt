package io.defitrack.protocol.adamant.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.AdamantService
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class AdamantVaultMarketService(
    private val adamantService: AdamantService,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway
) : StakingMarketService() {

    val genericVault by lazy {
        abiResource.getABI("adamant/GenericVault.json")
    }

    val addy by lazy {
        erC20Resource.getTokenInformation(getNetwork(), "0xc3fdbadc7c795ef1d6ba111e06ff8f16a20ea539")
    }


    override suspend fun fetchStakingMarkets(): List<StakingMarket> =
        withContext(Dispatchers.IO.limitedParallelism(10)) {
            adamantService.adamantGenericVaults().map {
                AdamantVaultContract(
                    contractAccessorGateway.getGateway(getNetwork()),
                    genericVault,
                    it.vaultAddress
                )
            }.map { vault ->
                async {
                    try {
                        val token = erC20Resource.getTokenInformation(getNetwork(), vault.token)
                        StakingMarket(
                            name = "${token.name} vault",
                            id = "adamant-polygon-${vault.address}",
                            stakedToken = token.toFungibleToken(),
                            rewardTokens = listOf(
                                token.toFungibleToken(),
                                addy.toFungibleToken()
                            ),
                            contractAddress = vault.address,
                            vaultType = "adamant-generic-vault",
                            network = getNetwork(),
                            protocol = getProtocol(),
                            balanceFetcher = StakingMarketBalanceFetcher(
                                vault.address,
                                { user -> vault.balanceOfMethod(user) }
                            )
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

    override fun getProtocol(): Protocol {
        return Protocol.ADAMANT
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}