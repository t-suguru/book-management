package io.github.t_suguru.book_management.domain.model

import java.time.LocalDateTime
import java.util.*

/**
 * 書籍ドメインモデル
 */
data class Book(
    val id: UUID? = null,
    val title: String,
    val price: Int,
    val status: PublicationStatus,
    val authorIds: List<UUID>,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    init {
        require(price >= 0) { "価格は0以上である必要があります" }
        require(authorIds.isNotEmpty()) { "最低1人の著者が必要です" }
    }
    
    // 著者IDをソート済みの状態で保持するためのコピー関数
    fun withSortedAuthorIds(): Book = this.copy(authorIds = authorIds.sorted())
}
