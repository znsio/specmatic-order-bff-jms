package com.component.orders.models.messages

import java.io.Serializable

data class ProductMessage(val id: Int, val name: String, val inventory: Int, val categories: List<ProductCategory> = emptyList()) : Serializable

