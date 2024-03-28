package io.defitrack.architecture.conditional

import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class OnCompanyCondition : SpringBootCondition() {
    override fun getMatchOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val attributes = metadata.getAnnotationAttributes(ConditionalOnCompany::class.java.getName())
        val company = attributes!!["value"] as Company


        val activeCompanies: List<String> = (context.environment.getProperty(
            "decentrifi.companies",
            List::class.java, emptyList<String>()
        ) as List<String>).map(String::lowercase)

        return if (activeCompanies.contains(company.slug.lowercase()) || activeCompanies.contains(company.name.lowercase())) {
            ConditionOutcome.match("Company $activeCompanies are active")
        } else {
            ConditionOutcome.noMatch("Company $activeCompanies are not active")
        }
    }
}