package io.defitrack.protocol.application.mantle

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mantle.MantleStakingContract
import org.springframework.stereotype.Component
import java.math.BigInteger

@ConditionalOnCompany(Company.MANTLE)
@Component
class MantleETHStakingMarketProvider(

) : FarmingMarketProvider(
) {

    val methAddress = "0xd5f7838f5c461feff7fe49ea5ebaf7728bb0adfa"
    val stakingContract = "0xe3cBd06D7dadB3F4e6557bAb7EdD924CD1489E8f"

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val stakingContract = MantleStakingContract(stakingContract)

        return create(
            name = "mantleETH",
            identifier = methAddress,
            stakedToken = getToken("0x0"),
            rewardToken = getToken("0x0"),
            type = "mantle.staked-eth",
            positionFetcher = PositionFetcher(
                { user ->
                    ContractCall(
                        ERC20Contract.balanceOf(user),
                        getNetwork(),
                        methAddress,
                    )
                }
            ) { retVal ->
                val result = retVal[0].value as BigInteger
                if (result > BigInteger.ZERO) {
                    val underlying = stakingContract.mEThToEth(result)
                    Position(underlying, result)
                } else {
                    Position.ZERO
                }
            },
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.MANTLE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }


}