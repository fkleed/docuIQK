package com.example.collection

import com.example.shared.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DocumentCollection(
    @Serializable(with = UUIDSerializer::class) var id: UUID?,
    val name: String,
    val description: String
)