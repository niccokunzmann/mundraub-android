package eu.quelltext.mundraub;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import eu.quelltext.mundraub.plant.Plant;

/**
 * A fragment representing a single Plant detail screen.
 * This fragment is either contained in a {@link PlantListActivity}
 * in two-pane mode (on tablets) or a {@link PlantDetailActivity}
 * on handsets.
 */
public class PlantDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_PLANT_ID = "plant_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Plant plant = null;
    private View rootView = null;
    private Context context = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlantDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_PLANT_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            plant = Plant.withId(getArguments().getString(ARG_PLANT_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(plant.getDetailsTitle());
            }

        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.plant_detail, container, false);
        context = container.getContext();
        updateViewFromPlant();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViewFromPlant();
    }

    private void updateViewFromPlant() {
        if (plant != null && rootView != null && context != null) {
            plant.setImageOf((ImageView) rootView.findViewById(R.id.image_plant));
            ((TextView) rootView.findViewById(R.id.text_plant_category)).setText(plant.getCategory().getResourceId());
            ((TextView) rootView.findViewById(R.id.text_count)).setText(Integer.toString(plant.getCount()));
            ((TextView) rootView.findViewById(R.id.text_description)).setText(plant.getDescription());
            ((TextView) rootView.findViewById(R.id.text_latitude)).setText(Double.toString(plant.getLatitude()));
            ((TextView) rootView.findViewById(R.id.text_longitude)).setText(Double.toString(plant.getLongitude()));
            Button uploadButton = (Button) rootView.findViewById(R.id.button_upload);
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    // intent.putExtra(NewPlantActivity.ARG_PLANT_ID, plantId);
                    context.startActivity(intent);
                }
            });
        }
    }
}
