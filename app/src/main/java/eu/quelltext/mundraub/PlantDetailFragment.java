package eu.quelltext.mundraub;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.map.MapCache;
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
    private static final String OSM_COPYRIGHT_RIGHT_URL = "https://www.openstreetmap.org/copyright";

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
            updateInfosFromPlant();
            updateOnlineActivities();
        }
    }

    private void updateInfosFromPlant() {
        plant.setPictureToPlant((ImageView) rootView.findViewById(R.id.image_plant));
        ((TextView) rootView.findViewById(R.id.text_plant_category)).setText(plant.getCategory().getResourceId());
        ((TextView) rootView.findViewById(R.id.text_count)).setText(Integer.toString(plant.getCount()));
        ((TextView) rootView.findViewById(R.id.text_description)).setText(plant.getDescription());
        ((TextView) rootView.findViewById(R.id.text_latitude)).setText(Double.toString(plant.getLatitude()));
        ((TextView) rootView.findViewById(R.id.text_longitude)).setText(Double.toString(plant.getLongitude()));
        // map
        final ImageView mapView = (ImageView) rootView.findViewById(R.id.image_plant_map);
        //final LinearLayout mapElements = (LinearLayout) rootView.findViewById(R.id.plant_map);
        final TextView mapLicense = (TextView) rootView.findViewById(R.id.text_map_license);
        plant.setPictureToMap(mapView, new MapCache.Callback() {
            @Override
            public void onSuccess(File file) {
                mapLicense.setVisibility(View.VISIBLE);
            }
            @Override
            public void onFailure() {
                mapView.setImageResource(android.R.drawable.ic_dialog_map);
                mapLicense.setVisibility(View.GONE);
            }
        });
        (rootView.findViewById(R.id.text_map_license)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openURLInBrowser(OSM_COPYRIGHT_RIGHT_URL);
            }
        });
        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseMapPosition();
            }
        });
    }

    private void chooseMapPosition() {
        Intent intent = new Intent(context, ChooseMapPosition.class);
        intent.putExtra(ChooseMapPosition.ARG_PLANT_ID, plant.getId());
        context.startActivity(intent);
    }

    private void updateOnlineActivities() {
        updateButton(R.id.button_login, plant.online().mustLogin(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        });
        updateButton(R.id.button_upload, plant.online().canCreate(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (plant.shouldAskTheUserAboutPlacementBeforeUpload()) {
                    new Dialog(getContext()).askYesNo(
                            plant.getRepositionReason(),
                            R.string.ask_open_the_map,
                            new Dialog.YesNoCallback() {
                        @Override
                        public void yes() {
                            chooseMapPosition();
                        }

                        @Override
                        public void no() {
                            createPlant();
                        }
                    });
                } else {
                    createPlant();
                }
            }

            private void createPlant() {
                plant.online().create(updateOrShowError(R.string.success_plant_uploaded));
            }
        });
        updateButton(R.id.button_edit, plant.online().canUpdate(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plant.online().update(updateOrShowError(R.string.success_plant_updated));
            }
        });
        updateButton(R.id.button_view, plant.online().hasURL(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openURLInBrowser(plant.online().getURL());

            }
        });
        updateButton(R.id.button_delete, plant.online().canDelete(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Dialog(getContext()).askYesNo(
                        R.string.delete_plant_information,
                        R.string.ask_delete_plant,
                        new Dialog.YesNoCallback() {
                    @Override
                    public void yes() {
                        plant.online().delete(updateOrShowError(R.string.success_plant_deleted));
                    }
                    @Override
                    public void no() {}
                });
            }
        });

    }

    private void openURLInBrowser(String url) {
        // from https://stackoverflow.com/a/3004542/1320237
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private API.Callback updateOrShowError(final int successResourceId) {
        return new API.Callback() {
            @Override
            public void onSuccess() {
                updateOnlineActivities();
                new Dialog(getContext()).alertSuccess(successResourceId);
            }

            @Override
            public void onFailure(int errorResourceString) {
                new Dialog(getContext()).alertError(errorResourceString);
            }
        };
    }

    private void updateButton(int resourceId, boolean visible, View.OnClickListener onClickListener) {
        Button button = (Button) rootView.findViewById(resourceId);
        if (visible) {
            button.setOnClickListener(onClickListener);
        }
        button.setVisibility(visible? View.VISIBLE: View.GONE);
    }
}
