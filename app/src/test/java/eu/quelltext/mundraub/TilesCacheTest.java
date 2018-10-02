package eu.quelltext.mundraub;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Set;

import eu.quelltext.mundraub.map.TilesCache;
import eu.quelltext.mundraub.map.position.BoundingBox;
import eu.quelltext.mundraub.map.position.Position;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class TilesCacheTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private boolean temporaryFolderIsCreated = false;

    private TilesCache cache() {
        return cache(TilesCache.ContentType.PNG);
    }

    private TilesCache cache(TilesCache.ContentType contentType, String url) {
        if (!temporaryFolderIsCreated) {
            try {
                temporaryFolder.create();
            } catch (IOException e) {
                e.printStackTrace();
            }
            temporaryFolderIsCreated = true;
        }
        return new TilesCache(temporaryFolder.getRoot(), url, contentType);
    }

    private TilesCache cache(String url) {
        return cache(TilesCache.ContentType.JPG, url);
    }

    private TilesCache cache(TilesCache.ContentType contentType) {
        return cache(contentType, "http://a.b/${x}/${y}/${z}");
    }


    @Test
    public void testTilesAreNotCachedByDefault() throws IOException {
        assertFalse(cache().getTileAt(0,0,0).isCached());
        assertNull(cache().getTileAt(0,0,0).bytes());
    }

    @Test
    public void testTilesCanHaveAContent() throws IOException {
        TilesCache.Tile tile = cache().getTileAt(1, 0, 0);
        tile.setBytes("123".getBytes());
        assertTrue(tile.isCached());
        assertEquals("123", new String(tile.bytes()));
    }

    @Test
    public void testTwoTilesHaveSameContent() throws IOException {
        TilesCache.Tile tile1 = cache().getTileAt(1, 2, 0);
        TilesCache.Tile tile2 = cache().getTileAt(1, 2, 0);
        tile1.setBytes("123".getBytes());
        assertTrue(tile2.isCached());
        assertEquals("123", new String(tile2.bytes()));
    }

    @Test
    public void testTilesAtDifferentPositionsHaveDifferentContent() throws IOException {
        TilesCache.Tile tile = cache().getTileAt(1, 2, 0);
        tile.setBytes("124".getBytes());
        assertTileIsNotCached(1, 2, 1);
        assertTileIsNotCached(1, 1, 0);
        assertTileIsNotCached(0, 2, 0);
    }

    public void assertTileIsNotCached(int x, int y, int z) {
        TilesCache.Tile tile = cache().getTileAt(x, y, z);
        assertFalse(tile.isCached());
    }

    @Test
    public void testTypeJPEG() {
        assertEquals(".jpg", TilesCache.ContentType.JPG.extension());
        assertEquals("image/jpeg", TilesCache.ContentType.JPG.contentType());
    }

    @Test
    public void testTypePNG() {
        assertEquals(".png", TilesCache.ContentType.PNG.extension());
        assertEquals("image/png", TilesCache.ContentType.PNG.contentType());
    }

    @Test
    public void testContentTypeGoesToTiles() {
        TilesCache.ContentType[] contentTypes = new TilesCache.ContentType[]{
                TilesCache.ContentType.PNG,
                TilesCache.ContentType.JPG
        };
        for (TilesCache.ContentType contentType : contentTypes) {
            assertEquals(contentType.contentType(), cache(contentType).getTileAt(0, 0, 0).contentType());
            assertTrue(cache(contentType).getTileAt(0, 0, 0).path().endsWith(contentType.extension()));
        }
    }

    @Test
    public void testContentCanChange() throws IOException {
        TilesCache.Tile tile = cache().getTileAt(1, 1, 1);
        tile.setBytes("asd".getBytes());
        tile.setBytes("123".getBytes());
        assertEquals("123", new String(tile.bytes()));
    }

    @Test
    public void testUrlForTileFillsTemplate() {
        assertEquals("https://a.b/tiles/1/2/3.x", cache("https://a.b/tiles/${x}/${y}/${z}.x").getTileAt(1, 2,  3).url());
        assertEquals("http://a.c/tiles/11/22/33", cache("http://a.c/tiles/${z}/${y}/${x}").getTileAt(33, 22,  11).url());
    }

    @Test
    public void testGetTileAtPositionAndZoom11() {
        assertPosition(78.8718, 21.707686082885235, 11, 1472, 897);
    }

    @Test
    public void testGetTileAtPositionAndZoom8() {
        assertPosition(85.07083564452995, 23.962892067343013, 8, 188, 110);
    }

    private void assertPosition(double longitude, double latitude, int zoom, int x, int y) {
        String url = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/${z}/${y}/${x}/"; // 11/897/1472/
        TilesCache.Tile tile = cache(url).getTileAt(new Position(longitude, latitude), zoom);
        assertEquals(tile.getPosition().x(), x);
        assertEquals(tile.getPosition().y(), y);
        assertEquals(tile.getPosition().zoom(), zoom);
    }

    @Test
    public void testTwoTilesAreEqual() {
        assertEquals(cache().getTileAt(1, 2, 3), cache().getTileAt(1, 2, 3));
        assertEquals(cache().getTileAt(1, 2, 3).hashCode(), cache().getTileAt(1, 2, 3).hashCode());
        assertEquals(cache().getTileAt(11, 211, 31), cache().getTileAt(11, 211, 31));
        assertEquals(cache().getTileAt(11, 211, 31).hashCode(), cache().getTileAt(11, 211, 31).hashCode());
    }

    @Test
    public void testTwoTilesAreNotEqual() {
        assertFalse(cache().getTileAt(11, 2, 3).equals(cache().getTileAt(1, 2, 3)));
        assertFalse(cache().getTileAt(1, 21, 3).equals(cache().getTileAt(1, 2, 3)));
        assertFalse(cache().getTileAt(1, 2, 31).equals(cache().getTileAt(1, 2, 3)));
        assertFalse(cache("http://x.x/${x}/${y}/${z}").getTileAt(1, 2, 31).equals(cache().getTileAt(1, 2, 3)));
    }

    @Test
    public void testBBoxWithZeroWidthHasOneTile() {
        for (int zoom = 1; zoom < 18; zoom++) {
            BoundingBox bbox = BoundingBox.fromNESW(1, 1, 1, 1);
            assertEquals(1, cache().getTilesIn(bbox, zoom).size());
        }
    }

    @Test
    public void testBBoxWithZeroWidthHasTileAtMiddle1() {
        assertMiddlePosition(78.8718, 21.707686082885235, 11, 1472, 897);
    }

    @Test
    public void testBBoxWithZeroWidthHasTileAtMiddle2() {
        assertMiddlePosition(85.07083564452995, 23.962892067343013, 8, 188, 110);
    }

    private void assertMiddlePosition(double longitude, double latitude, int zoom, int x, int y) {
        BoundingBox bbox = BoundingBox.fromNESW(latitude, longitude, latitude, longitude);
        Set<TilesCache.Tile> tiles = cache().getTilesIn(bbox, zoom);
        assertEquals(1, tiles.size());
        assertTrue(tiles.contains(cache().getTileAt(x, y, zoom)));
    }

    @Test
    public void testTilesInBoundingBox1() {
        assertTilesInBBox(BoundingBox.fromWSEN(3.4277343750002305,45.2052634561631,29.794921875000252,49.717376404935926), 7, 65, 43, 74, 45);
    }

    @Test
    public void testTilesInBoundingBox2() {
        assertTilesInBBox(BoundingBox.fromWSEN(13.351135253906426,48.03034580796611,16.647033691406662,48.585692256886375), 10, 549, 353, 559, 355);
    }

    private void assertTilesInBBox(BoundingBox boundingBox, int zoom, int minX, int minY, int maxX, int maxY) {
        TilesCache cache = cache();
        Set<TilesCache.Tile> boundingBoxTiles = cache.getTilesIn(boundingBox, zoom);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                assertTrue(boundingBoxTiles.contains(cache.getTileAt(x, y, zoom)));
            }
        }
        assertEquals((maxX - minX + 1) * (maxY - minY + 1), boundingBoxTiles.size());
    }
}
