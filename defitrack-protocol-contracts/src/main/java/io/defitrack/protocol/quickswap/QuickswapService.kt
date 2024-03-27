package io.defitrack.protocol.quickswap

import org.springframework.stereotype.Component


@Component
class QuickswapService {

    fun getOldDQuickContractAddress(): String {
        return "0xf28164a485b0b2c90639e47b0f377b4a438a16b1"
    }

    fun getDQuickContract(): String {
        return "0x958d208cdf087843e9ad98d23823d32e17d723a1"
    }

    fun getDualRewardFactory(): String {
        return "0x9dd12421c637689c3fc6e661c9e2f02c2f61b3eb"
    }

    fun getRewardFactory(): String {
        return "0x8aaa5e259f74c8114e0a471d9f2adfc66bfe09ed"
    }

    fun getOldRewardFactory(): String {
        return "0x8aaa5e259f74c8114e0a471d9f2adfc66bfe09ed"
    }

    fun getDeprecatedRewardFactory(): String {
        return "0x5eec262b05a57da9beb5fe96a34aa4ed0c5e029f"
    }
}