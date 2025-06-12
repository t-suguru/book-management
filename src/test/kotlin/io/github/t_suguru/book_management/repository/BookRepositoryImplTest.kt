package io.github.t_suguru.book_management.repository

import io.github.t_suguru.book_management.AbstractIntegrationTest
import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.domain.model.PublicationStatus
import io.github.t_suguru.book_management.infrastructure.repository.AuthorRepositoryImpl
import io.github.t_suguru.book_management.infrastructure.repository.BookRepositoryImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

/**
 * BookRepositoryImplの統合テスト
 */
@SpringBootTest
@Transactional
class BookRepositoryImplTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var bookRepository: BookRepositoryImpl

    @Autowired
    private lateinit var authorRepository: AuthorRepositoryImpl

    @Test
    fun `書籍を保存してIDが採番された状態で返ってくること`() {
        // Given - 著者を先に作成
        val author = authorRepository.save(
            Author(
                name = "夏目漱石",
                birthdate = LocalDate.of(1867, 2, 9)
            )
        )
        
        val book = Book(
            title = "吾輩は猫である",
            price = 1500,
            status = PublicationStatus.UNPUBLISHED,
            authorIds = listOf(author.id!!)
        )

        // When
        val savedBook = bookRepository.save(book)

        // Then
        assertNotNull(savedBook.id)
        assertEquals("吾輩は猫である", savedBook.title)
        assertEquals(1500, savedBook.price)
        assertEquals(PublicationStatus.UNPUBLISHED, savedBook.status)
        assertEquals(listOf(author.id), savedBook.authorIds)
        assertNotNull(savedBook.createdAt)
        assertNotNull(savedBook.updatedAt)
    }

    @Test
    fun `書籍をIDで取得できること`() {
        // Given - 著者と書籍を先に作成
        val author = authorRepository.save(
            Author(
                name = "芥川龍之介",
                birthdate = LocalDate.of(1892, 3, 1)
            )
        )
        
        val savedBook = bookRepository.save(
            Book(
                title = "羅生門",
                price = 800,
                status = PublicationStatus.PUBLISHED,
                authorIds = listOf(author.id!!)
            )
        )

        // When
        val foundBook = bookRepository.findById(savedBook.id!!)

        // Then
        assertNotNull(foundBook)
        assertEquals(savedBook.id, foundBook!!.id)
        assertEquals("羅生門", foundBook.title)
        assertEquals(800, foundBook.price)
        assertEquals(PublicationStatus.PUBLISHED, foundBook.status)
        assertEquals(listOf(author.id), foundBook.authorIds)
    }

    @Test
    fun `存在しないIDで検索した場合nullが返ること`() {
        // Given
        val nonExistentId = UUID.randomUUID()

        // When
        val foundBook = bookRepository.findById(nonExistentId)

        // Then
        assertNull(foundBook)
    }

    @Test
    fun `書籍を更新できること`() {
        // Given - 著者と書籍を先に作成
        val author1 = authorRepository.save(
            Author(
                name = "太宰治",
                birthdate = LocalDate.of(1909, 6, 19)
            )
        )
        
        val author2 = authorRepository.save(
            Author(
                name = "川端康成",
                birthdate = LocalDate.of(1899, 6, 14)
            )
        )
        
        val savedBook = bookRepository.save(
            Book(
                title = "人間失格",
                price = 1200,
                status = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author1.id!!)
            )
        )

        // When - 書籍情報を更新
        val updatedBook = savedBook.copy(
            title = "人間失格（改訂版）",
            price = 1500,
            status = PublicationStatus.PUBLISHED,
            authorIds = listOf(author1.id!!, author2.id!!)
        )
        val result = bookRepository.update(updatedBook)

        // Then
        assertEquals("人間失格（改訂版）", result.title)
        assertEquals(1500, result.price)
        assertEquals(PublicationStatus.PUBLISHED, result.status)
        assertEquals(2, result.authorIds.size)
        assertTrue(result.authorIds.contains(author1.id))
        assertTrue(result.authorIds.contains(author2.id))
        assertNotNull(result.updatedAt)

        // データベースからも確認
        val foundBook = bookRepository.findById(savedBook.id!!)
        assertNotNull(foundBook)
        assertEquals(result.id, foundBook!!.id)
        assertEquals(result.title, foundBook.title)
        assertEquals(result.price, foundBook.price)
        assertEquals(result.status, foundBook.status)
        assertEquals(result.authorIds.size, foundBook.authorIds.size)
        assertTrue(foundBook.authorIds.containsAll(result.authorIds))
        // updatedAtは時刻の微細な差があるかもしれないので、非null確認のみ
        assertNotNull(foundBook.updatedAt)
    }

    @Test
    fun `複数の著者を持つ書籍を保存・取得できること`() {
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
        
        val author3 = authorRepository.save(
            Author(
                name = "著者3",
                birthdate = LocalDate.of(1900, 3, 3)
            )
        )

        val book = Book(
            title = "共著書籍",
            price = 2000,
            status = PublicationStatus.PUBLISHED,
            authorIds = listOf(author1.id!!, author2.id!!, author3.id!!)
        )

        // When
        val savedBook = bookRepository.save(book)
        val foundBook = bookRepository.findById(savedBook.id!!)

        // Then
        assertNotNull(foundBook)
        assertEquals(3, foundBook!!.authorIds.size)
        assertTrue(foundBook.authorIds.contains(author1.id))
        assertTrue(foundBook.authorIds.contains(author2.id))
        assertTrue(foundBook.authorIds.contains(author3.id))
    }

    @Test
    fun `著者IDで書籍一覧を取得できること`() {
        // Given - 著者と複数の書籍を作成
        val author = authorRepository.save(
            Author(
                name = "宮沢賢治",
                birthdate = LocalDate.of(1896, 8, 27)
            )
        )
        
        val anotherAuthor = authorRepository.save(
            Author(
                name = "別の著者",
                birthdate = LocalDate.of(1900, 1, 1)
            )
        )

        val book1 = bookRepository.save(
            Book(
                title = "銀河鉄道の夜",
                price = 1000,
                status = PublicationStatus.PUBLISHED,
                authorIds = listOf(author.id!!)
            )
        )
        
        val book2 = bookRepository.save(
            Book(
                title = "注文の多い料理店",
                price = 800,
                status = PublicationStatus.PUBLISHED,
                authorIds = listOf(author.id!!)
            )
        )
        
        // 別の著者の書籍も作成（検索結果に含まれないことを確認するため）
        bookRepository.save(
            Book(
                title = "別の書籍",
                price = 1200,
                status = PublicationStatus.PUBLISHED,
                authorIds = listOf(anotherAuthor.id!!)
            )
        )

        // When
        val foundBooks = bookRepository.findByAuthorId(author.id!!)

        // Then
        assertEquals(2, foundBooks.size)
        
        // 特定の書籍が含まれていることを確認
        val foundBook1 = foundBooks.find { it.id == book1.id }
        val foundBook2 = foundBooks.find { it.id == book2.id }
        
        assertNotNull(foundBook1)
        assertNotNull(foundBook2)
        assertEquals("銀河鉄道の夜", foundBook1!!.title)
        assertEquals("注文の多い料理店", foundBook2!!.title)
        
        // 別の著者の書籍は含まれていないことを確認
        assertFalse(foundBooks.any { it.title == "別の書籍" })
    }

    @Test
    fun `存在しない著者IDで検索した場合空のリストが返ること`() {
        // Given
        val nonExistentAuthorId = UUID.randomUUID()

        // When
        val foundBooks = bookRepository.findByAuthorId(nonExistentAuthorId)

        // Then
        assertTrue(foundBooks.isEmpty())
    }
}
