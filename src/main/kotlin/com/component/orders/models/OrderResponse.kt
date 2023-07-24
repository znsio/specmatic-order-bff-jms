package com.component.orders.models

import com.fasterxml.jackson.annotation.JsonProperty

data class OrderResponse(
    @JsonProperty("id")
    val id: Int,

    @JsonProperty("status")
    val status: String
)