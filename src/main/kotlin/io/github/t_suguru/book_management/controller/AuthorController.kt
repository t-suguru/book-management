package io.github.t_suguru.book_management.controller

import io.github.t_suguru.book_management.domain.model.Author
import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.dto.AuthorCreateRequest
import io.github.t_suguru.book_management.service.AuthorService
import io.github.t_suguru.book_management.service.AuthorBookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * 著者API
 */
@RestController
@RequestMapping("/api/authors")
class AuthorController(
    private val authorService: AuthorService,
    private val authorBookService: AuthorBookService
) {

    /**
     * 著者を作成する
     */
    @PostMapping
    fun createAuthor(@Valid @RequestBody request: AuthorCreateRequest): ResponseEntity<Author> {
        val author = authorService.createAuthor(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(author)
    }

    /**
     * 著者を更新する
     */
    @PutMapping("/{id}")
    fun updateAuthor(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AuthorCreateRequest
    ): ResponseEntity<Author> {
        val author = authorService.updateAuthor(id, request)
        return if (author != null) {
            ResponseEntity.ok(author)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 著者の書籍一覧を取得する
     */
    @GetMapping("/{id}/books")
    fun getBooksByAuthor(@PathVariable id: UUID): ResponseEntity<List<Book>> {
        val books = authorBookService.getBooksByAuthorId(id)
        return ResponseEntity.ok(books)
    }
}
