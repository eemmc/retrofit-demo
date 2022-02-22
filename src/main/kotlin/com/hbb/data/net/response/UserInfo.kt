package com.hbb.data.net.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    @SerialName("id")
    var userId: Int,
    @SerialName("name")
    var name: String?,
)
