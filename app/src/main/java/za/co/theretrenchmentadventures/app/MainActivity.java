package za.co.theretrenchmentadventures.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String HOST = "www.the-retrenchment-adventures.co.za";
    private static final String ALT_HOST = "the-retrenchment-adventures.co.za";
    private static final String HOME_URL = "https://" + HOST + "/";
    private static final String PRODUCTS_URL = "https://" + HOST + "/service-booking/";
    private static final String EVENTS_URL = "https://" + HOST + "/events-list-style-with-search-box/";
    private static final String GALLERY_URL = "https://" + HOST + "/gallery/";
    private static final String BOOK_URL = "https://" + HOST + "/contact-us/";

    private static final String APP_CSS =
            "footer,#colophon,#site-footer,.site-footer,.tra-footer,.footer-widgets,.footer-bottom," +
            ".footer-main,.footer-inner,.site-info,[role='contentinfo']{" +
            "display:none!important;visibility:hidden!important;height:0!important;min-height:0!important;" +
            "max-height:0!important;margin:0!important;padding:0!important;overflow:hidden!important;}";

    private FrameLayout webContainer;
    private LinearLayout introOverlay;
    private ImageView introLogo;
    private ProgressBar introProgress;
    private TextView introStatus;
    private Button retryButton;
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private long introStartedAt;
    private boolean firstPageReady = false;

    private LinearLayout navHome, navProducts, navEvents, navGallery, navBook;

    private static final int FILE_CHOOSER_REQUEST = 4101;
    private static final long INTRO_MIN_MS = 850L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureSystemBars();
        setContentView(R.layout.activity_main);

        webContainer = findViewById(R.id.web_container);
        introOverlay = findViewById(R.id.intro_overlay);
        introLogo = findViewById(R.id.intro_logo);
        introProgress = findViewById(R.id.intro_progress);
        introStatus = findViewById(R.id.intro_status);
        retryButton = findViewById(R.id.retry_button);

        navHome = findViewById(R.id.nav_home);
        navProducts = findViewById(R.id.nav_products);
        navEvents = findViewById(R.id.nav_events);
        navGallery = findViewById(R.id.nav_gallery);
        navBook = findViewById(R.id.nav_book);

        setupBottomNavigation();
        startIntroAnimation();

        retryButton.setOnClickListener(v -> {
            showLoadingState();
            destroyWebView();
            webContainer.post(this::createWebViewSafely);
        });

        // Let Android draw the native frame first; then create WebView.
        webContainer.post(this::createWebViewSafely);
    }

    private void configureSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        window.setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void startIntroAnimation() {
        introStartedAt = System.currentTimeMillis();
        introLogo.setAlpha(0f);
        introLogo.setScaleX(0.94f);
        introLogo.setScaleY(0.94f);
        introLogo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(450L).start();
    }

    private void createWebViewSafely() {
        if (isFinishing() || isDestroyed() || webView != null) {
            return;
        }

        try {
            WebView candidate = new WebView(this);
            candidate.setBackgroundColor(Color.WHITE);
            candidate.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            webContainer.addView(candidate);
            webView = candidate;
            configureWebView(candidate);
            loadInitialUrl();
        } catch (Throwable error) {
            showNativeError("Android WebView could not start. Update Android System WebView or Chrome, then tap Retry.");
        }
    }

    private void configureWebView(WebView view) {
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSafeBrowsingEnabled(true);
        settings.setUserAgentString(settings.getUserAgentString() + " TRA-Android/1.0.0");

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(view, true);

        view.setWebViewClient(new TraWebViewClient());
        view.setWebChromeClient(new TraWebChromeClient());
        view.setDownloadListener(createDownloadListener());

    }

    private void loadInitialUrl() {
        Uri incoming = getIntent() != null ? getIntent().getData() : null;
        if (incoming != null && isTrustedWebUri(incoming)) {
            webView.loadUrl(incoming.toString());
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> loadTrustedUrl(HOME_URL));
        navProducts.setOnClickListener(v -> loadTrustedUrl(PRODUCTS_URL));
        navEvents.setOnClickListener(v -> loadTrustedUrl(EVENTS_URL));
        navGallery.setOnClickListener(v -> loadTrustedUrl(GALLERY_URL));
        navBook.setOnClickListener(v -> loadTrustedUrl(BOOK_URL));
        setSelectedNav(navHome);
    }

    private void loadTrustedUrl(String url) {
        if (webView == null) {
            return;
        }
        webView.loadUrl(url);
    }

    private void setSelectedNav(View selected) {
        View[] items = {navHome, navProducts, navEvents, navGallery, navBook};
        int active = getColor(R.color.tra_blue);
        int muted = getColor(R.color.tra_muted);

        for (View item : items) {
            boolean isSelected = item == selected;
            item.setSelected(isSelected);
            if (item instanceof LinearLayout) {
                LinearLayout box = (LinearLayout) item;
                for (int i = 0; i < box.getChildCount(); i++) {
                    View child = box.getChildAt(i);
                    if (child instanceof ImageView) {
                        ((ImageView) child).setColorFilter(isSelected ? active : muted);
                    } else if (child instanceof TextView) {
                        ((TextView) child).setTextColor(isSelected ? active : muted);
                    }
                }
            }
        }
    }

    private void updateSelectedNav(String url) {
        if (url == null) return;
        String lower = url.toLowerCase(Locale.ROOT);
        if (lower.contains("/service-booking/")) {
            setSelectedNav(navProducts);
        } else if (lower.contains("/events-list-style-with-search-box/") || lower.contains("/event/")) {
            setSelectedNav(navEvents);
        } else if (lower.contains("/gallery/")) {
            setSelectedNav(navGallery);
        } else if (lower.contains("/contact-us/") || lower.contains("booking")) {
            setSelectedNav(navBook);
        } else {
            setSelectedNav(navHome);
        }
    }

    private void injectAppOnlyCss(WebView view) {
        String script =
                "(function(){" +
                "var id='tra-android-app-css';" +
                "var old=document.getElementById(id);" +
                "if(!old){var s=document.createElement('style');s.id=id;s.textContent=" +
                JSONObject.quote(APP_CSS) +
                ";(document.head||document.documentElement).appendChild(s);}" +
                "return true;" +
                "})();";
        view.evaluateJavascript(script, null);
    }

    private void markFirstPageReady() {
        firstPageReady = true;
        long elapsed = System.currentTimeMillis() - introStartedAt;
        long remaining = Math.max(0L, INTRO_MIN_MS - elapsed);
        introOverlay.postDelayed(() -> {
            if (!firstPageReady || isFinishing()) return;
            introOverlay.animate()
                    .alpha(0f)
                    .setDuration(250L)
                    .withEndAction(() -> {
                        introOverlay.setVisibility(View.GONE);
                        introOverlay.setAlpha(1f);
                    })
                    .start();
        }, remaining);
    }

    private void showLoadingState() {
        firstPageReady = false;
        introOverlay.setVisibility(View.VISIBLE);
        introOverlay.setAlpha(1f);
        introProgress.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.GONE);
        introStatus.setText(R.string.loading);
        introStartedAt = System.currentTimeMillis();
    }

    private void showNativeError(String message) {
        introOverlay.setVisibility(View.VISIBLE);
        introOverlay.setAlpha(1f);
        introProgress.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
        introStatus.setText(message);
    }

    private boolean isTrustedWebUri(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        String host = uri.getHost();
        return "https".equalsIgnoreCase(scheme) &&
                (HOST.equalsIgnoreCase(host) || ALT_HOST.equalsIgnoreCase(host));
    }

    private boolean handleExternalUri(Uri uri) {
        if (uri == null) return true;

        if (isTrustedWebUri(uri)) {
            return false;
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (scheme.equals("http") || scheme.equals("https") ||
                scheme.equals("tel") || scheme.equals("mailto") ||
                scheme.equals("sms") || scheme.equals("geo") ||
                scheme.equals("market")) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (ActivityNotFoundException ignored) {
                Toast.makeText(this, "No app is available to open this link.", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    private DownloadListener createDownloadListener() {
        return (url, userAgent, contentDisposition, mimeType, contentLength) -> {
            if (url == null || url.isEmpty()) return;
            try {
                String filename = URLUtil.guessFileName(url, contentDisposition, mimeType);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                String cookie = CookieManager.getInstance().getCookie(url);
                if (cookie != null) request.addRequestHeader("Cookie", cookie);
                request.setTitle(filename);
                request.setDescription("Downloading from The Retrenchment Adventures");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(
                        this, Environment.DIRECTORY_DOWNLOADS, filename
                );
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
                Toast.makeText(this, "Download started.", Toast.LENGTH_SHORT).show();
            } catch (Exception error) {
                Toast.makeText(this, "This file could not be downloaded.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private class TraWebChromeClient extends WebChromeClient {
        @Override
        public boolean onShowFileChooser(
                WebView webView,
                ValueCallback<Uri[]> filePathCallbackNew,
                FileChooserParams fileChooserParams
        ) {
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(null);
            }
            filePathCallback = filePathCallbackNew;

            Intent intent;
            try {
                intent = fileChooserParams.createIntent();
            } catch (Exception error) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
            }

            try {
                startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                return true;
            } catch (ActivityNotFoundException error) {
                filePathCallback = null;
                Toast.makeText(MainActivity.this, "No file picker is available.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    private class TraWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleExternalUri(request.getUrl());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleExternalUri(Uri.parse(url));
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
            injectAppOnlyCss(view);
            updateSelectedNav(url);
            markFirstPageReady();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            injectAppOnlyCss(view);
            updateSelectedNav(url);
            markFirstPageReady();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (request.isForMainFrame()) {
                showNativeError("The website could not be reached. Check your internet connection and tap Retry.");
            }
        }

        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            destroyWebView();
            showNativeError("The website renderer stopped. Tap Retry to reopen the app safely.");
            return true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Uri uri = intent != null ? intent.getData() : null;
        if (webView != null && isTrustedWebUri(uri)) {
            webView.loadUrl(uri.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != FILE_CHOOSER_REQUEST || filePathCallback == null) {
            return;
        }

        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
        }

        filePathCallback.onReceiveValue(results);
        filePathCallback = null;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    private void destroyWebView() {
        if (webView == null) return;
        try {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webContainer.removeView(webView);
            webView.removeAllViews();
            webView.destroy();
        } catch (Throwable ignored) {
        } finally {
            webView = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
        destroyWebView();
        super.onDestroy();
    }
}
