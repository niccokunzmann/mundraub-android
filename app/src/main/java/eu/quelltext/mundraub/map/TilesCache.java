package eu.quelltext.mundraub.map;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Set<Tile> getTilesIn(BoundingBox bbox, int zoom) {
        Set<Tile> tiles = new HashSet<>();
        List<Tile> tilesInMiddleNorth = new ArrayList<>();
        List<Tile> tilesInMiddleSouth = new ArrayList<>();
        Tile centerTile = getTileAt(bbox.middle(), zoom);
        tiles.add(centerTile);
        // expand center to north
        for (Tile tile = centerTile; tile != null && tile.bbox().southBorderLatitude() < bbox.northBorderLatitude(); tile = tile.inTheNorth()) {
            tilesInMiddleNorth.add(tile);
            tiles.add(tile);
        }
        // expand center to south
        for (Tile tile = centerTile; tile != null && tile.bbox().northBorderLatitude() > bbox.southBorderLatitude(); tile = tile.inTheSouth()) {
            tilesInMiddleSouth.add(tile);
            tiles.add(tile);
        }
        for (Tile middleNorthTile : tilesInMiddleNorth) {
            // expand north middle to east
            for (Tile tile = middleNorthTile; bbox.contains(tile.bbox().southWestCorner()); tile = tile.inTheEast()) {
                tiles.add(tile);
            }
            // expand north middle to west
            for (Tile tile = middleNorthTile; bbox.contains(tile.bbox().southEastCorner()); tile = tile.inTheWest()) {
                tiles.add(tile);
            }
        }
        for (Tile middleSouthTile : tilesInMiddleSouth) {
            // expand north middle to east
            for (Tile tile = middleSouthTile; bbox.contains(tile.bbox().northWestCorner()); tile = tile.inTheEast()) {
                tiles.add(tile);
            }
            // expand north middle to west
            for (Tile tile = middleSouthTile; bbox.contains(tile.bbox().northEastCorner()); tile = tile.inTheWest()) {
                tiles.add(tile);
            }
        }
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

        public BoundingBox bbox() {
            return BoundingBox.ofTileAt(position);
        }

        public Tile inTheNorth() {
            TilePosition northPosition = getPosition().oneNorth();
            if (northPosition == null) {
                return null;
            }
            return new Tile(northPosition);
        }

        public Tile inTheSouth() {
            TilePosition southPosition = getPosition().oneSouth();
            if (southPosition == null) {
                return null;
            }
            return new Tile(southPosition);
        }

        public Tile inTheWest() {
            return new Tile(getPosition().oneWest());
        }

        public Tile inTheEast() {
            return new Tile(getPosition().oneEast());
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
