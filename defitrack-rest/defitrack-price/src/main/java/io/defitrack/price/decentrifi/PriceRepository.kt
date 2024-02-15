package io.defitrack.price.decentrifi

import org.springframework.scheduling.annotation.Scheduled

abstract class PriceRepository {
    abstract fun populate()

    open fun order() = 100
}