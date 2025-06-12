package io.github.t_suguru.book_management.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * 著者ドメインモデル
 */
data class Author(
    val id: UUID? = null,
    val name: String,
    val birthdate: LocalDate,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
