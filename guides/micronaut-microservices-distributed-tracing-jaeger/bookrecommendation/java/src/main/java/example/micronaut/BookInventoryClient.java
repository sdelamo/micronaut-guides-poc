package example.micronaut;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Recoverable;
import org.reactivestreams.Publisher;
import io.micronaut.core.async.annotation.SingleResult;

import javax.validation.constraints.NotBlank;

@Client("http://localhost:8082")
@Recoverable(api = BookInventoryOperations.class)
public interface BookInventoryClient extends BookInventoryOperations {

    @Consumes(MediaType.TEXT_PLAIN)
    @Get("/books/stock/{isbn}")
    @SingleResult
    Publisher<Boolean> stock(@NotBlank String isbn);
}
