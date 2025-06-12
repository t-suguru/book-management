package io.github.t_suguru.book_management.service

import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.domain.repository.AuthorRepository
import io.github.t_suguru.book_management.dto.AuthorCreateRequest
import org.springframework.stereotype.Service
import java.util.*

/**
 * 著者ビジネスロジック
 */
@Service
class AuthorService(
    private val authorRepository: AuthorRepository
) {

    /**
     * 著者を作成する
     */
    fun createAuthor(request: AuthorCreateRequest): Author {
        val author = Author(
            name = request.name,
            birthdate = request.birthdate
        )
        return authorRepository.save(author)
    }

    /**
     * 著者を更新する
     */
    fun updateAuthor(id: UUID, request: AuthorCreateRequest): Author? {
        val existingAuthor = authorRepository.findById(id) ?: return null
        
        val updatedAuthor = existingAuthor.copy(
            name = request.name,
            birthdate = request.birthdate
        )
        
        return authorRepository.update(updatedAuthor)
    }

    /**
     * IDで著者を取得する
     */
    fun getAuthorById(id: UUID): Author? {
        return authorRepository.findById(id)
    }
}
