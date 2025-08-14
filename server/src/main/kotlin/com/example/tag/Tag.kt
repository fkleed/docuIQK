package com.example.tag

import com.example.shared.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Tag (
    @Serializable(with = UUIDSerializer::class) var id: UUID?,
    val name: String,
)