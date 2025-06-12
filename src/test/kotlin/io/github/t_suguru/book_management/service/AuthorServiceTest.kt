package io.github.t_suguru.book_management.service

import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.domain.repository.AuthorRepository
import io.github.t_suguru.book_management.dto.AuthorCreateRequest
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
 * AuthorServiceの単体テスト
 */
class AuthorServiceTest {

    private lateinit var authorRepository: AuthorRepository
    private lateinit var authorService: AuthorService

    @BeforeEach
    fun setUp() {
        authorRepository = mockk()
        authorService = AuthorService(authorRepository)
    }

    @Test
    fun `著者作成時に正しくRepositoryが呼ばれること`() {
        // Given
        val request = AuthorCreateRequest(
            name = "夏目漱石",
            birthdate = LocalDate.of(1867, 2, 9)
        )
        
        val expectedAuthor = Author(
            id = UUID.randomUUID(),
            name = "夏目漱石",
            birthdate = LocalDate.of(1867, 2, 9),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { authorRepository.save(any()) } returns expectedAuthor

        // When
        val result = authorService.createAuthor(request)

        // Then
        assertEquals(expectedAuthor, result)
        verify(exactly = 1) { 
            authorRepository.save(
                match { it.name == "夏目漱石" && it.birthdate == LocalDate.of(1867, 2, 9) }
            ) 
        }
    }

    @Test
    fun `存在する著者の更新が正常に動作すること`() {
        // Given
        val authorId = UUID.randomUUID()
        val request = AuthorCreateRequest(
            name = "夏目漱石（更新後）",
            birthdate = LocalDate.of(1867, 2, 9)
        )
        
        val existingAuthor = Author(
            id = authorId,
            name = "夏目漱石",
            birthdate = LocalDate.of(1867, 2, 9)
        )
        
        val updatedAuthor = existingAuthor.copy(
            name = "夏目漱石（更新後）",
            updatedAt = LocalDateTime.now()
        )

        every { authorRepository.findById(authorId) } returns existingAuthor
        every { authorRepository.update(any()) } returns updatedAuthor

        // When
        val result = authorService.updateAuthor(authorId, request)

        // Then
        assertNotNull(result)
        assertEquals("夏目漱石（更新後）", result?.name)
        verify(exactly = 1) { authorRepository.findById(authorId) }
        verify(exactly = 1) { authorRepository.update(any()) }
    }

    @Test
    fun `存在しない著者の更新時にnullが返されること`() {
        // Given
        val authorId = UUID.randomUUID()
        val request = AuthorCreateRequest(
            name = "存在しない著者",
            birthdate = LocalDate.of(1867, 2, 9)
        )

        every { authorRepository.findById(authorId) } returns null

        // When
        val result = authorService.updateAuthor(authorId, request)

        // Then
        assertNull(result)
        verify(exactly = 1) { authorRepository.findById(authorId) }
        verify(exactly = 0) { authorRepository.update(any()) }
    }
}
