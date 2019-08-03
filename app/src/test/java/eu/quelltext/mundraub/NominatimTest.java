package eu.quelltext.mundraub;

import org.junit.Test;

import eu.quelltext.mundraub.search.IAddressSearch;
import eu.quelltext.mundraub.search.INominatimInteraction;
import eu.quelltext.mundraub.search.NominatimAddressSearch;

import static org.junit.Assert.assertEquals;

public class NominatimTest {

    private IAddressSearch createSearch() {
        return new NominatimAddressSearch(new INominatimInteraction() {
            @Override
            public void search(String text, INominatimCallback cb) {
            }
        });
    }

    @Test
    public void testCreatedSearchIsEmpty() {
        IAddressSearch search = createSearch();
        assertEquals(search.size(), 0);
    }

}
