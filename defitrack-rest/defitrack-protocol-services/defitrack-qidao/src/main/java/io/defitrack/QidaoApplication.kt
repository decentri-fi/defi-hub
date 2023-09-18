package io.defitrack

import io.defitrack.protocol.Company
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

class QidaoApplication(applicationContext: ApplicationContext) : ProtocolApplication(
    applicationContext
) {
    override fun getCompany(): Company {
        return Company.QIDAO
    }
}

fun main(args: Array<String>) {
    runApplication<QidaoApplication>(*args)
}