package io.github.t_suguru.book_management

import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * 統合テスト用のベースクラス
 * Testcontainersを使用してPostgreSQLコンテナを起動し、
 * 各テストで独立したデータベース環境を提供します
 */
@SpringBootTest
@Testcontainers
abstract class AbstractIntegrationTest {

    companion object {
        @ServiceConnection
        @JvmStatic
        val postgresContainer: POSTGRES_CONTAINER<*> = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true) // コンテナの再利用を有効化
            .waitingFor(Wait.forListeningPort())
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger("tc.container.postgres")))

        init {
            // コンテナがまだ起動していない場合のみ起動
            if (!postgresContainer.isRunning) {
                postgresContainer.start()
            }
        }
    }
}
