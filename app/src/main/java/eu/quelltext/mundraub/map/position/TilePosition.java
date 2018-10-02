package eu.quelltext.mundraub.map.position;

public class TilePosition {

    private final int zoom;
    private final int x;
    private final int y;

    public TilePosition(Position position, int zoom) {
        this.zoom = zoom;
        // computation is taken from
        // https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
        int xtile = (int) Math.floor((position.getLongitude() + 180) / 360 * (1 << zoom));;
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(position.getLatitude())) + 1 / Math.cos(Math.toRadians(position.getLatitude()))) / Math.PI) / 2 * (1 << zoom));
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1<<zoom))
            xtile = ((1<<zoom)-1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1<<zoom))
            ytile = ((1<<zoom)-1);
        x = xtile;
        y = ytile;
    }

    public TilePosition(int x, int y, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public int x() {
        return x;
    }
    public int y() {
        return y;
    }
    public int zoom() {
        return zoom;
    }

    public TilePosition oneNorth() {
        if (y <= 0) {
            return null;
        }
        return new TilePosition(x, y -1, zoom);
    }
    public TilePosition oneSouth() {
        if (y >= (1 << zoom) - 1) {
            return null;
        }
        return new TilePosition(x, StrictMath.min(((1<<zoom)-1), y+1), zoom);
    }
    public TilePosition oneEast() {
        return new TilePosition((x + (1<<zoom) + 1) % (1<<zoom), y, zoom);
    }
    public TilePosition oneWest() {
        return new TilePosition((x + (1<<zoom) - 1) % (1<<zoom), y, zoom);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + x + ", " + y + ", " + zoom + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!getClass().isInstance(obj)) {
            return super.equals(obj);
        }
        TilePosition other = (TilePosition) obj;
        return x() == other.x() && y() == other.y() && zoom() == other.zoom();
    }

    public BoundingBox bbox() {
        return BoundingBox.ofTileAt(this);
    }
}
