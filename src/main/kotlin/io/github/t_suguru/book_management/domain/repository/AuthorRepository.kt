package io.github.t_suguru.book_management.domain.repository

import io.github.t_suguru.book_management.domain.model.Author
import java.util.*

/**
 * 著者用リポジトリインターフェース
 */
interface AuthorRepository {
    fun save(author: Author): Author
    fun findById(id: UUID): Author?
    fun update(author: Author): Author
}
