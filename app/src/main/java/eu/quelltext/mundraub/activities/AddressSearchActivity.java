package eu.quelltext.mundraub.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.search.AddressSearchResult;
import eu.quelltext.mundraub.search.AddressSearchStore;
import eu.quelltext.mundraub.search.IAddressSearch;
import eu.quelltext.mundraub.search.NominatimAddressSearch;
import eu.quelltext.mundraub.search.NominatimWebInteraction;

public class AddressSearchActivity extends MundraubBaseActivity implements AddressSearchResultFragment.SearchResultListener {

    private static final String OPEN_STREET_MAP_COPYRIGHT_URL = "https://osm.org/copyright";
    public static final String ARG_MAP_URL = "map-url"; // used as a return value in the intent
    public static final String STATE_SAVED_ADDRESSES = "saved-addresses";

    private IAddressSearch addressSearch = new NominatimAddressSearch(new NominatimWebInteraction());
    private AddressSearchStore selectedAddresses = new AddressSearchStore();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSelectedAddresses();
        setContentView(R.layout.activity_address_search);

        progressBar = (ProgressBar)findViewById(R.id.search_progress);
        stopSearchProgressBar();

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
                startSearchProgressBar();
            }
        });
        // search the found addresses when the text is changed.
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                selectedAddresses.search(s.toString());
            }
        });

        onSearchResult(selectedAddresses);
    }

    protected void startSearchProgressBar() {
        progressBar.setIndeterminate(true);
        progressBar.setProgress(10);
        progressBar.setMinimumWidth(progressBar.getHeight());
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void stopSearchProgressBar() {
        if (progressBar != null) {
            progressBar.setProgress(0);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onListFragmentInteraction(AddressSearchResult chosenAddress) {
        selectedAddresses.add(chosenAddress);
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
        selectedAddresses.notifyAboutChanges(observer);
        selectedAddresses.search("");
    }

    @Override
    public void onSearchError(int errorId) {
        new Dialog(this).alertError(errorId);
        stopSearchProgressBar();
    }

    @Override
    public void onSearchResult(IAddressSearch addressSearch) {
        stopSearchProgressBar();
        TextView noSearchResult = (TextView) findViewById(R.id.no_search_result);
        if (noSearchResult == null) {
            return;
        }
        if (addressSearch.size() == 0) {
            if (addressSearch == selectedAddresses) {
                noSearchResult.setText(R.string.search_no_local_result);
            } else {
                noSearchResult.setText(R.string.search_no_result);
            }
            noSearchResult.setVisibility(View.VISIBLE);
        } else {
            noSearchResult.setVisibility(View.GONE);
        }
    }

    @Override
    protected void menuOpenAddressSearch() {
    }

    @Override
    protected void onPause() {
        super.onPause();
        storeSelectedAddresses();
    }

    private void storeSelectedAddresses() {
        SharedPreferences settings = getState();
        SharedPreferences.Editor editor = settings.edit();
        try {
            JSONObject savedAddressesJSON = selectedAddresses.toJSON();
            String savedAddressesString = savedAddressesJSON.toString();
            editor.putString(STATE_SAVED_ADDRESSES, savedAddressesString);
            editor.commit();
        } catch (JSONException e) {
            log.printStackTrace(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSelectedAddresses();
    }

    private void loadSelectedAddresses() {
        SharedPreferences settings = getState();
        String savedAddressesString = settings.getString(STATE_SAVED_ADDRESSES, null);
        if (savedAddressesString != null) {
            try {
                JSONObject savedAddressesJSON = new JSONObject(savedAddressesString);
                selectedAddresses.loadFrom(savedAddressesJSON);
            } catch (JSONException e) {
                log.e("saved addresses", "I could not load the old saved addresses.");
                log.printStackTrace(e);
            }
        }
    }

    private SharedPreferences getState() {
        return getSharedPreferences("AddressSearchActivity", 0);
    }


}
