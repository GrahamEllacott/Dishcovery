package com.example.dishcovery.util

/**
 * Sealed class for input validation results
 * Implements the Sealed Class pattern to represent different validation results
 */
sealed class ValidationResult {
    /**
     * Represents a successful validation with no errors
     */
    object Valid : ValidationResult()

    /**
     * Represents a failed validation with an error message
     * @param errorMessage The error message to display to the user
     */
    data class Invalid(val errorMessage: String) : ValidationResult()
}

/**
 * Extension function to check if validation result is valid
 */
fun ValidationResult.isValid(): Boolean = this is ValidationResult.Valid

/**
 * Extension function to get error message if invalid, null otherwise
 */
fun ValidationResult.getErrorOrNull(): String? = when (this) {
    is ValidationResult.Valid -> null
    is ValidationResult.Invalid -> errorMessage
}
