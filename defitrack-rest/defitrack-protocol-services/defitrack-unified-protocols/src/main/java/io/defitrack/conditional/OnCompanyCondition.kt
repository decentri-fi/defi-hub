package io.defitrack.conditional

import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata
import java.lang.IllegalArgumentException

class OnCompanyCondition : SpringBootCondition() {
    override fun getMatchOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val attributes = metadata.getAnnotationAttributes(ConditionalOnCompany::class.java.getName())
        val company = attributes!!["value"] as Company

        val activeCompany = context.environment.getProperty("decentrifi.company")

        if (activeCompany == null) {
            throw IllegalArgumentException("decentrifi.company is not set")
        }

        return if (company.slug.lowercase() == activeCompany.lowercase() || company.name.lowercase() == activeCompany.lowercase()) {
            ConditionOutcome.match("Company $activeCompany is active")
        } else {
            ConditionOutcome.noMatch("Company $activeCompany is not active")
        }
    }
}