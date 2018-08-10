package eu.quelltext.mundraub;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
        final ImageView mapView = (ImageView) rootView.findViewById(R.id.image_plant_map);
        plant.setPictureToMap(mapView, new MapCache.Callback() {
            @Override
            public void onSuccess(File file) {
                Log.d("updateInfosFromPlant", "got image success.");
                mapView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure() {
                Log.d("updateInfosFromPlant", "got image fail.");
                mapView.setVisibility(View.GONE);
            }
        });
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
                // from https://stackoverflow.com/a/3004542/1320237
                String url = plant.online().getURL();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        updateButton(R.id.button_delete, plant.online().canDelete(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plant.online().delete(updateOrShowError(R.string.success_plant_deleted));
            }
        });

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
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
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
}
