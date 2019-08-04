package eu.quelltext.mundraub;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.quelltext.mundraub.map.position.IPosition;
import eu.quelltext.mundraub.map.position.Position;
import eu.quelltext.mundraub.search.AddressSearchResult;
import eu.quelltext.mundraub.search.AddressSearchStore;
import eu.quelltext.mundraub.search.IAddressSearch;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class AddressStoreTest {

    private static IPosition POSITION = new Position(0,0);
    private static AddressSearchResult A1 = new AddressSearchResult(1, "Test1", "Te", POSITION);
    private static AddressSearchResult A2 = new AddressSearchResult(2, "Test2", "Te", POSITION);

    private AddressSearchStore store;
    private boolean notified;

    @Before
    public void setUp() {
        notified = false;
        store = new AddressSearchStore();
        store.notifyAboutChanges(new IAddressSearch.Observer() {
            @Override
            public void onNewSearchResults(IAddressSearch addressSearch) {
                notified = true;
            }
            @Override
            public void onSearchError(int errorId) {}
        });
    }

    @Test
    public void testNewAddressStoreIsEmpty() {
        assertEquals(0, store.size());
        store.search("");
        assertEquals(0, store.size());
    }

    @Test
    public void testNewAddressIsNotUsedBeforeSearch() {
        store.add(A1);
        assertEquals(0, store.size());
    }

    @Test
    public void testNewAddressIsUsedAfterSearch() {
        store.add(A1);
        store.search("");
        assertEquals(1, store.size());
        assertEquals(A1, store.get(0));
    }

    @Test
    public void testNotAllAddressesAreUsed() {
        store.add(A1);
        store.add(A2);
        store.search("Test2");
        assertEquals(1, store.size());
        assertEquals(A2, store.get(0));
    }

    @Test
    public void testNotifyOnSearch() {
        store.search("");
        assertTrue(notified);
    }

    @Test
    public void testDoNotNotifyOnSubscribe() {
        assertFalse(notified);
    }

    @Test
    public void testCanSerializeContent() throws JSONException {
        store.add(A1);
        store.add(A2);
        JSONObject json = store.toJSON();
        store = AddressSearchStore.fromJSON(json);
        store.search("");
        assertEquals(2, store.size());
        assertEquals(A1, store.get(0));
        assertEquals(A2, store.get(1));
    }

}
