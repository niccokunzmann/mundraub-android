package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.search.AddressSearch;
import eu.quelltext.mundraub.search.AddressSearchResult;
import eu.quelltext.mundraub.search.DummyAddressSearch;

public class AddressSearchActivity extends MundraubBaseActivity implements AddressSearchResultFragment.SearchResultListener {

    private static final String OPEN_STREET_MAP_COPYRIGHT_URL = "https://osm.org/copyright";

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

        EditText searchText = (EditText) findViewById(R.id.search_address_text);
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onListFragmentInteraction(AddressSearchResult item) {

    }

    @Override
    public void notifyAboutChanges(AddressSearch.Observer observer) {
        new DummyAddressSearch().notifyAboutChanges(observer);
    }
}
