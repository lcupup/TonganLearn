package com.tongan.learn.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.tongan.learn.StudyActivity;
import com.tongan.learn.bean.TongAnBridge;

/**
 * @author lichao
 * @date 2019/5/22 9:15
 */
public class WebHelper {


    private WebView webView;
    private VideoImpl videoImpl;
    private MyWebChromeClient myWebChromeClient;



    public WebHelper() {

    }


    public WebHelper setWebView(WebView webView) {
        this.webView = webView;
        return this;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public WebHelper init(Activity activity) {
        if (webView != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setSupportMultipleWindows(true);

            // 设置 loaclStorage
            webSettings.setAppCacheEnabled(true);
            webSettings.setSupportZoom(true);
            webSettings.setTextZoom(100);
            webSettings.setDomStorageEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webSettings.setAllowFileAccess(true);
            webSettings.setAppCacheEnabled(true);
            webSettings.setAppCachePath(activity.getApplication().getCacheDir().getAbsolutePath());
            webSettings.setDefaultTextEncodingName("utf-8");
            webView.addJavascriptInterface(new TongAnBridge(activity, (StudyActivity) activity), "TongAnBridge");
            videoImpl = new VideoImpl(activity, webView);
            myWebChromeClient = new MyWebChromeClient(videoImpl);
            webView.setWebChromeClient(myWebChromeClient);
            webView.setWebViewClient(new MyWebClient());
            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });

        }

        return this;
    }

    public WebHelper LoadUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            webView.loadUrl(url);
        }
        return this;
    }

    public WebHelper setTitleListener(TitleListener titleListener) {
        myWebChromeClient.setTitleListener(titleListener);
        return this;
    }

    public Eventlnterceptor getEvent() {
        if (videoImpl != null) {
            return videoImpl;
        }
        return null;

    }


    public void onResume() {
        if (webView != null) {
            webView.onResume();
        }
    }

    public void onPause() {
        if (webView != null) {
            webView.onPause();
        }
    }

    public void onDestroy() {
        if (webView != null) {
//            CookieSyncManager.createInstance(context);
//            CookieSyncManager.getInstance().startSync();
//            CookieManager.getInstance().removeSessionCookie();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearCache(true);
            webView.destroy();
        }
    }
}