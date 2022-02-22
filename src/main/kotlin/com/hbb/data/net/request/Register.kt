package com.hbb.data.net.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Register(
    @SerialName("mobile")
    var mobile: String,
    @SerialName("password")
    var password: String,
    @SerialName("code")
    var code: String
)