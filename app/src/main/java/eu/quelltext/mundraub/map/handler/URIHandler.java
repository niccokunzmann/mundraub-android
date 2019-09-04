package eu.quelltext.mundraub.map.handler;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.util.IHandler;

public class URIHandler implements IHandler<IHTTPSession, Response> {

    private final String uri;
    private final ErrorHandler errorHandler;

    public URIHandler(String uri,ErrorHandler errorHandler) {
        this.uri = uri;
        this.errorHandler = errorHandler;
    }

    @Override
    public Response handle(IHTTPSession input) {
        if (wantsToServe(input)) {
            Response response;
            try {
                response = respondTo(input);
            } catch (Exception e) {
                response = handleError(e);
            }
            response.addHeader("Access-Control-Allow-Origin", "*"); // allow JavaScript to access the content
            return response;
        }
        return null;
    }

    // this should be overwritten
    public Response respondTo(IHTTPSession input) throws Exception {
        return null;
    }

    public boolean wantsToServe(IHTTPSession input) {
        return wantsToHandleMethod(input.getMethod()) &&
                wantsToServeURI(input.getUri());
    }

    public boolean wantsToServeURI(String uri) {
        return uri.equals(this.uri);
    }

    public boolean wantsToHandleMethod(Method method) {
        return method == Method.GET;
    }

    public Response handleError(Exception e) {
        errorHandler.handleError(e);
        String msg = "<html><body><h1>500 Internal server error</h1>\n" +
                "<pre>" + ExceptionUtils.getStackTrace(e) + "</pre></body></html>";
        return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, msg);
    }

    protected String baseUri() {
        return uri;
    }
}
