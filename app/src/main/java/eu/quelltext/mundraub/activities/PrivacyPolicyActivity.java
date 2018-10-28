package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;

public class PrivacyPolicyActivity extends WebViewBaseActivity {

    private static final String PRIVACY_FOLDER = "privacy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        initializeWebView(R.id.web_view);
        webView.loadUrl(getUrl());
        Settings.setUserHasReadThePrivacyPolicy();
    }

    @NonNull
    private String getUrl() {
        String fileName = "en.html";
        try {
            List<String> files = Arrays.asList(getAssets().list(PRIVACY_FOLDER));
            String language = Locale.getDefault().getLanguage();
            String country = Locale.getDefault().getCountry();
            String languageFileName = language.toLowerCase() + ".html";
            String languageAndCountryFileName = language.toLowerCase() + "_" + country.toUpperCase() + ".html";
            if (files.contains(languageAndCountryFileName)) {
                fileName = languageAndCountryFileName;
            } else if (files.contains(languageFileName)) {
                fileName = languageFileName;
            }
        } catch (IOException e) {
            log.printStackTrace(e);
        }
        return "file:///android_asset/privacy/" + fileName;
    }

    @Override
    protected void menuOpenPrivacyPolicy() {
    }
}
