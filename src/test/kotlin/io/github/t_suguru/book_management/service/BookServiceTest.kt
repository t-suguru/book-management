package io.github.t_suguru.book_management.service

import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.domain.model.PublicationStatus
import io.github.t_suguru.book_management.domain.repository.AuthorRepository
import io.github.t_suguru.book_management.domain.repository.BookRepository
import io.github.t_suguru.book_management.dto.BookCreateRequest
import io.github.t_suguru.book_management.dto.BookUpdateRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * BookServiceの単体テスト
 */
class BookServiceTest {
    private lateinit var bookRepository: BookRepository
    private lateinit var authorRepository: AuthorRepository
    private lateinit var bookService: BookService

    @BeforeEach
    fun setUp() {
        bookRepository = mockk()
        authorRepository = mockk()
        bookService = BookService(bookRepository, authorRepository)
    }

    @Test
    fun `書籍作成時に正しくRepositoryが呼ばれること`() {
        // Given
        val authorId = UUID.randomUUID()
        val author = Author(
            id = authorId,
            name = "夏目漱石",
            birthdate = LocalDate.of(1867, 2, 9),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val request = BookCreateRequest(
            title = "吾輩は猫である",
            price = 1500,
            status = PublicationStatus.UNPUBLISHED,
            authorIds = listOf(authorId)
        )

        val expectedBook = Book(
            id = UUID.randomUUID(),
            title = "吾輩は猫である",
            price = 1500,
            status = PublicationStatus.UNPUBLISHED,
            authors = listOf(author),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { authorRepository.findById(authorId) } returns author
        every { bookRepository.save(any()) } returns expectedBook

        // When
        val result = bookService.createBook(request)

        // Then
        assertEquals(expectedBook, result)
        verify(exactly = 1) { authorRepository.findById(authorId) }
        verify(exactly = 1) { bookRepository.save(any()) }
    }

    @Test
    fun `書籍更新時に正しくRepositoryが呼ばれること`() {
        // Given
        val bookId = UUID.randomUUID()
        val authorId = UUID.randomUUID()
        val existingAuthor = Author(
            id = authorId,
            name = "夏目漱石",
            birthdate = LocalDate.of(1867, 2, 9),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val existingBook = Book(
            id = bookId,
            title = "旧タイトル",
            price = 1000,
            status = PublicationStatus.UNPUBLISHED,
            authors = listOf(existingAuthor),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val request = BookUpdateRequest(
            title = "新タイトル",
            price = 2000,
            status = PublicationStatus.PUBLISHED,
            authorIds = listOf(authorId)
        )

        val expectedBook = existingBook.copy(
            title = "新タイトル",
            price = 2000,
            status = PublicationStatus.PUBLISHED,
            updatedAt = LocalDateTime.now()
        )

        every { authorRepository.findById(authorId) } returns existingAuthor
        every { bookRepository.findById(bookId) } returns expectedBook
        every { bookRepository.update(any()) } returns expectedBook

        // When
        val result = bookService.updateBook(bookId, request)

        // Then
        assertEquals(expectedBook, result)
        verify(exactly = 1) { bookRepository.findById(bookId) }
        verify(exactly = 1) { bookRepository.update(any()) }
    }

    @Test
    fun `存在しない書籍を更新しようとした場合nullが返ること`() {
        // Given
        val bookId = UUID.randomUUID()
        val authorId = UUID.randomUUID()

        val request = BookUpdateRequest(
            title = "新タイトル",
            price = 2000,
            status = PublicationStatus.PUBLISHED,
            authorIds = listOf(authorId)
        )

        every { bookRepository.findById(bookId) } returns null

        // When
        val result = bookService.updateBook(bookId, request)

        // Then
        assertNull(result)
        verify(exactly = 1) { bookRepository.findById(bookId) }
        verify(exactly = 0) { bookRepository.update(any()) }
    }

    @Test
    fun `出版済みから未出版への変更時に例外が発生すること`() {
        // Given
        val bookId = UUID.randomUUID()
        val authorId = UUID.randomUUID()
        val existingAuthor = Author(
            id = UUID.randomUUID(),
            name = "夏目漱石",
            birthdate = LocalDate.of(1867, 2, 9),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val existingBook = Book(
            id = bookId,
            title = "出版済み書籍",
            price = 1000,
            status = PublicationStatus.PUBLISHED,
            authors = listOf(existingAuthor),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val request = BookUpdateRequest(
            title = "出版済み書籍",
            price = 1000,
            status = PublicationStatus.UNPUBLISHED,
            authorIds = listOf(authorId)
        )

        every { bookRepository.findById(bookId) } returns existingBook

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            bookService.updateBook(bookId, request)
        }
        assertEquals("出版済みの書籍を未出版に変更することはできません", exception.message)

        verify(exactly = 1) { bookRepository.findById(bookId) }
        verify(exactly = 0) { bookRepository.update(any()) }
    }
}
