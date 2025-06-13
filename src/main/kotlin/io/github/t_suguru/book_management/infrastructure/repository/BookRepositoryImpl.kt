package io.github.t_suguru.book_management.infrastructure.repository

import io.github.t_suguru.book_management.db.tables.Authors.Companion.AUTHORS
import io.github.t_suguru.book_management.db.tables.Authorships.Companion.AUTHORSHIPS
import io.github.t_suguru.book_management.db.tables.Books.Companion.BOOKS
import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.domain.model.PublicationStatus
import io.github.t_suguru.book_management.domain.repository.BookRepository
import org.jooq.DSLContext
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import io.github.t_suguru.book_management.db.tables.pojos.Authors as AuthorPojo
import io.github.t_suguru.book_management.db.tables.pojos.Books as BookPojo

/**
 * 書籍リポジトリ実装クラス
 */
@Repository
class BookRepositoryImpl(
    private val dsl: DSLContext,
) : BookRepository {

    @Transactional
    override fun save(book: Book): Book {
        // 書籍情報を保存し、挿入されたレコードを取得
        val insertedRecord = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.STATUS_ID, book.status.id)
            .returning()
            .fetchOne()!!

        // 著者関係を保存
        if (book.authorIds.isNotEmpty()) {
            val authorships = book.authorIds.map { authorId ->
                dsl.insertInto(AUTHORSHIPS)
                    .set(AUTHORSHIPS.BOOK_ID, insertedRecord.id)
                    .set(AUTHORSHIPS.AUTHOR_ID, authorId)
            }
            dsl.batch(authorships).execute()
        }

        return book.copy(
            id = insertedRecord.id,
            createdAt = insertedRecord.createdAt,
            updatedAt = insertedRecord.updatedAt
        )
    }

    override fun findById(id: UUID): Book? {
        // multisetを使って1つのクエリで書籍と著者を取得
        val result = dsl.select(
            BOOKS.asterisk(),
            // multisetで関連する著者を取得
            multiset(
                select(AUTHORS.asterisk())
                    .from(AUTHORS)
                    .join(AUTHORSHIPS).on(AUTHORS.ID.eq(AUTHORSHIPS.AUTHOR_ID))
                    .where(AUTHORSHIPS.BOOK_ID.eq(BOOKS.ID))
                    .orderBy(AUTHORS.NAME)
            ).`as`("authors").convertFrom { it.into(AuthorPojo::class.java) }
        )
            .from(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne() ?: return null

        val bookPojo = result.into(BOOKS).into(BookPojo::class.java)
        val authorPojos = (result.getValue("authors") as? List<*>)?.filterIsInstance<AuthorPojo>() ?: emptyList()
        
        return Book(
            id = bookPojo.id,
            title = bookPojo.title!!,
            price = bookPojo.price!!,
            status = PublicationStatus.fromId(bookPojo.statusId!!)!!,
            authors = authorPojos.map { authorPojo ->
                Author(
                    id = authorPojo.id,
                    name = authorPojo.name!!,
                    birthdate = authorPojo.birthdate!!,
                    createdAt = authorPojo.createdAt,
                    updatedAt = authorPojo.updatedAt
                )
            },
            createdAt = bookPojo.createdAt,
            updatedAt = bookPojo.updatedAt
        )
    }

    @Transactional
    override fun update(book: Book): Book {
        // 書籍情報を更新し、更新されたレコードを取得
        val updatedRecord = dsl.update(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.STATUS_ID, book.status.id)
            .where(BOOKS.ID.eq(book.id))
            .returning()
            .fetchOne()!!

        // 既存の著者関係を削除
        dsl.deleteFrom(AUTHORSHIPS)
            .where(AUTHORSHIPS.BOOK_ID.eq(book.id))
            .execute()

        // 新しい著者関係を挿入
        if (book.authorIds.isNotEmpty()) {
            val batch = book.authorIds.map { authorId ->
                dsl.insertInto(AUTHORSHIPS)
                    .set(AUTHORSHIPS.BOOK_ID, book.id)
                    .set(AUTHORSHIPS.AUTHOR_ID, authorId)
            }
            dsl.batch(batch).execute()
        }

        return book.copy(updatedAt = updatedRecord.updatedAt)
    }

    override fun findByAuthorId(authorId: UUID): List<Book> {
        // multisetを使って1つのクエリで書籍と全著者を取得
        val results = dsl.select(
            BOOKS.asterisk(),
            // 各書籍に関連する全著者をmultisetで取得
            multiset(
                select(AUTHORS.asterisk())
                    .from(AUTHORS)
                    .join(AUTHORSHIPS).on(AUTHORS.ID.eq(AUTHORSHIPS.AUTHOR_ID))
                    .where(AUTHORSHIPS.BOOK_ID.eq(BOOKS.ID))
                    .orderBy(AUTHORS.NAME)
            ).`as`("authors").convertFrom { it.into(AuthorPojo::class.java) }
        )
            .from(BOOKS)
            .join(AUTHORSHIPS).on(BOOKS.ID.eq(AUTHORSHIPS.BOOK_ID))
            .where(AUTHORSHIPS.AUTHOR_ID.eq(authorId))
            .orderBy(BOOKS.TITLE)
            .fetch()

        return results.map { result ->
            val bookPojo = result.into(BOOKS).into(BookPojo::class.java)
            val authorPojos = result.getValue("authors") as List<AuthorPojo>

            Book(
                id = bookPojo.id,
                title = bookPojo.title!!,
                price = bookPojo.price!!,
                status = PublicationStatus.fromId(bookPojo.statusId!!)!!,
                authors = authorPojos.map { authorPojo ->
                    Author(
                        id = authorPojo.id,
                        name = authorPojo.name!!,
                        birthdate = authorPojo.birthdate!!,
                        createdAt = authorPojo.createdAt,
                        updatedAt = authorPojo.updatedAt
                    )
                },
                createdAt = bookPojo.createdAt,
                updatedAt = bookPojo.updatedAt
            )
        }
    }
}
