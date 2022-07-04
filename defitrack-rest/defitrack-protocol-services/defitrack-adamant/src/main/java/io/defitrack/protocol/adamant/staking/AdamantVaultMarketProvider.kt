package io.defitrack.protocol.adamant.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.AdamantService
import io.defitrack.protocol.adamant.AdamantVaultContract
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.*
import org.springframework.stereotype.Service

@Service
class AdamantVaultMarketProvider(
    private val adamantService: AdamantService,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : FarmingMarketProvider() {

    val genericVault by lazy {
        abiResource.getABI("adamant/GenericVault.json")
    }

    val addy by lazy {
        runBlocking(Dispatchers.IO) {
            erC20Resource.getTokenInformation(getNetwork(), "0xc3fdbadc7c795ef1d6ba111e06ff8f16a20ea539")
        }
    }


    override suspend fun fetchMarkets(): List<FarmingMarket> =
        withContext(Dispatchers.IO.limitedParallelism(10)) {
            adamantService.adamantGenericVaults().map {
                AdamantVaultContract(
                    blockchainGatewayProvider.getGateway(getNetwork()),
                    genericVault,
                    it.vaultAddress
                )
            }.map { vault ->
                async {
                    try {
                        val token = erC20Resource.getTokenInformation(getNetwork(), vault.token())
                        create(
                            name = "${token.name} vault",
                            identifier = vault.address,
                            stakedToken = token.toFungibleToken(),
                            rewardTokens = listOf(
                                token.toFungibleToken(),
                                addy.toFungibleToken()
                            ),
                            vaultType = "adamant-generic-vault",
                            balanceFetcher = FarmingPositionFetcher(
                                vault.address,
                                { user -> vault.balanceOfMethod(user) }
                            ),
                            farmType = FarmType.VAULT
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