package com.crimsonedge.studioadmin.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageResponse(val message: String)

@JsonClass(generateAdapter = true)
data class CreatedResponse(val id: Int)

@JsonClass(generateAdapter = true)
data class ErrorResponse(val error: String)

@JsonClass(generateAdapter = true)
data class BatchDeleteRequest(val ids: List<Int>)
