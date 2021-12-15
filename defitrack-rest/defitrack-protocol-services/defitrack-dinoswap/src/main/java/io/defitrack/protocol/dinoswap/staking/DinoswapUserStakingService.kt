package io.defitrack.protocol.dinoswap.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.VaultRewardToken
import io.defitrack.token.TokenService
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.ERC20Contract
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dinoswap.DinoswapFossilFarmsContract
import io.defitrack.protocol.dinoswap.DinoswapService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*

@Service
class DinoswapUserStakingService(
    private val dinoService: DinoswapService,
    private val abiResource: ABIResource,
    objectMapper: ObjectMapper,
    tokenService: TokenService,
    private val polygonContractAccessor: PolygonContractAccessor,
) : UserStakingService(tokenService, objectMapper) {

    val fossilFarms by lazy {
        abiResource.getABI("polycat/FossilFarms.json")
    }

    override fun getStakings(address: String): List<StakingElement> {

        val polycatMasterChefContracts = dinoService.getDinoFossilFarms().map {
            DinoswapFossilFarmsContract(
                polygonContractAccessor,
                fossilFarms,
                it
            )
        }

        return polycatMasterChefContracts.flatMap { masterChef ->
            (0 until masterChef.poolLength).mapNotNull { poolIndex ->
                val balance = masterChef.userInfo(address, poolIndex).amount

                if (balance > BigInteger.ZERO) {
                    val stakedtoken =
                        tokenService.getTokenInformation(masterChef.getLpTokenForPoolId(poolIndex), getNetwork())
                    val rewardToken = tokenService.getTokenInformation(masterChef.rewardToken, getNetwork())

                    val poolBalance = ERC20Contract(
                        polygonContractAccessor,
                        abiResource.getABI("general/ERC20.json"),
                        stakedtoken.address
                    ).balanceOf(masterChef.address)


                    val userRewardPerBlock = balance.toBigDecimal().divide(
                        poolBalance.toBigDecimal(), 18, RoundingMode.HALF_UP
                    ).times(masterChef.rewardPerBlock.toBigDecimal())

                    val perDay = userRewardPerBlock.times(BigDecimal(43200))

                    StakingElement(
                        id = "dinoswap-${masterChef.address}-${poolIndex}",
                        network = getNetwork(),
                        user = address.lowercase(),
                        protocol = getProtocol(),
                        name = stakedtoken.name + " Vault",
                        url = "https://polygon.iron.finance/farms",
                        stakedToken = vaultStakedToken(
                            stakedtoken.address,
                            balance
                        ),
                        rewardTokens = listOf(
                            VaultRewardToken(
                                name = rewardToken.name,
                                symbol = rewardToken.symbol,
                                decimals = rewardToken.decimals,
                                daily = perDay.divide(BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP).toString()
                            )
                        ),
                        contractAddress = masterChef.address,
                        vaultType = "polycat-masterchef"
                    )
                } else {
                    null
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}