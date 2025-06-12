package io.github.t_suguru.book_management.domain.model

/**
 * 出版状況
 */
enum class PublicationStatus(
    val id: Int,
    val displayName: String
) {
    UNPUBLISHED(1, "未出版"),
    PUBLISHED(2, "出版済み");

    companion object {
        fun fromId(id: Int): PublicationStatus? {
            return values().find { it.id == id }
        }
    }
}
