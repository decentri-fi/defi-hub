package io.defitrack.protocol

interface Protocols {
    suspend fun getProtocols(): List<ProtocolInformation>
}