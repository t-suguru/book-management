package io.github.t_suguru.book_management.service

import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.domain.model.PublicationStatus
import io.github.t_suguru.book_management.domain.repository.AuthorRepository
import io.github.t_suguru.book_management.domain.repository.BookRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * AuthorBookServiceの単体テスト
 */
class AuthorBookServiceTest {

    private lateinit var authorRepository: AuthorRepository
    private lateinit var bookRepository: BookRepository
    private lateinit var authorBookService: AuthorBookService

    @BeforeEach
    fun setUp() {
        authorRepository = mockk()
        bookRepository = mockk()
        authorBookService = AuthorBookService(authorRepository, bookRepository)
    }

    @Test
    fun `著者が存在する場合に書籍一覧を取得できること`() {
        // Given
        val authorId = UUID.randomUUID()

        val author = Author(
            id = authorId,
            name = "夏目漱石",
            birthdate = LocalDate.of(1867, 2, 9),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val expectedBooks = listOf(
            Book(
                id = UUID.randomUUID(),
                title = "吾輩は猫である",
                price = 1500,
                status = PublicationStatus.PUBLISHED,
                authors = listOf(author),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Book(
                id = UUID.randomUUID(),
                title = "坊っちゃん",
                price = 1200,
                status = PublicationStatus.PUBLISHED,
                authors = listOf(author),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        every { authorRepository.findById(authorId) } returns author
        every { bookRepository.findByAuthorId(authorId) } returns expectedBooks

        // When
        val result = authorBookService.getBooksByAuthorId(authorId)

        // Then
        assertEquals(expectedBooks, result)
        verify(exactly = 1) { authorRepository.findById(authorId) }
        verify(exactly = 1) { bookRepository.findByAuthorId(authorId) }
    }

    @Test
    fun `著者が存在しない場合に空のリストが返ること`() {
        // Given
        val nonExistentAuthorId = UUID.randomUUID()

        every { authorRepository.findById(nonExistentAuthorId) } returns null

        // When
        val result = authorBookService.getBooksByAuthorId(nonExistentAuthorId)

        // Then
        assertTrue(result.isEmpty())
        verify(exactly = 1) { authorRepository.findById(nonExistentAuthorId) }
        verify(exactly = 0) { bookRepository.findByAuthorId(any()) }
    }

    @Test
    fun `著者は存在するが書籍がない場合に空のリストが返ること`() {
        // Given
        val authorId = UUID.randomUUID()

        val author = Author(
            id = authorId,
            name = "新人作家",
            birthdate = LocalDate.of(1990, 1, 1),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { authorRepository.findById(authorId) } returns author
        every { bookRepository.findByAuthorId(authorId) } returns emptyList()

        // When
        val result = authorBookService.getBooksByAuthorId(authorId)

        // Then
        assertTrue(result.isEmpty())
        verify(exactly = 1) { authorRepository.findById(authorId) }
        verify(exactly = 1) { bookRepository.findByAuthorId(authorId) }
    }
}
