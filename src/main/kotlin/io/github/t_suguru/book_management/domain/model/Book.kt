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
    val authors: List<Author>,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    init {
        require(price >= 0) { "価格は0以上である必要があります" }
        require(authors.isNotEmpty()) { "最低1人の著者が必要です" }
    }
    
    // 便利メソッド：著者IDのリストを取得
    val authorIds: List<UUID>
        get() = authors.mapNotNull { it.id }
    
    // 著者を名前でソートしたコピーを作成
    fun withSortedAuthors(): Book = this.copy(authors = authors.sortedBy { it.name })
}
