package io.defitrack.protocol

import io.defitrack.common.network.Network

interface AbstractHopService {

    fun getNetwork(): Network
    fun getGraph(): String
}