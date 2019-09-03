package eu.quelltext.mundraub.map.handler;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.map.MundraubMapAPIForApp;
import eu.quelltext.mundraub.map.TilesCache;

public class TilesHandler extends URIHandler {
    private final Logger.Log log;
    private MundraubMapAPIForApp mundraubMapAPIForApp;
    private final TilesCache cache;

    public TilesHandler(MundraubMapAPIForApp mundraubMapAPIForApp, String uri, TilesCache cache, ErrorHandler errorHandler, Logger.Log log) {
        super(uri, errorHandler);
        this.mundraubMapAPIForApp = mundraubMapAPIForApp;
        this.cache = cache;
        this.log = log;
    }

    @Override
    public boolean wantsToServeURI(String uri) {
        return uri.startsWith(baseUri());
    }

    public Response respondTo(IHTTPSession input) throws Exception {
        String[] zyx = input.getUri().substring(this.baseUri().length()).split("/");
        if (zyx.length < 3) {
            return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "I need " + baseUri() + "z/y/x");
        }
        int x = Integer.parseInt(zyx[zyx.length - 1]);
        int y = Integer.parseInt(zyx[zyx.length - 2]);
        int z = Integer.parseInt(zyx[zyx.length - 3]);
        TilesCache.Tile tile = cache.getTileAt(x, y, z);
        if (tile.isCached()) {
            log.d("handle tile", 200 + ": " + z + "/" + y + "/" + x);
            Response response = Response.newFixedLengthResponse(Status.OK, tile.contentType(), tile.bytes());
            response.addHeader("Cache-Control", "no-store, must-revalidate"); // no cache from https://stackoverflow.com/a/2068407
            return response;
        } else {
            log.d("handle tile", 307 + ": " + z + "/" + y + "/" + x + " -> " + tile.url());
            Response response = Response.newFixedLengthResponse(Status.TEMPORARY_REDIRECT, NanoHTTPD.MIME_PLAINTEXT, "Image not found.");
            response.addHeader("Location", tile.url());
            return response;
        }
    }
}
