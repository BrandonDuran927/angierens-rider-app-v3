package com.brandon.angierens_rider.core

sealed class CustomResult<out T> {
    data class Success<out T>(val data: T) : CustomResult<T>()
    data class Failure(val exception: Throwable) : CustomResult<Nothing>()

    // Helpers
    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure


    fun getOrNull() : T? = (this as? Success)?.data
    fun exceptionOrNull(): Throwable? = (this as? Failure)?.exception
}