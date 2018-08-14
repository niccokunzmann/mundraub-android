package eu.quelltext.mundraub;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import eu.quelltext.mundraub.api.API;
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
                    askYesNo(plant.getRepositionReason(), R.string.ask_open_the_map, new YesNoCallback() {
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
                plant.online().delete(updateOrShowError(R.string.success_plant_deleted));
            }
        });

    }

    private void askYesNo(int repositionReason, int ask_open_the_map, final YesNoCallback callback) {
        // from https://stackoverflow.com/a/2478662
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        callback.yes();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        callback.no();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = Helper.getAlertBuilder(this.getContext());;
        builder .setMessage(getResources().getString(repositionReason) + "\n" + getResources().getString(ask_open_the_map))
                .setNegativeButton(R.string.no, dialogClickListener)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .show();
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
                alertSuccess(successResourceId);
            }

            @Override
            public void onFailure(int errorResourceString) {
                alertError(errorResourceString);
            }
        };
    }

    private void alertError(int errorResourceString) {
        alert(R.string.error, android.R.drawable.ic_dialog_alert, errorResourceString);
    }

    private void alertSuccess(int successResourceString) {
        alert(R.string.success, android.R.drawable.ic_dialog_info, successResourceString);
    }

    private void alert(int title, int icon, int message) {
        // from https://stackoverflow.com/a/2115770/1320237
        AlertDialog.Builder builder = Helper.getAlertBuilder(this.getContext());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(icon)
                .show();
    }

    private void updateButton(int resourceId, boolean visible, View.OnClickListener onClickListener) {
        Button button = (Button) rootView.findViewById(resourceId);
        if (visible) {
            button.setOnClickListener(onClickListener);
        }
        button.setVisibility(visible? View.VISIBLE: View.GONE);
    }

    interface YesNoCallback {
        void yes();
        void no();
    }
}
