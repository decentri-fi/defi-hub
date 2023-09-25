package io.defitrack.transaction

import org.springframework.stereotype.Component
import org.web3j.abi.FunctionEncoder

@Component
class PreparedTransactionVOMapper {
    fun map(preparedTransaction: PreparedTransaction?): PreparedTransactionVO? {
        if (preparedTransaction == null) {
            return null
        }
        return PreparedTransactionVO(
            network = preparedTransaction.network,
            data = FunctionEncoder.encode(preparedTransaction.function),
            to = preparedTransaction.to,
            from = preparedTransaction.from
        )
    }
}