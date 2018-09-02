package eu.quelltext.mundraub.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.FirstActivity;
import eu.quelltext.mundraub.plant.Plant;

/**
 * An activity representing a list of Plants. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PlantDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PlantListActivity extends FirstActivity {

    private static final String ARG_RECYCLER_VIEW_STATE = "recycler_view_state";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private PlantListActivity me = this;
    RecyclerView recyclerView;
    private Parcelable recyclerViewState = null;
    private SimpleItemRecyclerViewAdapter adapter;
    private boolean doNotUpdateRecyclerViewAgainAfterCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // from https://developer.android.com/training/basics/firstapp/starting-activity#java
                Intent intent = new Intent(me, NewPlantActivity.class);
                startActivity(intent);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.plant_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        recyclerView = (RecyclerView) findViewById(R.id.plant_list);
        recyclerViewState = savedInstanceState != null && savedInstanceState.containsKey(ARG_RECYCLER_VIEW_STATE) ?
                                savedInstanceState.getParcelable(ARG_RECYCLER_VIEW_STATE) : null;
        adapter = new SimpleItemRecyclerViewAdapter(this, mTwoPane);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        loadRecyclerViewState();
        doNotUpdateRecyclerViewAgainAfterCreate = true;

    }
    @Override
    protected void onResume() {
        super.onResume();
        assert recyclerView != null;
        if (!doNotUpdateRecyclerViewAgainAfterCreate) {
            updateRecyclerView();
        }
        doNotUpdateRecyclerViewAgainAfterCreate = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // restore recyclerView from https://stackoverflow.com/a/45436460
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(ARG_RECYCLER_VIEW_STATE, recyclerViewState);
    }


    private void updateRecyclerView() {
        log.d("DEBUG", "updateRecyclerView");
        loadRecyclerViewState();
        adapter.update();
    }

    private void loadRecyclerViewState() {
        if (recyclerViewState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final PlantListActivity mParentActivity;
        private List<Plant> plants;
        private final boolean mTwoPane;
        private final Logger.Log log;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Plant plant = (Plant) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(PlantDetailFragment.ARG_PLANT_ID, plant.getId());
                    PlantDetailFragment fragment = new PlantDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.plant_detail_container, fragment)
                            .commit();
                    fragment.isDoublePane();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PlantDetailActivity.class);
                    intent.putExtra(PlantDetailActivity.ARG_PLANT_ID, plant.getId());

                    context.startActivity(intent);
                }
            }
        };

        public void update() {
            load();
            log.d("RECYCLER", "changed start");
            notifyDataSetChanged();
            log.d("RECYCLER", "changed stop");
        }

        private void load() {
            log.d("RECYCLER", "load start");
            plants = Plant.all();
            Collections.sort(plants);
            log.d("RECYCLER", "load stop");
        }

        SimpleItemRecyclerViewAdapter(PlantListActivity parent,
                                      boolean twoPane) {
            log = Logger.newFor(this);
            mParentActivity = parent;
            mTwoPane = twoPane;
            load();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.plant_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Plant plant = plants.get(position);
            holder.fillFromPlant(plant);
            if (position == 0) {
                holder.showDate(plant);
            } else {
                Plant lastPlant = plants.get(position - 1);
                if (plant.getCreationDay().equals(lastPlant.getCreationDay())) {
                    holder.hideDate();
                } else {
                    holder.showDate(plant);
                }
            }

        }

        @Override
        public int getItemCount() {
            return plants.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageView plantImage;
            private final TextView plantCategoryText;
            private final LinearLayout dateContainer;
            private final String dateFormat;
            private final TextView dateText;
            private final ImageView imageUploaded;

            ViewHolder(View view) {
                super(view);
                plantImage = (ImageView) view.findViewById(R.id.image_plant);
                plantCategoryText = (TextView) view.findViewById(R.id.plant_category);
                dateContainer = (LinearLayout) view.findViewById(R.id.new_day);
                dateFormat = view.getResources().getString(R.string.plant_list_date_format);
                dateText = (TextView) view.findViewById(R.id.date);
                imageUploaded = (ImageView) view.findViewById(R.id.image_uploaded);

            }

            public void fillFromPlant(Plant plant) {
                plant.setPictureToPlant(plantImage);
                plantCategoryText.setText(plant.getCategory().getResourceId());
                String textWithCount = Integer.toString(plant.getCount()) + "x " + plantCategoryText.getText().toString();
                plantCategoryText.setText(textWithCount);
                itemView.setTag(plant);
                itemView.setOnClickListener(mOnClickListener);
                imageUploaded.setVisibility(
                        plant.online().isPublished() ? View.VISIBLE : View.GONE);
            }

            public void showDate(Plant plant) {
                Date date = plant.getCreationDay();
                String text = DateFormat.format(dateFormat, date).toString();
                dateText.setText(text);
                dateContainer.setVisibility(View.VISIBLE);
            }

            public void hideDate() {
                // from https://stackoverflow.com/a/7348547/1320237
                dateContainer.setVisibility(View.GONE);
            }
        }
    }
}
