package com.bradz.dotdashdot.randomreddit.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bradz.dotdashdot.randomreddit.R;
import com.bradz.dotdashdot.randomreddit.utils.Statics;

/**
 * Created by Mauve3 on 8/31/16.
 */
public class LoginActivity extends AppCompatActivity {

    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        mWebView = (WebView) findViewById(R.id.web_login);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println(url);
                if(url.contains("code=")){
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("url",url);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                    return true;
                } else {
                    return false;
                }
            }
        });
        mWebView.loadUrl(Statics.REDDIT_BASE_URL + "?" +
                "client_id=" + Statics.REDDIT_CLIENT_ID + "&response_type=code&" +
                "duration=permanent&state=1234abcd&" +
                "redirect_uri=" + Statics.REDDIT_REDIRECT_URL + "&" +
                "scope=" + Statics.REDDIT_SCOPE);
    }
}
