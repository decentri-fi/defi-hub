package io.defitrack.protocol.compound

import org.springframework.stereotype.Service

@Service
class IronBankFantomService : IronBankService {
    override fun getComptroller(): String {
        return "0x4250a6d3bd57455d7c6821eecb6206f507576cd2"
    }
}