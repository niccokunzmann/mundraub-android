package eu.quelltext.mundraub.map;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TilesCache {

    private final ContentType contentType;
    private final File root;

    public TilesCache(File root, ContentType contentType) {
        this.contentType = contentType;
        this.root = root;
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
