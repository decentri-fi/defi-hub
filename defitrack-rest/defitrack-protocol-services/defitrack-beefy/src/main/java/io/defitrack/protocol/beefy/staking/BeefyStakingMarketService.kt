package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import java.math.BigDecimal
import java.math.RoundingMode

abstract class BeefyStakingMarketService(
    private val contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    private val beefyAPYService: BeefyAPYService,
    private val vaults: List<BeefyVault>,
    private val erC20Resource: ERC20Resource,
    private val priceService: PriceResource
) : StakingMarketService() {

    val vaultV6ABI by lazy {
        abiResource.getABI("beefy/VaultV6.json")
    }

    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
        return vaults
            .mapNotNull {
                toStakingMarketElement(it)
            }
    }

    private fun toStakingMarketElement(beefyVault: BeefyVault): StakingMarketElement? {
        return try {
            val contract = BeefyVaultContract(
                contractAccessorGateway.getGateway(getNetwork()),
                vaultV6ABI,
                beefyVault.earnContractAddress,
                beefyVault.id
            )
            val want = erC20Resource.getTokenInformation(getNetwork(), contract.want)
            StakingMarketElement(
                id = contract.vaultId,
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${contract.symbol} Beefy Vault",
                apr = getAPY(contract),
                stakedToken = want.toFungibleToken(),
                rewardTokens = listOf(
                    want.toFungibleToken()
                ),
                contractAddress = contract.address,
                marketSize = getMarketSize(want, contract),
                vaultType = "beefyVaultV6",
                balanceFetcher = StakingMarketBalanceFetcher(
                    contract.address,
                    { user -> contract.balanceOfMethod(user) }
                )
            )
        } catch (ex: Exception) {
            logger.error("Error trying to fetch vault metadata", ex)
            null
        }
    }

    private fun getMarketSize(
        want: TokenInformation,
        beefyVault: BeefyVaultContract
    ) = BigDecimal.valueOf(
        priceService.calculatePrice(
            PriceRequest(
                want.address,
                getNetwork(),
                beefyVault.balance.toBigDecimal()
                    .divide(BigDecimal.TEN.pow(want.decimals), 18, RoundingMode.HALF_UP),
                want.type
            )
        )
    )

    private fun getAPY(beefyVault: BeefyVaultContract): BigDecimal {
        return try {
            (beefyAPYService.getAPYS().getOrDefault(beefyVault.vaultId, null)) ?: BigDecimal.ZERO
        } catch (ex: Exception) {
            BigDecimal.ZERO
        }
    }

}