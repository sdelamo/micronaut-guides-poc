package example.micronaut

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import org.reactivestreams.Publisher
import io.micronaut.core.async.annotation.SingleResult
import java.net.URI
import javax.inject.Singleton

@Singleton // <1>
class GithubLowLevelClient(@param:Client(GithubConfiguration.GITHUB_API_URL) private val httpClient: HttpClient,  // <2>
                           configuration: GithubConfiguration) {  // <3>
    private val uri: URI = UriBuilder.of("/repos")
        .path(configuration.organization)
        .path(configuration.repo)
        .path("releases")
        .build()

    @SingleResult
    fun fetchReleases(): Publisher<List<GithubRelease>> {
        val req: HttpRequest<*> = HttpRequest.GET<Any>(uri) // <4>
            .header(USER_AGENT, "Micronaut HTTP Client") // <5>
            .header(ACCEPT, "application/vnd.github.v3+json, application/json") // <6>
        return httpClient.retrieve(req, Argument.listOf(GithubRelease::class.java)) // <7>
    }
}
