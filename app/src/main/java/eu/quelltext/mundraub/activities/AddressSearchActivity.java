package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.search.AddressSearchResult;
import eu.quelltext.mundraub.search.DummyAddressSearch;
import eu.quelltext.mundraub.search.IAddressSearch;

public class AddressSearchActivity extends MundraubBaseActivity implements AddressSearchResultFragment.SearchResultListener {

    private static final String OPEN_STREET_MAP_COPYRIGHT_URL = "https://osm.org/copyright";
    private IAddressSearch addressSearch = new DummyAddressSearch();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_search);

        TextView searchLicense = (TextView) findViewById(R.id.text_seach_license);
        searchLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openURLInBrowser(OPEN_STREET_MAP_COPYRIGHT_URL);
            }
        });

        final EditText searchText = (EditText) findViewById(R.id.search_address_text);
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressSearch.search(searchText.getText().toString());
            }
        });

    }

    @Override
    public void onListFragmentInteraction(AddressSearchResult item) {

    }

    @Override
    public void notifyAboutChanges(IAddressSearch.Observer observer) {
        addressSearch.notifyAboutChanges(observer);
    }

    @Override
    public void onSearchError(int errorId) {
        new Dialog(this).alertError(errorId);
    }

    @Override
    protected void menuOpenAddressSearch() {
    }
}
