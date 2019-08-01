package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;

/*
    The LocalizedWebViewActivity uses one folder which contains the files
    to localize. It chooses the correct localization from this folder.

    Files in this folder must be named
    - LANG.html
    - LANG_COUNTRY.html
    Examples:
    - en.html
    - de.html
    - de_AT.html
 */
public abstract class LocalizedWebViewActivity extends WebViewBaseActivity {

    abstract public String folderName();

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
            List<String> files = Arrays.asList(getAssets().list(folderName()));
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
        return "file:///android_asset/" + folderName() + "/" + fileName;
    }

}
