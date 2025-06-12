package io.github.t_suguru.book_management.dto

import io.github.t_suguru.book_management.domain.model.PublicationStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.*

/**
 * 書籍作成リクエスト
 */
data class BookCreateRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,
    
    @field:Min(value = 0, message = "価格は0以上である必要があります")
    val price: Int,
    
    @field:NotNull(message = "出版状況は必須です")
    val status: PublicationStatus,
    
    @field:NotEmpty(message = "最低1人の著者が必要です")
    val authorIds: List<UUID>
)
