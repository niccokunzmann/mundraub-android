package eu.quelltext.mundraub;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import eu.quelltext.mundraub.map.TilesCache;

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

    private TilesCache cache(TilesCache.ContentType contentType) {
        if (!temporaryFolderIsCreated) {
            try {
                temporaryFolder.create();
            } catch (IOException e) {
                e.printStackTrace();
            }
            temporaryFolderIsCreated = true;
        }
        return new TilesCache(temporaryFolder.getRoot(), contentType);
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

    // TODO:
    // test file path
    // test url for tile
    // test getting tiles at position and zoom
    // test getting tiles in range of point

}
