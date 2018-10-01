package eu.quelltext.mundraub.map;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.map.position.BoundingBox;
import eu.quelltext.mundraub.map.position.Position;
import eu.quelltext.mundraub.map.position.TilePosition;

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

    public Tile getTileAt(int x, int y, int zoom) {
        return new Tile(new TilePosition(x, y, zoom));
    }

    public Tile getTileAt(Position position, int zoom) {
        return new Tile(new TilePosition(position, zoom));
    }

    public List<Tile> getTilesIn(BoundingBox bbox, int zoom) {
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        tiles.add(getTileAt(bbox.middle(), zoom));
        return tiles;
    }

    public class Tile {

        private final TilePosition position;
        private String url = null;

        public Tile(TilePosition position) {
            this.position = position;
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
            return new File(root, position.zoom() + "/" + position.x() + "/" + position.y() + contentType.extension());
        }

        public String path() {
            return contentType.extension();
        }

        public String url() {
            if (url == null) {
                url = urlTemplate
                        .replaceFirst("\\$\\{x\\}", Integer.toString(position.x()))
                        .replaceFirst("\\$\\{y\\}", Integer.toString(position.y()))
                        .replaceFirst("\\$\\{z\\}", Integer.toString(position.zoom()));
            }
            return url;
        }

        public TilePosition getPosition() {
            return position;
        }

        @Override
        public boolean equals(Object obj) {
            if (!getClass().isInstance(obj)) {
                return super.equals(obj);
            }
            Tile other = (Tile) obj;
            return url().equals(other.url());
        }

        @Override
        public int hashCode() {
            return url().hashCode();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + getPosition().x() + ", " + getPosition().y() + ", " + getPosition().zoom() + ")";
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
