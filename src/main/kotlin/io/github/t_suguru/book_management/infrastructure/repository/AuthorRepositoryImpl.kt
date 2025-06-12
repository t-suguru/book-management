package io.github.t_suguru.book_management.infrastructure.repository

import io.github.t_suguru.book_management.db.tables.Authors.Companion.AUTHORS
import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.domain.repository.AuthorRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * 著者リポジトリ実装クラス
 */
@Repository
class AuthorRepositoryImpl(
    private val dsl: DSLContext,
) : AuthorRepository {

    override fun save(author: Author): Author {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        dsl.insertInto(AUTHORS)
            .set(AUTHORS.ID, id)
            .set(AUTHORS.NAME, author.name)
            .set(AUTHORS.BIRTHDATE, author.birthdate)
            .set(AUTHORS.CREATED_AT, now)
            .set(AUTHORS.UPDATED_AT, now)
            .execute()

        return author.copy(
            id = id,
            createdAt = now,
            updatedAt = now
        )
    }

    override fun findById(id: UUID): Author? {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()
            ?.let { record ->
                Author(
                    id = record.id,
                    name = record.name!!,
                    birthdate = record.birthdate!!,
                    createdAt = record.createdAt,
                    updatedAt = record.updatedAt
                )
            }
    }

    override fun update(author: Author): Author {
        val now = LocalDateTime.now()

        dsl.update(AUTHORS)
            .set(AUTHORS.NAME, author.name)
            .set(AUTHORS.BIRTHDATE, author.birthdate)
            .set(AUTHORS.UPDATED_AT, now)
            .where(AUTHORS.ID.eq(author.id))
            .execute()

        return author.copy(updatedAt = now)
    }
}
