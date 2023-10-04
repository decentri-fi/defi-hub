package io.defitrack.protocol.mstable

import org.springframework.stereotype.Service

@Service
class MStableEthereumService {

    fun getSavingsContracts(): List<String> {
        return listOf(
            "0x30647a72dc82d7fbb1123ea74716ab8a317eac19",
            "0x945Facb997494CC2570096c74b5F66A3507330a1"
        )
    }

    fun getBoostedSavingsVaults(): List<String> {
        return listOf(
            "0x78BefCa7de27d07DC6e71da295Cc2946681A6c7B",
            "0xF38522f63f40f9Dd81aBAfD2B8EFc2EC958a3016"
        )
    }
}