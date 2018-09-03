package eu.quelltext.mundraub.activities.map;

import android.webkit.ConsoleMessage;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.activities.MundraubBaseActivity;
import eu.quelltext.mundraub.map.MundraubProxy;

public class MapBaseActivity extends MundraubBaseActivity {

    protected WebView webView = null;
    private MundraubProxy apiProxy;

    protected void initializeWebView(int webViewId) {
        webView = (WebView) findViewById(webViewId);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // from https://stackoverflow.com/a/8846105/1320237
        // from https://stackoverflow.com/a/32587047/1320237
        //webSettings.setBuiltInZoomControls(true);
        //webSettings.setDisplayZoomControls(false);
        // from https://stackoverflow.com/a/6255353/1320237
        //webView.setVerticalScrollBarEnabled(true);
        //webView.setHorizontalScrollBarEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // from https://stackoverflow.com/a/30294054/1320237
                log.d("WebView", consoleMessage.sourceId() + " at line " +
                        consoleMessage.lineNumber() + ": " + consoleMessage.message());
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // TODO: check for API level 21? https://stackoverflow.com/a/47913011
                CookieSyncManager.getInstance().sync(); // from https://stackoverflow.com/a/8390280
            }
        });
        apiProxy = Settings.getMundraubMapProxy();
        getPermissions().INTERNET.askIfNotGranted();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            log.d("start", "proxy1");
            apiProxy.start();
            log.d("start", "proxy2");
        } catch (IOException e) {
            log.printStackTrace(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        apiProxy.stop();
    }
}
