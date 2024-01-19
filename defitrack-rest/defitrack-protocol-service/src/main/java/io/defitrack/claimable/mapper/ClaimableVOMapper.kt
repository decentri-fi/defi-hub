package io.defitrack.claimable.mapper

import io.defitrack.claimable.domain.UserClaimable
import io.defitrack.claimable.vo.UserClaimableVO
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.domain.GetPriceCommand
import io.defitrack.domain.toNetworkInformation
import io.defitrack.port.input.PriceResource
import io.defitrack.protocol.mapper.ProtocolVOMapper
import io.defitrack.transaction.PreparedTransactionVOMapper
import org.springframework.stereotype.Component

@Component
class ClaimableVOMapper(
    private val priceResource: PriceResource,
    private val protocolVOMapper: ProtocolVOMapper,
    private val preparedTransactionVOMapper: PreparedTransactionVOMapper
) {

    suspend fun map(userClaimable: UserClaimable): UserClaimableVO {
        return with(userClaimable) {
            val amount = amount.asEth(claimableToken.decimals)
            val claimableInDollar = priceResource.calculatePrice(
                GetPriceCommand(
                    address = claimableToken.address,
                    network = network,
                    amount = amount,
                )
            )

            UserClaimableVO(
                id = id,
                name = name,
                protocol = protocolVOMapper.map(protocol),
                network = network.toNetworkInformation(),
                token = claimableToken,
                amount = amount.toDouble(),
                dollarValue = claimableInDollar,
                claimTransaction = preparedTransactionVOMapper.map(claimTransaction)
            )
        }
    }
}