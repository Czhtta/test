package com.comp5348.banktransaction.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.PRECONDITION_FAILED,
        reason = "Account has insufficient balance to perform the transaction.")
public class InsufficientBalanceException extends RuntimeException {
}
