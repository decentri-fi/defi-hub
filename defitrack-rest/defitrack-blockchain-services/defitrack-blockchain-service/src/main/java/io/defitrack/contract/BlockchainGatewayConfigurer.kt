package io.defitrack.contract

interface BlockchainGatewayConfigurer {
    fun blockchainGateway(): BlockchainGateway
}