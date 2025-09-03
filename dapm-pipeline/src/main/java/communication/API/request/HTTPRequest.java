package communication.API.request;

public class HTTPRequest {

    private final String url;
    private String body;

    public HTTPRequest(String url, String body) {
        this.url = url;
        this.body = body;
    }

    public HTTPRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }
}
