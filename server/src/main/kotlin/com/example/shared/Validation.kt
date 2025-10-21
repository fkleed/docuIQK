package com.example.shared

sealed class Validated<T> {
    abstract fun getOrThrowIfInvalid(): T

    class Valid<T>(val value: T) : Validated<T>() {
        override fun getOrThrowIfInvalid() = value
    }

    class Invalid<T>(val errors: Set<String>) : Validated<T>() {
        override fun getOrThrowIfInvalid() = throw IllegalArgumentException(errors.joinToString("\n"))
    }
}