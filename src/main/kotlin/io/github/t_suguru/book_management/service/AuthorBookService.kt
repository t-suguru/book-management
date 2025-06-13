package io.github.t_suguru.book_management.service

import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.domain.repository.AuthorRepository
import io.github.t_suguru.book_management.domain.repository.BookRepository
import org.springframework.stereotype.Service
import java.util.*

/**
 * 著者と書籍の統合サービス
 * AuthorとBookの境界を跨ぐ操作を担当
 */
@Service
class AuthorBookService(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository
) {
    
    /**
     * 著者の書籍一覧を取得する
     * 著者の存在チェックも行う
     */
    fun getBooksByAuthorId(authorId: UUID): List<Book> {
        // 著者の存在確認
        val author = authorRepository.findById(authorId) ?: return emptyList()
        
        // 書籍一覧を取得
        return bookRepository.findByAuthorId(authorId)
    }
}
