package example.micronaut;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Flowable;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest // <1>
class GithubControllerTest {

    @Inject
    @Client("/")
    RxStreamingHttpClient client; // <2>

    private static final List<String> expectedReleases = Arrays.asList("Micronaut 2.5.0", "Micronaut 2.4.4", "Micronaut 2.4.3");

    @Test
    public void verifyGithubReleasesCanBeFetchedWithLowLevelHttpClient() {
        //when:
        HttpRequest<Object> request = HttpRequest.GET("/github/releases-lowlevel");

        HttpResponse<List<GithubRelease>> rsp = client.toBlocking().exchange(request, // <3>
                Argument.listOf(GithubRelease.class)); // <4>

        //then: 'the endpoint can be accessed'
        assertEquals(HttpStatus.OK, rsp.getStatus());   // <5>
        assertNotNull(rsp.body()); // <6>

        //when:
        List<GithubRelease> releases = rsp.body();

        //then:
        for (String name : expectedReleases) {
            assertTrue(releases.stream().map(GithubRelease::getName).anyMatch(name::equals));
        }
    }

    @Test
    public void verifyGithubReleasesCanBeFetchedWithCompileTimeAutoGeneratedAtClient() {
        //when:
        HttpRequest<Object> request = HttpRequest.GET("/github/releases-lowlevel");

        Flowable<GithubRelease> githubReleaseStream = client.jsonStream(request, GithubRelease.class); // <7>
        Iterable<GithubRelease> githubReleases = githubReleaseStream.blockingIterable();

        //then:
        for (String name : expectedReleases) {
            assertTrue(StreamSupport.stream(githubReleases.spliterator(), false)
                    .map(GithubRelease::getName)
                    .anyMatch(name::equals));
        }
    }
}