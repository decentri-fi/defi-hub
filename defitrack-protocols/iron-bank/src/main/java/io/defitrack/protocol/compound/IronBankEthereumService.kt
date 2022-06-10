package io.defitrack.protocol.compound

import org.springframework.stereotype.Service

@Service
class IronBankEthereumService : IronBankService {
    override fun getComptroller(): String {
        return "0xAB1c342C7bf5Ec5F02ADEA1c2270670bCa144CbB"
    }
}