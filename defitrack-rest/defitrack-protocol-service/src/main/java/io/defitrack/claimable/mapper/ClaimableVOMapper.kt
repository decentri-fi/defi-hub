package io.defitrack.claimable.mapper

import io.defitrack.claimable.domain.UserClaimable
import io.defitrack.claimable.vo.UserClaimableVO
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
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
                PriceRequest(
                    address = claimableToken.address,
                    network = network,
                    amount = amount,
                    type = claimableToken.type
                )
            )

            UserClaimableVO(
                id = id,
                name = name,
                protocol = protocolVOMapper.map(protocol),
                network = network.toVO(),
                token = claimableToken,
                amount = amount.toDouble(),
                dollarValue = claimableInDollar,
                claimTransaction = preparedTransactionVOMapper.map(claimTransaction)
            )
        }
    }
}