package io.defitrack.protocol.set

interface SetProvider {
    suspend fun getSets(): List<String>
}