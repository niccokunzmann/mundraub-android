package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.dummy.DummyContent;

public class AddressSearchActivity extends MundraubBaseActivity implements AddressSearchResultFragment.OnListFragmentInteractionListener {

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
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
