package eu.quelltext.mundraub.activities;

import android.os.Bundle;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;

public class PrivacyPolicyActivity extends WebViewBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        initializeWebView(R.id.web_view);
        webView.loadUrl("file:///android_asset/privacy/en.html");
        Settings.setUserHasReadThePrivacyPolicy();
    }

    @Override
    protected void menuOpenPrivacyPolicy() {
    }
}
