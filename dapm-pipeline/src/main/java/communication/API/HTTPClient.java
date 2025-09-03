package communication.API;

import communication.API.request.HTTPRequest;
import communication.API.response.HTTPResponse;

public interface HTTPClient {
    HTTPResponse postSync(HTTPRequest httpRequest);
    HTTPResponse putSync(HTTPRequest httpRequest);
    HTTPResponse deleteSync(HTTPRequest httpRequest);
}
