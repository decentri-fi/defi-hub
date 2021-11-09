package io.defitrack.abi


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.defitrack.abi.domain.AbiContract
import io.defitrack.abi.domain.AbiContractElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AbiDecoder {

    private val mapper: ObjectMapper = ObjectMapper()
    val log: Logger = LoggerFactory.getLogger(AbiDecoder::class.java)

    init {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        this.mapper.registerModule(KotlinModule())
    }

    fun decode(json: String): AbiContract {
        return try {
            AbiContract(mapper.readValue<List<AbiContractElement>>(json, object : TypeReference<List<AbiContractElement>>() {

            }))
        } catch (ex: Exception) {
            ex.printStackTrace()
            log.info("Unable to decode abi-json: ", ex)
            log.debug("JSON: {}", json)
            throw IllegalArgumentException("Unable to decode json", ex)
        }
    }
}
