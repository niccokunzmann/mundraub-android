package eu.quelltext.mundraub;

import org.junit.Before;
import org.junit.Test;

import eu.quelltext.mundraub.search.AddressSearchResult;
import eu.quelltext.mundraub.search.IAddressSearch;
import eu.quelltext.mundraub.search.INominatimInteraction;
import eu.quelltext.mundraub.search.NominatimAddressSearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NominatimTest {

    // from https://nominatim.openstreetmap.org/search?q=Potsdam&format=json
    private static String SEARCH_POTSDAM = "[{\"place_id\":17825094,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"node\",\"osm_id\":1695218178,\"boundingbox\":[\"52.2409309\",\"52.5609309\",\"12.8991397\",\"13.2191397\"],\"lat\":\"52.4009309\",\"lon\":\"13.0591397\",\"display_name\":\"Potsdam, Brandenburg, 14467, Germany\",\"class\":\"place\",\"type\":\"city\",\"importance\":0.7496565807311549,\"icon\":\"https://nominatim.openstreetmap.org/images/mapicons/poi_place_city.p.20.png\"},{\"place_id\":198676882,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"relation\",\"osm_id\":62369,\"boundingbox\":[\"52.3420411\",\"52.5146876\",\"12.8867757\",\"13.1682033\"],\"lat\":\"52.4284128\",\"lon\":\"13.0241778076903\",\"display_name\":\"Potsdam, Brandenburg, Germany\",\"class\":\"place\",\"type\":\"city\",\"importance\":0.7496565807311549,\"icon\":\"https://nominatim.openstreetmap.org/images/mapicons/poi_place_city.p.20.png\"},{\"place_id\":4082434,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"node\",\"osm_id\":435478312,\"boundingbox\":[\"54.3826687\",\"54.4226687\",\"9.4128681\",\"9.4528681\"],\"lat\":\"54.4026687\",\"lon\":\"9.4328681\",\"display_name\":\"Potsdam, Kropp-Stapelholm, Schleswig-Flensburg, Schleswig-Holstein, 24872, Germany\",\"class\":\"place\",\"type\":\"hamlet\",\"importance\":0.385,\"icon\":\"https://nominatim.openstreetmap.org/images/mapicons/poi_place_village.p.20.png\"},{\"place_id\":198112701,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"relation\",\"osm_id\":175967,\"boundingbox\":[\"44.649463\",\"44.693873\",\"-75.021608\",\"-74.953263\"],\"lat\":\"44.6697996\",\"lon\":\"-74.9813349\",\"display_name\":\"Potsdam, Saint Lawrence County, New York, 13699, USA\",\"class\":\"boundary\",\"type\":\"administrative\",\"importance\":0.382558137233171,\"icon\":\"https://nominatim.openstreetmap.org/images/mapicons/poi_boundary_administrative.p.20.png\"},{\"place_id\":197904773,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"relation\",\"osm_id\":182772,\"boundingbox\":[\"39.959346\",\"39.968036\",\"-84.422004\",\"-84.407645\"],\"lat\":\"39.9633855\",\"lon\":\"-84.4174489\",\"display_name\":\"Potsdam, Miami County, Ohio, 45361, USA\",\"class\":\"boundary\",\"type\":\"administrative\",\"importance\":0.366998402502008,\"icon\":\"https://nominatim.openstreetmap.org/images/mapicons/poi_boundary_administrative.p.20.png\"},{\"place_id\":736633,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"node\",\"osm_id\":262715019,\"boundingbox\":[\"-32.9897222\",\"-32.9497222\",\"27.6302778\",\"27.6702778\"],\"lat\":\"-32.9697222\",\"lon\":\"27.6502778\",\"display_name\":\"Potsdam, Kwelera, Buffalo City Metropolitan Municipality, Eastern Cape, RSA\",\"class\":\"place\",\"type\":\"suburb\",\"importance\":0.36,\"icon\":\"https://nominatim.openstreetmap.org/images/mapicons/poi_place_village.p.20.png\"},{\"place_id\":385287,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"node\",\"osm_id\":151665611,\"boundingbox\":[\"44.1455215\",\"44.1855215\",\"-92.3590638\",\"-92.3190638\"],\"lat\":\"44.1655215\",\"lon\":\"-92.3390638\",\"display_name\":\"Potsdam, Olmsted County, Minnesota, USA\",\"class\":\"place\",\"type\":\"hamlet\",\"importance\":0.35238366538230403,\"icon\":\"https://nominatim.openstreetmap.org/images/mapicons/poi_place_village.p.20.png\"},{\"place_id\":111360886,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"way\",\"osm_id\":155211733,\"boundingbox\":[\"43.8531399\",\"43.8534546\",\"25.9791362\",\"25.9797862\"],\"lat\":\"43.8533267\",\"lon\":\"25.9794702\",\"display_name\":\"Potsdam, Търговия на едро, жк. Здравец-север, Ruse, 7005, Bulgaria\",\"class\":\"highway\",\"type\":\"tertiary\",\"importance\":0.21000000000000002},{\"place_id\":72766718,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"way\",\"osm_id\":10817193,\"boundingbox\":[\"26.910974\",\"26.911481\",\"-82.212921\",\"-82.209848\"],\"lat\":\"26.911481\",\"lon\":\"-82.211429\",\"display_name\":\"Potsdam, Charlotte County, Florida, 33927, USA\",\"class\":\"highway\",\"type\":\"residential\",\"importance\":0.21000000000000002},{\"place_id\":69552236,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"way\",\"osm_id\":4910353,\"boundingbox\":[\"14.6269233\",\"14.6289158\",\"121.0603938\",\"121.0613666\"],\"lat\":\"14.6270084\",\"lon\":\"121.0613199\",\"display_name\":\"Potsdam, Mangga, Cubao, 3rd District, Quezon City, Metro Manila, 1102, Philippines\",\"class\":\"highway\",\"type\":\"residential\",\"importance\":0.21000000000000002}]";
    private static String SEARCH_EMPTY = "[]";

    private String searchResult;
    private String searchTerm;
    private int searchError;
    private int observedError;
    private IAddressSearch observedSearch;
    private IAddressSearch search;

    @Before
    public void setUp() {
        searchResult = null;
        searchTerm = null;
        searchError = 0;
        observedSearch = null;
        observedError = 0;
        search = createSearch();
    }

    private IAddressSearch createSearch() {
        IAddressSearch search = new NominatimAddressSearch(new INominatimInteraction() {
            @Override
            public void search(String text, INominatimCallback cb) {
                searchTerm = text;
                if (searchResult == null) {
                    cb.onError(searchError);
                } else {
                    cb.onResult(searchResult);
                }
            }
        });
        search.notifyAboutChanges(new IAddressSearch.Observer() {
            @Override
            public void onNewSearchResults(IAddressSearch addressSearch) {
                observedSearch = addressSearch;
            }

            @Override
            public void onSearchError(int errorId) {
                observedError = errorId;
            }
        });
        return search;
    }

    private void nextSearchResultsIn(String searchResult) {
        this.searchResult = searchResult;
    }
    private void nextSearchResultsIn(int errorId) {
        this.searchError = errorId;
    }

    private String getSearchTerm() {
        return searchTerm;
    }

    private int getObservedError() {
        return observedError;
    }

    private IAddressSearch getObservedSearch() {
        return observedSearch;
    }

    private void searchFor(String searchTerm, String nominatimResult) {
        nextSearchResultsIn(nominatimResult);
        search.search(searchTerm);
    }

    /* ------------------------- Tests ------------------------- */

    @Test
    public void testCreatedSearchIsEmpty() {
        assertEquals(search.size(), 0);
    }

    @Test
    public void testObserverIsNotNotifiedOnSubscribe() {
        final boolean[] observerWasNotified = {false};
        search.notifyAboutChanges(new IAddressSearch.Observer() {
            @Override
            public void onNewSearchResults(IAddressSearch addressSearch) {
                observerWasNotified[0] = true;
            }

            @Override
            public void onSearchError(int errorId) {
                observerWasNotified[0] = true;
            }
        });
        assertFalse(observerWasNotified[0]);
    }

    @Test
    public void testEmptySearchHasNoItems() {
        nextSearchResultsIn(SEARCH_EMPTY);
        search.search("Test");
        assertEquals(0, search.size());
    }

    @Test
    public void testEmptySearchPassesOnSearchTerm() {
        nextSearchResultsIn(SEARCH_EMPTY);
        search.search("Test");
        assertEquals(getSearchTerm(), "Test");
    }

    @Test
    public void testErrorCodeIsPassedOn() {
        nextSearchResultsIn(1000);
        search.search("Test");
        assertEquals(1000, getObservedError());
    }

    @Test
    public void testSearchNotifiesAboutResult() {
        nextSearchResultsIn(SEARCH_EMPTY);
        search.search("123");
        assertEquals(search,getObservedSearch());
    }

    @Test
    public void testPotsdamHasSearchResults() {
        searchFor("Potsdam", SEARCH_POTSDAM);
        assertEquals(10, search.size());
    }

    @Test
    public void testPotsdamResultsAreNotNull() {
        searchFor("Potsdam", SEARCH_POTSDAM);
        for (int i = 0; i < search.size(); i++) {
            assertNotNull(search.get(i));
        }
    }

    @Test
    public void testPotsdamIsOrderedByImportance() {
        searchFor("Potsdam", SEARCH_POTSDAM);
        for (int i = 0; i < search.size() - 1; i++) {
            AddressSearchResult thisResult = search.get(i);
            AddressSearchResult otherResult = search.get(i + 1);
            assertTrue(thisResult.compareTo(otherResult) >= 0);
        }
    }

    @Test
    public void testFirstSearchResultsHasFieldsSet() {
        searchFor("Potsdam", SEARCH_POTSDAM);
        AddressSearchResult result = search.get(0);
        assertEquals("Potsdam, Brandenburg, 14467, Germany", result.getDisplayName());
        assertEquals(52.4009309, result.getPosition().getLatitude(), 0.00000001);
        assertEquals(13.0591397, result.getPosition().getLongitude(), 0.00000001);
    }

    @Test
    public void testLastSearchResultsHasFieldsSet() {
        searchFor("Potsdam", SEARCH_POTSDAM);
        AddressSearchResult result = search.get(9);
        assertEquals("Potsdam, Mangga, Cubao, 3rd District, Quezon City, Metro Manila, 1102, Philippines", result.getDisplayName());
        assertEquals(14.6270084, result.getPosition().getLatitude(), 0.00000001);
        assertEquals(121.0613199, result.getPosition().getLongitude(), 0.00000001);
    }
}
