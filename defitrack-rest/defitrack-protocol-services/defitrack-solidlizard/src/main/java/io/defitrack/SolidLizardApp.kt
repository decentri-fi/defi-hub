package io.defitrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class SolidLizardApp

fun main(args: Array<String>) {
    runApplication<SolidLizardApp>(*args)
}