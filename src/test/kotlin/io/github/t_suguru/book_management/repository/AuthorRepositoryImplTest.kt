package io.github.t_suguru.book_management.repository

import io.github.t_suguru.book_management.AbstractIntegrationTest
import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.infrastructure.repository.AuthorRepositoryImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

/**
 * AuthorRepositoryImplの統合テスト
 */
@SpringBootTest
@Transactional
class AuthorRepositoryImplTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var authorRepository: AuthorRepositoryImpl

    @Test
    fun `著者を保存してIDが採番された状態で返ってこと`() {
        // Given
        val author = Author(
            name = "夏目漱石",
            birthdate = LocalDate.of(1867, 2, 9)
        )

        // When
        val savedAuthor = authorRepository.save(author)

        // Then
        assertNotNull(savedAuthor.id)
        assertEquals("夏目漱石", savedAuthor.name)
        assertEquals(LocalDate.of(1867, 2, 9), savedAuthor.birthdate)
        assertNotNull(savedAuthor.createdAt)
        assertNotNull(savedAuthor.updatedAt)
    }

    @Test
    fun `IDで著者を取得できること`() {
        // Given
        val author = Author(
            name = "芥川龍之介",
            birthdate = LocalDate.of(1892, 3, 1)
        )
        val savedAuthor = authorRepository.save(author)

        // When
        val foundAuthor = authorRepository.findById(savedAuthor.id!!)

        // Then
        assertNotNull(foundAuthor)
        assertEquals(savedAuthor.id, foundAuthor?.id)
        assertEquals("芥川龍之介", foundAuthor?.name)
        assertEquals(LocalDate.of(1892, 3, 1), foundAuthor?.birthdate)
    }

    @Test
    fun `存在しないIDで取得時にnullが返されること`() {
        // Given
        val nonExistentId = UUID.randomUUID()

        // When
        val foundAuthor = authorRepository.findById(nonExistentId)

        // Then
        assertNull(foundAuthor)
    }

    @Test
    fun `著者情報を更新できること`() {
        // Given
        val author = Author(
            name = "三島由紀夫",
            birthdate = LocalDate.of(1925, 1, 14)
        )
        val savedAuthor = authorRepository.save(author)

        // When
        val updatedAuthor = savedAuthor.copy(
            name = "三島由紀夫（更新後）",
            birthdate = LocalDate.of(1925, 1, 14)
        )
        val result = authorRepository.update(updatedAuthor)

        // Then
        assertEquals("三島由紀夫（更新後）", result.name)
        assertEquals(savedAuthor.id, result.id)
        assertNotEquals(savedAuthor.updatedAt, result.updatedAt)

        // データベースからも確認
        val foundAuthor = authorRepository.findById(savedAuthor.id!!)
        assertEquals("三島由紀夫（更新後）", foundAuthor?.name)
    }
}
