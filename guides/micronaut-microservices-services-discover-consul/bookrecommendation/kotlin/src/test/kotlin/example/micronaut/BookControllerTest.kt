package example.micronaut

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.inject.Inject
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable

@MicronautTest
class BookControllerTest {
    @Inject
    @field:Client("/")
    lateinit var client: RxStreamingHttpClient

    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    @Test
    fun testRetrieveBooks() {
        val books = client.jsonStream(HttpRequest.GET<Any>("/books"), BookRecommendation::class.java)
        assertEquals(books.toList().blockingGet().size, 1)
        assertEquals(books.toList().blockingGet()[0].name, "Building Microservices")
    }
}
