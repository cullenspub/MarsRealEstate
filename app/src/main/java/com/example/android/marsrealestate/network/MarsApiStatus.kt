package com.example.android.marsrealestate.network

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}


data class MarsApiStatus<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T): MarsApiStatus<T> = MarsApiStatus(Status.SUCCESS, data, null)
        fun <T> error(data: T?, message: String): MarsApiStatus<T> = MarsApiStatus(Status.ERROR, data, message)
        fun <T> loading(data: T?): MarsApiStatus<T> = MarsApiStatus(Status.LOADING, data, null)
    }
}