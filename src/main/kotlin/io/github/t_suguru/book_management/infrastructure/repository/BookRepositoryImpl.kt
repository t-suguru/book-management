package io.github.t_suguru.book_management.infrastructure.repository

import io.github.t_suguru.book_management.db.tables.Authorships.Companion.AUTHORSHIPS
import io.github.t_suguru.book_management.db.tables.Books.Companion.BOOKS
import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.domain.model.PublicationStatus
import io.github.t_suguru.book_management.domain.repository.BookRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * 書籍リポジトリ実装クラス
 */
@Repository
class BookRepositoryImpl(
    private val dsl: DSLContext,
) : BookRepository {

    @Transactional
    override fun save(book: Book): Book {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        // 書籍情報を保存
        dsl.insertInto(BOOKS)
            .set(BOOKS.ID, id)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.STATUS_ID, book.status.id)
            .set(BOOKS.CREATED_AT, now)
            .set(BOOKS.UPDATED_AT, now)
            .execute()

        // 著者関係を保存
        book.authorIds.forEach { authorId ->
            dsl.insertInto(AUTHORSHIPS)
                .set(AUTHORSHIPS.BOOK_ID, id)
                .set(AUTHORSHIPS.AUTHOR_ID, authorId)
                .execute()
        }

        return book.copy(
            id = id,
            createdAt = now,
            updatedAt = now
        )
    }

    override fun findById(id: UUID): Book? {
        // 書籍情報を取得
        val bookRecord = dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne() ?: return null

        // 関連する著者IDを取得（順序を統一するためにソート）
        val authorIds = dsl.select(AUTHORSHIPS.AUTHOR_ID)
            .from(AUTHORSHIPS)
            .where(AUTHORSHIPS.BOOK_ID.eq(id))
            .orderBy(AUTHORSHIPS.AUTHOR_ID) // UUIDでソートして順序を統一
            .fetch(AUTHORSHIPS.AUTHOR_ID)
            .filterNotNull()

        return Book(
            id = bookRecord.id,
            title = bookRecord.title!!,
            price = bookRecord.price!!,
            status = PublicationStatus.fromId(bookRecord.statusId!!)!!,
            authorIds = authorIds,
            createdAt = bookRecord.createdAt,
            updatedAt = bookRecord.updatedAt
        )
    }

    @Transactional
    override fun update(book: Book): Book {
        val now = LocalDateTime.now()

        // 書籍情報を更新
        dsl.update(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.STATUS_ID, book.status.id)
            .set(BOOKS.UPDATED_AT, now)
            .where(BOOKS.ID.eq(book.id))
            .execute()

        // 既存の著者関係を削除
        dsl.deleteFrom(AUTHORSHIPS)
            .where(AUTHORSHIPS.BOOK_ID.eq(book.id))
            .execute()

        // 新しい著者関係を挿入
        book.authorIds.forEach { authorId ->
            dsl.insertInto(AUTHORSHIPS)
                .set(AUTHORSHIPS.BOOK_ID, book.id)
                .set(AUTHORSHIPS.AUTHOR_ID, authorId)
                .execute()
        }

        return book.copy(updatedAt = now)
    }
}
