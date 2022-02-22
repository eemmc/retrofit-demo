package com.hbb.data.net

import java.io.IOException

@Suppress("MemberVisibilityCanBePrivate")
class HttpException(
    override val message: String,
    val code: Int = 0
) : IOException(message) {
    override fun toString(): String {
        return "$message ($code)"
    }
}