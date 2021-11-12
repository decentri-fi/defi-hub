package io.codechef

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class DfynApplication

fun main(args: Array<String>) {
    runApplication<DfynApplication>(*args)
}