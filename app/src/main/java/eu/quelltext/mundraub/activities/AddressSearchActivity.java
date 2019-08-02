package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.dummy.DummyContent;

public class AddressSearchActivity extends AppCompatActivity implements AddressSearchResultFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_search);
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
