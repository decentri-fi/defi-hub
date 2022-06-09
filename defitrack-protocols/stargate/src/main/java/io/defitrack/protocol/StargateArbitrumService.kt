package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateArbitrumService: StargateService {

    override fun getLpFarm(): String {
        return "0xeA8DfEE1898a7e0a59f7527F076106d7e44c2176"
    }
}