package eu.quelltext.mundraub;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button button_add_new_plant;
    private MainActivity me = this;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_add_new_plant = (Button) findViewById(R.id.button_add_new_plant);
        button_add_new_plant.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // form https://developer.android.com/training/basics/firstapp/starting-activity#java
                Intent intent = new Intent(me, NewPlantActivity.class);
                startActivity(intent);
            }
        });
  }
}
