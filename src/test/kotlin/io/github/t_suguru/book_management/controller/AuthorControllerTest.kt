package io.github.t_suguru.book_management.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.t_suguru.book_management.AbstractIntegrationTest
import io.github.t_suguru.book_management.dto.AuthorCreateRequest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

/**
 * 著者APIの統合テスト
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 各テスト後にロールバックしてデータベース状態を元に戻す
class AuthorControllerTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Nested
    inner class CreateAuthorTests {

        @Test
        fun `正常な著者データで著者を作成できること`() {
            // Given
            val request = AuthorCreateRequest(
                name = "夏目漱石",
                birthdate = LocalDate.of(1867, 2, 9)
            )

            // When & Then
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.name").value("夏目漱石"))
                .andExpect(jsonPath("$.birthdate").value("1867-02-09"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isString)
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
        }

        @Test
        fun `空の名前でバリデーションエラーが発生すること`() {
            // Given
            val request = AuthorCreateRequest(
                name = "",
                birthdate = LocalDate.of(1867, 2, 9)
            )

            // When & Then
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `空白のみの名前でバリデーションエラーが発生すること`() {
            // Given
            val request = AuthorCreateRequest(
                name = "   ",
                birthdate = LocalDate.of(1867, 2, 9)
            )

            // When & Then
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `未来の生年月日でバリデーションエラーが発生すること`() {
            // Given
            val request = AuthorCreateRequest(
                name = "未来人",
                birthdate = LocalDate.now().plusDays(1)
            )

            // When & Then
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `今日の日付の生年月日でバリデーションエラーが発生すること`() {
            // Given
            val request = AuthorCreateRequest(
                name = "今日生まれ",
                birthdate = LocalDate.now()
            )

            // When & Then
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `不正な日付フォーマットでエラーが発生すること`() {
            // Given
            val invalidDateJson = """{"name": "夏目漱石", "birthdate": "invalid-date"}"""

            // When & Then
            mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidDateJson)
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `Content-Typeが指定されていない場合エラーが発生すること`() {
            // Given
            val request = AuthorCreateRequest(
                name = "夏目漱石",
                birthdate = LocalDate.of(1867, 2, 9)
            )

            // When & Then
            mockMvc.perform(
                post("/api/authors")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnsupportedMediaType)
        }
    }

    @Nested
    inner class UpdateAuthorTests {

        @Test
        fun `存在する著者を正常に更新できること`() {
            // Given
            val createRequest = AuthorCreateRequest(
                name = "夏目漱石",
                birthdate = LocalDate.of(1867, 2, 9)
            )

            val createResult = mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            )
                .andExpect(status().isCreated)
                .andReturn()

            val createResponse = createResult.response.contentAsString
            val createdAuthor = objectMapper.readTree(createResponse)
            val authorId = createdAuthor.get("id").asText()

            val updateRequest = AuthorCreateRequest(
                name = "夏目漱石（ペンネーム）",
                birthdate = LocalDate.of(1867, 2, 9)
            )

            // When & Then
            mockMvc.perform(
                put("/api/authors/$authorId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(authorId))
                .andExpect(jsonPath("$.name").value("夏目漱石（ペンネーム）"))
                .andExpect(jsonPath("$.birthdate").value("1867-02-09"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
        }

        @Test
        fun `存在しない著者IDで404エラーが発生すること`() {
            // Given
            val nonExistentId = UUID.randomUUID()
            val updateRequest = AuthorCreateRequest(
                name = "存在しない著者",
                birthdate = LocalDate.of(1867, 2, 9)
            )

            // When & Then
            mockMvc.perform(
                put("/api/authors/$nonExistentId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `無効なUUID形式で400エラーが発生すること`() {
            // Given
            val invalidId = "invalid-uuid"
            val updateRequest = AuthorCreateRequest(
                name = "夏目漱石",
                birthdate = LocalDate.of(1867, 2, 9)
            )

            // When & Then
            mockMvc.perform(
                put("/api/authors/$invalidId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `更新時のバリデーションエラーが正常に動作すること`() {
            // Given
            val createRequest = AuthorCreateRequest(
                name = "夏目漱石",
                birthdate = LocalDate.of(1867, 2, 9)
            )

            val createResult = mockMvc.perform(
                post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            )
                .andExpect(status().isCreated)
                .andReturn()

            val createResponse = createResult.response.contentAsString
            val createdAuthor = objectMapper.readTree(createResponse)
            val authorId = createdAuthor.get("id").asText()

            val invalidUpdateRequest = AuthorCreateRequest(
                name = "",
                birthdate = LocalDate.now().plusDays(1)
            )

            // When & Then
            mockMvc.perform(
                put("/api/authors/$authorId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidUpdateRequest))
            )
                .andExpect(status().isBadRequest)
        }
    }
}
