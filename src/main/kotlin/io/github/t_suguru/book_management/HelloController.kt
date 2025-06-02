package io.github.t_suguru.book_management

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class HelloController {

    @GetMapping("/hello")
    fun hello(): String {
        return "Hello, Spring Boot with Kotlin and Gradle Groovy DSL!"
    }
}