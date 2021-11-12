package io.codechef

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class BeefyApplication

fun main(args: Array<String>) {
    runApplication<BeefyApplication>(*args)
}