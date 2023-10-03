package io.defitrack.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionControllerAdvice {

    @ExceptionHandler(value = [TransactionPreparationException::class])
    fun handleTransactionPreparation(investException: TransactionPreparationException): ResponseEntity<ExceptionResult> {
        return ResponseEntity.badRequest().body(
            ExceptionResult(
                investException.msg
            )
        )
    }
}