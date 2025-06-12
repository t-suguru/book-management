package io.github.t_suguru.book_management.repository

import io.github.t_suguru.book_management.AbstractIntegrationTest
import io.github.t_suguru.book_management.db.tables.references.AUTHORS
import io.github.t_suguru.book_management.db.tables.references.BOOKS
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

/**
 * データベース接続とFlywayマイグレーションのテスト
 */
@SpringBootTest
@Transactional
class DatabaseIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var dslContext: DSLContext

    @Test
    fun `データベース接続が正常に動作することを確認`() {
        // Given & When
        val tableCount = dslContext.selectCount()
            .from("information_schema.tables")
            .where("table_schema = 'public'")
            .fetchOne(0, Int::class.java) ?: 0

        // Then
        // Flywayマイグレーションによってテーブルが作成されていることを確認
        assertTrue(tableCount > 0, "マイグレーションによってテーブルが作成されている必要があります")
    }

    @Test
    fun `Flywayマイグレーションが正常に実行されることを確認`() {
        // Given & When
        val migrationHistoryCount = dslContext.selectCount()
            .from("flyway_schema_history")
            .fetchOne(0, Int::class.java) ?: 0

        // Then
        assertTrue(migrationHistoryCount > 0, "Flywayマイグレーション履歴が存在する必要があります")
    }

    @Test
    fun `jOOQの生成されたクラスが正常に動作することを確認`() {
        // Given & When
        val authorCount = dslContext.selectCount()
            .from(AUTHORS)
            .fetchOne(0, Int::class.java)

        val bookCount = dslContext.selectCount()
            .from(BOOKS)
            .fetchOne(0, Int::class.java)

        // Then
        // テーブルが存在し、クエリが正常に実行されることを確認
        // 初期状態では0件でも問題なし（テーブルが存在することが重要）
        authorCount?.let { assertTrue(it >= 0, "AUTHORSテーブルへのクエリが正常に実行される必要があります") }
        bookCount?.let { assertTrue(it >= 0, "BOOKSテーブルへのクエリが正常に実行される必要があります") }
    }
}
