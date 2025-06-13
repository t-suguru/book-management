package io.github.t_suguru.book_management.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.t_suguru.book_management.AbstractIntegrationTest
import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.domain.model.PublicationStatus
import io.github.t_suguru.book_management.dto.BookCreateRequest
import io.github.t_suguru.book_management.dto.BookUpdateRequest
import io.github.t_suguru.book_management.infrastructure.repository.AuthorRepositoryImpl
import io.github.t_suguru.book_management.infrastructure.repository.BookRepositoryImpl
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

/**
 * 書籍APIの統合テスト
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 各テスト後にロールバックしてデータベース状態を元に戻す
class BookControllerTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var authorRepository: AuthorRepositoryImpl

    @Autowired
    private lateinit var bookRepository: BookRepositoryImpl

    @Nested
    inner class CreateBookTests {

        @Test
        fun `正常な書籍データで書籍を作成できること`() {
            // Given - 著者を先に作成
            val author = authorRepository.save(
                Author(
                    name = "夏目漱石",
                    birthdate = LocalDate.of(1867, 2, 9)
                )
            )

            val request = BookCreateRequest(
                title = "吾輩は猫である",
                price = 1500,
                status = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author.id!!)
            )

            // When & Then
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.title").value("吾輩は猫である"))
                .andExpect(jsonPath("$.price").value(1500))
                .andExpect(jsonPath("$.status").value("UNPUBLISHED"))
                .andExpect(jsonPath("$.authorIds[0]").value(author.id.toString()))
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.createdAt").isNotEmpty)
                .andExpect(jsonPath("$.updatedAt").isNotEmpty)
        }

        @Test
        fun `複数の著者を持つ書籍を作成できること`() {
            // Given - 複数の著者を作成
            val author1 = authorRepository.save(
                Author(
                    name = "著者1",
                    birthdate = LocalDate.of(1900, 1, 1)
                )
            )
            val author2 = authorRepository.save(
                Author(
                    name = "著者2",
                    birthdate = LocalDate.of(1900, 2, 2)
                )
            )

            val request = BookCreateRequest(
                title = "共著書籍",
                price = 2000,
                status = PublicationStatus.PUBLISHED,
                authorIds = listOf(author1.id!!, author2.id!!)
            )

            // When & Then
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.title").value("共著書籍"))
                .andExpect(jsonPath("$.authorIds").isArray)
                .andExpect(jsonPath("$.authorIds.length()").value(2))
        }

        @Test
        fun `タイトルが空の場合400エラーが返ること`() {
            // Given
            val author = authorRepository.save(
                Author(
                    name = "著者",
                    birthdate = LocalDate.of(1900, 1, 1)
                )
            )

            val request = BookCreateRequest(
                title = "",
                price = 1500,
                status = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author.id!!)
            )

            // When & Then
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `価格が負の値の場合400エラーが返ること`() {
            // Given
            val author = authorRepository.save(
                Author(
                    name = "著者",
                    birthdate = LocalDate.of(1900, 1, 1)
                )
            )

            val request = BookCreateRequest(
                title = "書籍",
                price = -100,
                status = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author.id!!)
            )

            // When & Then
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `著者が空の場合400エラーが返ること`() {
            // Given
            val request = BookCreateRequest(
                title = "書籍",
                price = 1500,
                status = PublicationStatus.UNPUBLISHED,
                authorIds = emptyList()
            )

            // When & Then
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    inner class UpdateBookTests {

        @Test
        fun `正常なデータで書籍を更新できること`() {
            // Given - 著者と書籍を先に作成
            val author = authorRepository.save(
                Author(
                    name = "芥川龍之介",
                    birthdate = LocalDate.of(1892, 3, 1)
                )
            )

            val book = bookRepository.save(
                Book(
                    title = "羅生門",
                    price = 800,
                    status = PublicationStatus.UNPUBLISHED,
                    authors = listOf(author)
                )
            )

            val request = BookUpdateRequest(
                title = "羅生門（改訂版）",
                price = 1000,
                status = PublicationStatus.PUBLISHED,
                authorIds = listOf(author.id!!)
            )

            // When & Then
            mockMvc.perform(
                put("/api/books/{id}", book.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("羅生門（改訂版）"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.id").value(book.id.toString()))
        }

        @Test
        fun `出版済みから未出版への変更時に400エラーが返ること`() {
            // Given - 出版済みの書籍を作成
            val author = authorRepository.save(
                Author(
                    name = "太宰治",
                    birthdate = LocalDate.of(1909, 6, 19)
                )
            )

            val book = bookRepository.save(
                Book(
                    title = "人間失格",
                    price = 1200,
                    status = PublicationStatus.PUBLISHED,
                    authors = listOf(author)
                )
            )

            val request = BookUpdateRequest(
                title = "人間失格",
                price = 1200,
                status = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author.id!!)
            )

            // When & Then
            mockMvc.perform(
                put("/api/books/{id}", book.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `存在しない書籍IDの場合404エラーが返ること`() {
            // Given
            val author = authorRepository.save(
                Author(
                    name = "著者",
                    birthdate = LocalDate.of(1900, 1, 1)
                )
            )

            val nonExistentId = UUID.randomUUID()
            val request = BookUpdateRequest(
                title = "書籍",
                price = 1500,
                status = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author.id!!)
            )

            // When & Then
            mockMvc.perform(
                put("/api/books/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
        }
    }
}
