package eu.quelltext.mundraub.map;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import eu.quelltext.mundraub.map.position.Position;

public class TilesCache {

    private final ContentType contentType;
    private final File root;
    private final String urlTemplate;

    public TilesCache(File root, String url, ContentType contentType) {
        this.contentType = contentType;
        this.root = root;
        this.urlTemplate = url;
    }

    public static TilesCache forSatellite() {
        return null;
    }

    public static TilesCache forOSM() {
        return null;
    }

    public Tile getTileAt(int x, int y, int z) {
        return new Tile(x, y, z);
    }

    public Tile getTileAt(Position position, int zoom) {
        // computation is taken from
        // https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
        int xtile = (int)Math.floor( (position.getLongitude() + 180) / 360 * (1<<zoom) ) ;
        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(position.getLatitude())) + 1 / Math.cos(Math.toRadians(position.getLatitude()))) / Math.PI) / 2 * (1<<zoom) ) ;
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1<<zoom))
            xtile = ((1<<zoom)-1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1<<zoom))
            ytile = ((1<<zoom)-1);
        return getTileAt(xtile, ytile, zoom);
    }

    public class Tile {

        private final int x;
        private final int y;
        private final int z;

        public Tile(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean isCached() {
            return file().exists();
        }

        public byte[] bytes() throws IOException {
            if (isCached()) {
                return FileUtils.readFileToByteArray(file());
            }
            return null;
        }

        public String contentType() {
            return contentType.contentType();
        }

        public void setBytes(byte[] bytes) throws IOException {
            if (isCached() || file().getParentFile().mkdirs()) {
                FileUtils.writeByteArrayToFile(file(), bytes);
            }
        }

        public File file() {
            return new File(root, x + "/" + y + "/" + z + contentType.extension());
        }

        public String path() {
            return contentType.extension();
        }

        public String url() {
            return urlTemplate
                    .replaceFirst("\\$\\{x\\}", Integer.toString(x))
                    .replaceFirst("\\$\\{y\\}", Integer.toString(y))
                    .replaceFirst("\\$\\{z\\}", Integer.toString(z));
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int zoom() {
            return z;
        }
    }

    public static class ContentType {
        public static ContentType PNG = new ContentType(".png", "image/png");
        public static ContentType JPG = new ContentType(".jpg", "image/jpeg");
        private final String extension;
        private final String contentType;

        public ContentType(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        public String extension() {
            return extension;
        }

        public String contentType() {
            return contentType;
        }
    }
}
