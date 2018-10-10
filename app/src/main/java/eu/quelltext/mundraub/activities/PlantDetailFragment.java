package eu.quelltext.mundraub.activities;

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

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.map.ChooseMapPosition;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.map.MapCache;
import eu.quelltext.mundraub.plant.Plant;
import eu.quelltext.mundraub.plant.PlantCategory;

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
    private static Logger.Log log = Logger.newFor("PlantDetailFragment");

    /**
     * The dummy content this fragment is presenting.
     */
    private Plant plant = null;
    private View rootView = null;
    private Context context = null;
    private boolean doublePane;

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

        Button btn_edit = (Button) rootView.findViewById(R.id.btn_edit);
        Button btn_delete = (Button) rootView.findViewById(R.id.btn_delete);

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewPlantActivity.class);
                intent.putExtra(NewPlantActivity.ARG_PLANT_ID, plant.getId());
                context.startActivity(intent);
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Dialog(context).askAndDeleteThePlantAndFinishTheActivity(plant);
            }
        });

        updateViewFromPlant();
        if (savedInstanceState != null) {
            doublePane = savedInstanceState.getBoolean("doublePane", true);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("doublePane", doublePane);
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
            updateButton(R.id.button_edit, doublePane, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, NewPlantActivity.class);
                    intent.putExtra(NewPlantActivity.ARG_PLANT_ID, plant.getId());
                    context.startActivity(intent);
                }
            });
        }
    }

    private void updateInfosFromPlant() {
        plant.setPictureToPlant((ImageView) rootView.findViewById(R.id.image_plant));
        ((TextView) rootView.findViewById(R.id.text_plant_category)).setText(plant.getCategory().getResourceId());
        ((TextView) rootView.findViewById(R.id.text_count)).setText(Integer.toString(plant.getCount()));
        ((TextView) rootView.findViewById(R.id.text_description)).setText(plant.getDescription());
        Plant.Position position = plant.getPosition();
        TextView textLongitude = ((TextView) rootView.findViewById(R.id.text_longitude));
        TextView textLatitude = ((TextView) rootView.findViewById(R.id.text_latitude));
        if (position.isValid()) {
            textLongitude.setText(Double.toString(plant.getLongitude()));
            textLatitude.setText(Double.toString(plant.getLatitude()));
        } else {
            textLongitude.setText(R.string.position_not_set);
            textLatitude.setText(R.string.position_not_set);
        }
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
                plant.online().openLoginFrom(context);
            }
        });
        updateButton(R.id.button_upload, plant.online().canCreate(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (plant.shouldAskTheUserAboutPlacementBeforeUpload()) {
                    new Dialog(getActivity()).askYesNo(
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
                askForUploadIfCategoryChanges(new Dialog.YesNoCallback() {
                    @Override
                    public void yes() {
                        plant.online().create(updateOrShowError(R.string.success_plant_uploaded));
                    }
                    @Override
                    public void no() {

                    }
                });

            }
        });
        updateButton(R.id.button_upload_changes, plant.online().canUpdate(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForUploadIfCategoryChanges(new Dialog.YesNoCallback() {
                    @Override
                    public void yes() {
                        plant.online().update(updateOrShowError(R.string.success_plant_updated));
                    }

                    @Override
                    public void no() {

                    }
                });
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
                new Dialog(getActivity()).askYesNo(
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

    private String resourceIdToString(int resourceId) {
        return getContext().getResources().getString(resourceId);
    }

    private void askForUploadIfCategoryChanges(Dialog.YesNoCallback yesNoCallback) {
        API api = plant.online().api();
        PlantCategory category = plant.getCategory();
        PlantCategory categoryOnAPI = plant.getCategory().on(api);
        if (category != categoryOnAPI) {
            String reason = resourceIdToString(R.string.reason_category_changes);
            String categoryName = resourceIdToString(api.nameResourceId());
            String sourceCategory = resourceIdToString(plant.getCategory().getResourceId());
            String targetCategory = resourceIdToString(plant.getCategory().on(api).getResourceId());
            reason = String.format(reason, categoryName, sourceCategory, targetCategory);
            new Dialog(this.getActivity()).askYesNo(reason, R.string.ask_upload_ok, yesNoCallback);
        } else {
            yesNoCallback.yes();
        }
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
                new Dialog(getActivity()).alertSuccess(successResourceId);
            }

            @Override
            public void onFailure(int errorResourceString) {
                new Dialog(getActivity()).alertError(errorResourceString);
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

    /*
        This fragment is show as the only thing on the screen.
     */
    public void isSinglePane() {
        doublePane = false;
    }
    /*
        This fragment is shown with a left bar to choose plants.
     */
    public void isDoublePane() {
        doublePane = true;
    }
}
