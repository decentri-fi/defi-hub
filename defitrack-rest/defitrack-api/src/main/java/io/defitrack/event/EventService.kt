package io.defitrack.event

interface EventService {
    fun publish(routeKey: String, event: Any)
}