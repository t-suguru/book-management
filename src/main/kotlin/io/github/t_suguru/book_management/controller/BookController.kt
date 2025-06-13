package io.github.t_suguru.book_management.controller

import io.github.t_suguru.book_management.domain.model.Book
import io.github.t_suguru.book_management.dto.BookCreateRequest
import io.github.t_suguru.book_management.dto.BookUpdateRequest
import io.github.t_suguru.book_management.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * 書籍API
 */
@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService
) {

    /**
     * 書籍を作成する
     */
    @PostMapping
    fun createBook(@Valid @RequestBody request: BookCreateRequest): ResponseEntity<Book> {
        val book = bookService.createBook(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(book)
    }

    /**
     * 書籍を更新する
     */
    @PutMapping("/{id}")
    fun updateBook(
        @PathVariable id: UUID,
        @Valid @RequestBody request: BookUpdateRequest
    ): ResponseEntity<Book> {
        return try {
            val book = bookService.updateBook(id, request)
            if (book != null) {
                ResponseEntity.ok(book)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
}
