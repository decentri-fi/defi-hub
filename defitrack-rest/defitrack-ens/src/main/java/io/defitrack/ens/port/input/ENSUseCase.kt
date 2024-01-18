package io.defitrack.ens.port.input

import java.math.BigInteger

interface ENSUseCase {
    suspend fun getAvatar(name: String): String
    suspend fun getEnsByName(name: String): String
    suspend fun getEnsByAddress(address: String): String
    suspend fun getExpires(ensName: String): BigInteger
}