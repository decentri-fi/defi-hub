package io.defitrack.protocol.beefy.staking

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyVaultService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.protocol.beefy.contract.BeefyVaultContract
import io.defitrack.protocol.beefy.staking.invest.BeefyStakingInvestmentPreparer
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

abstract class BeefyFarmingMarketProvider(
) : FarmingMarketProvider() {

    @Autowired
    private lateinit var beefyAPYService: BeefyAPYService

    @Autowired
    private lateinit var beefyService: BeefyVaultService

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = with(getBlockchainGateway()) {
        val contracts = beefyService.getVaults(getNetwork()).map { beefyVault ->
            BeefyVaultContract(
                beefyVault.earnContractAddress,
                beefyVault
            )
        }.resolve()

        return contracts.parMapNotNull(concurrency = 12) { beefy ->
            catch {
                toStakingMarketElement(beefy)
            }.mapLeft {
                logger.error("Failed to create market for ${beefy.beefyVault}", it)
            }.getOrNull()
        }
    }

    private suspend fun toStakingMarketElement(contract: BeefyVaultContract): FarmingMarket {
        val want = getToken(contract.want())
        val token = getToken(contract.address)
        return create(
            identifier = contract.beefyVault.id,
            name = "${want.name} Beefy Vault",
            apr = getAPY(contract),
            stakedToken = want,
            rewardToken = want,
            token = token,
            deprecated = contract.beefyVault.status == "eol",
            marketSize = refreshable {
                getMarketSize(want, contract)
            },
            positionFetcher = PositionFetcher(
                contract::balanceOfFunction,
                extractBalance = { result ->
                    val balance = result[0].value as BigInteger

                    if (balance > BigInteger.ZERO) {
                        Position(
                            (balance.times(contract.pricePerFullShare.await())).asEth().toBigInteger(),
                            balance,
                        )
                    } else Position.ZERO
                }
            ),
            investmentPreparer = BeefyStakingInvestmentPreparer(contract, getERC20Resource()),
            exitPositionPreparer = prepareExit {
                contract.fullExitFunction()
            },
            metadata = mapOf(
                "vaultAddress" to contract.address,
            )
        )
    }

    private suspend fun getMarketSize(
        want: FungibleTokenInformation,
        beefyVault: BeefyVaultContract
    ) = BigDecimal.valueOf(
        getPriceResource().calculatePrice(
            GetPriceCommand(
                want.address,
                getNetwork(),
                beefyVault.balance.await().toBigDecimal()
                    .divide(BigDecimal.TEN.pow(want.decimals), 18, RoundingMode.HALF_UP),
                want.type
            )
        )
    )

    private suspend fun getAPY(contract: BeefyVaultContract): BigDecimal {
        return try {
            (beefyAPYService.getAPYS().getOrDefault(contract.beefyVault.id, BigDecimal.ZERO))
        } catch (ex: Exception) {
            BigDecimal.ZERO
        }
    }
}