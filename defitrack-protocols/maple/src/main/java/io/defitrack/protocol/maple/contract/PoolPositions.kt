package io.defitrack.protocol.maple.contract

import java.math.BigInteger

//Data regarding loans and collaterals associated with an account
class UserAccountData(
    val collateralAmount: string,
    val collateralRequired: string,
    val amountFunded: string,
    val collateralSwapped: string,
    val claimableAmount: string,
    val nextPayment: string,
    val nextPaymentDue: string
)
