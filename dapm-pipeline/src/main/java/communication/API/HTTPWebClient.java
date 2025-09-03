package communication.API;

import communication.API.request.HTTPRequest;
import communication.API.response.HTTPResponse;
import exceptions.RemoteCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HTTPWebClient implements HTTPClient {
    private final WebClient webClient;

    @Autowired
    public HTTPWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public HTTPResponse postSync(HTTPRequest httpRequest) {
        return sendRequest("POST", httpRequest);
    }
    @Override
    public HTTPResponse putSync(HTTPRequest httpRequest) {
        return sendRequest("PUT", httpRequest);
    }
    @Override
    public HTTPResponse deleteSync(HTTPRequest httpRequest) {return sendRequest("DELETE", httpRequest);}

    private HTTPResponse sendRequest(String method, HTTPRequest httpRequest) {
        String url = httpRequest.getUrl();
        String body = httpRequest.getBody();
        try {
            WebClient.RequestBodySpec request = webClient
                    .method(org.springframework.http.HttpMethod.valueOf(method))
                    .uri(url)
                    .header("Content-Type", "application/json");

            var result = (body == null || body.isEmpty())
                    ? request.exchangeToMono(response ->
                    response.bodyToMono(String.class)
                            .map(b -> new HTTPResponse(response.statusCode(), b))
                            .defaultIfEmpty(new HTTPResponse(response.statusCode(), null))
            ).block()
                    : request.bodyValue(body).exchangeToMono(response ->
                    response.bodyToMono(String.class)
                            .map(b -> new HTTPResponse(response.statusCode(), b))
                            .defaultIfEmpty(new HTTPResponse(response.statusCode(), null))
            ).block();
            if (result == null) {
                throw new RemoteCallException("Received no response at " + url);
            }
            return result;
        } catch (Exception e) {
            throw new RemoteCallException("Request failed: " + url, e);
        }
    }
}