package io.github.t_suguru.book_management.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate

/**
 * 著者作成リクエスト
 */
data class AuthorCreateRequest(
    @field:NotBlank(message = "名前は必須です")
    val name: String,
    
    @field:Past(message = "生年月日は現在の日付より過去である必要があります")
    val birthdate: LocalDate
)
