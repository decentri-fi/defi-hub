package io.defitrack.protocol.makerdao.domain

class Market(
    val id: String,
    val name: String,
    val rates: List<Rate>
)

class Rate(
    val id: String,
    val rate: String,
    val type: String
)
