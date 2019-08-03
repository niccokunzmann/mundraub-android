package eu.quelltext.mundraub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.search.AddressSearchResult;
import eu.quelltext.mundraub.search.IAddressSearch;
import eu.quelltext.mundraub.search.NominatimAddressSearch;
import eu.quelltext.mundraub.search.NominatimWebInteraction;

public class AddressSearchActivity extends MundraubBaseActivity implements AddressSearchResultFragment.SearchResultListener {

    private static final String OPEN_STREET_MAP_COPYRIGHT_URL = "https://osm.org/copyright";
    public static final String ARG_MAP_URL = "map-url"; // used as a return value in the intent

    private IAddressSearch addressSearch = new NominatimAddressSearch(new NominatimWebInteraction());

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
    public void onListFragmentInteraction(AddressSearchResult chosenAddress) {
        // create an intent and return the position to the calling activity
        // see https://stackoverflow.com/a/14785924
        Intent intent = new Intent();
        intent.putExtra(ARG_MAP_URL, chosenAddress.asMapUrl().getUrl());
        setResult(RESULT_OK, intent);
        finish();
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
