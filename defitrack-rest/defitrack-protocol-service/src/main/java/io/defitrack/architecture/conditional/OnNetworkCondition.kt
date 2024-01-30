package io.defitrack.architecture.conditional

import io.defitrack.common.network.Network
import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class OnNetworkCondition : SpringBootCondition() {
    override fun getMatchOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val attributes = metadata.getAnnotationAttributes(ConditionalOnNetwork::class.java.getName())
        val network = attributes!!["value"] as Network

        val activeNetworks: List<String> = (context.environment.getProperty(
            "decentrifi.networks",
            List::class.java
        ) as List<String>).map(String::lowercase)

        if (activeNetworks.isEmpty()) {
            throw IllegalArgumentException("decentrifi.networks is not set")
        }

        return if (activeNetworks.contains(network.slug.lowercase()) || activeNetworks.contains(network.name.lowercase())) {
            ConditionOutcome.match("Company $activeNetworks are active")
        } else {
            ConditionOutcome.noMatch("Company $activeNetworks are not active")
        }
    }
}