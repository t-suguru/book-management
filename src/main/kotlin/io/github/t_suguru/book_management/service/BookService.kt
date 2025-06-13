package io.github.t_suguru.book_management.service

import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.domain.model.PublicationStatus
import io.github.t_suguru.book_management.domain.repository.AuthorRepository
import io.github.t_suguru.book_management.domain.repository.BookRepository
import io.github.t_suguru.book_management.dto.BookCreateRequest
import io.github.t_suguru.book_management.dto.BookUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 書籍ビジネスロジック
 */
@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {
    /**
     * 書籍を作成する
     */
    @Transactional
    fun createBook(request: BookCreateRequest): Book {
        // 著者の存在チェックと取得
        val authors = request.authorIds.map { authorId ->
            authorRepository.findById(authorId)
                ?: throw IllegalArgumentException("著者が見つかりません: $authorId")
        }

        val book = Book(
            title = request.title,
            price = request.price,
            status = request.status,
            authors = authors
        )
        return bookRepository.save(book)
    }

    /**
     * 書籍を更新する
     */
    @Transactional
    fun updateBook(id: UUID, request: BookUpdateRequest): Book? {
        val existingBook = bookRepository.findById(id) ?: return null

        // 出版済み→未出版への変更は許可しない
        // ルールが増えるようならばドメイン層に移す
        if (existingBook.status == PublicationStatus.PUBLISHED &&
            request.status == PublicationStatus.UNPUBLISHED
        ) {
            throw IllegalArgumentException("出版済みの書籍を未出版に変更することはできません")
        }

        // 著者の存在チェックと取得
        val authors = request.authorIds.map { authorId ->
            authorRepository.findById(authorId)
                ?: throw IllegalArgumentException("著者が見つかりません: $authorId")
        }

        val updatedBook = existingBook.copy(
            title = request.title,
            price = request.price,
            status = request.status,
            authors = authors
        )

        return bookRepository.update(updatedBook)
    }
}
