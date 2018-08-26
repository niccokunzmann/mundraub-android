package eu.quelltext.mundraub.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import eu.quelltext.mundraub.BuildConfig;
import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.MundraubBaseActivity;

public class AboutActivity extends MundraubBaseActivity {

    private Button buttonViewSource;
    private Button buttonViewFreedoms;
    private Button buttonViewIssues;
    private Button buttonViewMIT;
    private Button buttonViewGPL;
    private TextView textSelectedLicense;
    private Button buttonViewVersion;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        buttonViewSource = (Button) findViewById(R.id.button_view_source);
        buttonViewVersion = (Button) findViewById(R.id.button_view_source_version);
        buttonViewFreedoms = (Button) findViewById(R.id.button_view_freedoms);
        buttonViewIssues = (Button) findViewById(R.id.button_view_issues);
        buttonViewMIT = (Button) findViewById(R.id.button_mit);
        buttonViewGPL = (Button) findViewById(R.id.button_gpl);
        textSelectedLicense = (TextView) findViewById(R.id.text_selected_license);

        String versionText = getResources().getString(R.string.about_view_source_at_version);
        buttonViewVersion.setText(String.format(versionText, BuildConfig.VERSION_NAME, Settings.getShortHash()));

        buttonViewSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite(R.string.about_view_source_url);
            }
        });
        buttonViewVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlTemplate = getResources().getString(R.string.about_view_source_at_version_url);
                openWebsite(String.format(urlTemplate, Settings.COMMIT_HASH));
            }
        });
        buttonViewFreedoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite(R.string.about_view_four_freedoms_url);
            }
        });
        buttonViewIssues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite(R.string.about_view_issues_url);
            }
        });
        buttonViewGPL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLicense("license/LICENSE");
            }
        });
        buttonViewMIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLicense("map/license.txt");
            }
        });
    }

    private void selectLicense(String path) {
        // from https://stackoverflow.com/a/9544781
        BufferedReader reader = null;
        textSelectedLicense.setText("");
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(path), "UTF-8"));

            // do reading, usually loop until end of file reading
            String mLine;
            StringBuilder text = new StringBuilder();
            while ((mLine = reader.readLine()) != null) {
                //process line
                text.append(mLine);
                text.append("\n");
            }
            textSelectedLicense.setText(text.toString());
        } catch (IOException e) {
            log.printStackTrace(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.printStackTrace(e);
                }
            }
        }
    }

    private void openWebsite(int urlResourceId) {
        String url = getResources().getString(urlResourceId);
        openWebsite(url);
    }

    private void openWebsite(String url) {
        // from https://stackoverflow.com/a/3004542/1320237
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
