package io.github.t_suguru.book_management.domain.repository

import io.github.t_suguru.book_management.domain.model.Book
import java.util.*

/**
 * 書籍用リポジトリインターフェース
 */
interface BookRepository {
    fun save(book: Book): Book
    fun findById(id: UUID): Book?
    fun update(book: Book): Book
    fun findByAuthorId(authorId: UUID): List<Book>
}
