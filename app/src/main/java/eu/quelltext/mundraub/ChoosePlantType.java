/*
  Most of this is copied from
  https://www.simplifiedcoding.net/android-recyclerview-cardview-tutorial/
 */
package eu.quelltext.mundraub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.plant.PlantCategory;

public class ChoosePlantType extends AppCompatActivity {

    private RecyclerView plantTypesView;

    class PlantCategoryResult {
        private final AppCompatActivity activity;
        PlantCategoryResult(AppCompatActivity activity) {
            this.activity = activity;
        }
        public void resolve(PlantCategory category) {
            Intent resultIntent = new PlantCategory.Intent(category);
            // from https://stackoverflow.com/questions/920306/sending-data-back-to-the-main-activity-in-android#947560
            this.activity.setResult(Activity.RESULT_OK, resultIntent);
            this.activity.finish();
        }
    }

    PlantCategoryResult getResult() {
        return new PlantCategoryResult(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_plant_type);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // TODO: set title of toolbar
        plantTypesView = (RecyclerView) findViewById(R.id.plant_types);
        // from https://stackoverflow.com/q/29141729
        plantTypesView.setLayoutManager(new LinearLayoutManager(this));

        fillInPlantTypes();
    }

    private void fillInPlantTypes() {
        List<PlantCategory> categories = new ArrayList<PlantCategory>(PlantCategory.all());
        // TODO: Sort by name
        PlantCategoryAdapter adapter = new PlantCategoryAdapter(this, categories);
        plantTypesView.setAdapter(adapter);
    }

    class PlantCategoryViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTitle;
        private Button choosePlantType;

        public PlantCategoryViewHolder(View itemView) {
            super(itemView);

            textViewTitle = (TextView)itemView.findViewById(R.id.textViewTitle);
            choosePlantType = (Button) itemView.findViewById(R.id.button_choose_plant_type);
        }

        public void setCategory(final PlantCategory category, final PlantCategoryResult result) {
            textViewTitle.setText(category.getResourceId());
            choosePlantType.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Log.d("onClick", category.toString());
                    result.resolve(category);
                }
            });
        }
    }

    class PlantCategoryAdapter extends RecyclerView.Adapter<PlantCategoryViewHolder> {
        private final List<PlantCategory> categories;
        private final ChoosePlantType mCtx;

        public PlantCategoryAdapter(ChoosePlantType mCtx, List<PlantCategory> categories) {
            this.mCtx = mCtx;
            this.categories = categories;
        }
        @Override
        public PlantCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //inflating and returning our view holder
            LayoutInflater inflater = LayoutInflater.from(mCtx);
            View view = inflater.inflate(R.layout.layout_plant_category, null);
            return new PlantCategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PlantCategoryViewHolder holder, int position) {
            //getting the product of the specified position
            PlantCategory category = categories.get(position);

            //binding the data with the viewholder views
            holder.setCategory(category, mCtx.getResult());
        }
        @Override
        public int getItemCount() {
            return categories.size();
        }
    }

}
