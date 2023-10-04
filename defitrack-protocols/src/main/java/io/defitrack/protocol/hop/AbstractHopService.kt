package io.defitrack.protocol.hop

import io.defitrack.common.network.Network

interface AbstractHopService {
    fun getStakingRewards(): List<String>
    fun getNetwork(): Network
    fun getGraph(): String
}