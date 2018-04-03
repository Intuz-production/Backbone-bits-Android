//  The MIT License (MIT)

//  Copyright (c) 2018 Intuz Solutions Pvt Ltd.

//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
//  (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
//  merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:

//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
//  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.backbonebits;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.backbonebits.customviews.BBCustomBoldTextView;
import com.backbonebits.requestresponse.BBVideoHelpRequestResponse;
import com.backbonebits.webservices.BBWebServiceCaller;
import com.backbonebits.webviewVideo.VideoEnabledWebChromeClient;
import com.backbonebits.webviewVideo.VideoEnabledWebView;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BBVideoHelperActivity extends Activity {
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    BBCustomBoldTextView imgBackBtn;
    ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bb_activity_video_helper);
        webView = findViewById(R.id.webView);
        pbar = findViewById(R.id.progressBar1);
        imgBackBtn = findViewById(R.id.imgBackBtn);
        imgBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(BBVideoHelperActivity.this, Backbonebits.class);
                startActivity(i);
                finish();
                overridePendingTransition(0, 0);

            }
        });
        getHelperVideo();
    }

    private void getHelperVideo() {
        try {
            if (!BBUtils.isNetworkAvailable(BBVideoHelperActivity.this)) {
                BBUtils.showToast(BBVideoHelperActivity.this, getResources().getString(R.string.no_internet_connection_bb));
            } else

            {
                BBUtils.showProgress(BBVideoHelperActivity.this);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();

                Call<BBVideoHelpRequestResponse> call = service.getVideoHelper(BBUtils.Key, BBUtils.packageName, "video", BBUtils.version);
                call.enqueue(new Callback<BBVideoHelpRequestResponse>() {

                    @Override
                    public void onResponse(Call<BBVideoHelpRequestResponse> call, Response<BBVideoHelpRequestResponse> response) {
                        BBUtils.hideProgress();
                        try {
                            if (response.isSuccessful()) {
                                BBVideoHelpRequestResponse result = response.body();
                                if (result.getStatus() == 1) {

                                    if (result.getData().getVideo().getTutorial_video() != null && !result.getData().getVideo().getTutorial_video().equalsIgnoreCase("")) {

                                        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
                                        ViewGroup videoLayout = findViewById(R.id.videoLayout); // Your own view, read class comments
                                        //noinspection all
                                        View loadingView = getLayoutInflater().inflate(R.layout.bb_view_loading_video, null); // Your own view, read class comments
                                        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
                                        {
                                            // Subscribe to standard events, such as onProgressChanged()...
                                            @Override
                                            public void onProgressChanged(WebView view, int progress) {
                                                // Your code...
                                            }
                                        };
                                        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
                                            @Override
                                            public void toggledFullscreen(boolean fullscreen) {
                                                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                                                if (fullscreen) {
                                                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                                                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                                                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                                                    getWindow().setAttributes(attrs);
                                                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                                                        //noinspection all
                                                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                                                    }
                                                } else {
                                                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                                                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                                                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                                                    getWindow().setAttributes(attrs);
                                                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                                                        //noinspection all
                                                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                                                    }
                                                }

                                            }
                                        });
                                        webView.setWebChromeClient(webChromeClient);
                                        // Call private class InsideWebViewClient
                                        webView.setWebViewClient(new InsideWebViewClient());
                                        if (result.getData().getVideo().getVideo_type().equalsIgnoreCase("youtube")) {

                                            String videoURL = result.getData().getVideo().getTutorial_video();
                                            String url = null;

                                            if(!videoURL.contains("watch?v="))
                                            {
                                                Pattern regex = Pattern.compile("http://(?:www\\.)?youtu(?:\\.be/|be\\.com/(?:watch\\?v=|v/|embed/|user/(?:[\\w#]+/)+))([^&#?\n]+)");
                                                Matcher regexMatcher = regex.matcher(videoURL);
                                                if (regexMatcher.find()) {
                                                    String VideoID = regexMatcher.group(1);
                                                    url = "https://youtu.be/embed/+"+VideoID;
                                                }
                                                else {
                                                   String  VideoID = videoURL.replace("https://youtu.be/", "");
                                                    url = "https://youtu.be/embed/"+VideoID;
                                                }

                                            }
                                            else
                                            {
                                                url = videoURL.replace("watch?v=", "embed/").replace("&feature=youtu.be","");
                                            }
                                            webView.loadUrl(url);

                                        } else if (result.getData().getVideo().getVideo_type().equalsIgnoreCase("vimeo")) {

                                            String vimeourl = result.getData().getVideo().getTutorial_video().replace("https://vimeo.com/", "");

                                            webView.loadUrl("https://player.vimeo.com/video/" + vimeourl + "?autoplay=1&loop=1&title=0&byline=0&portrait=0&badge=0");

                                        } else {
                                            webView.loadUrl(result.getData().getVideo().getTutorial_video());

                                        }
                                    }


                                } else {
                                    Toast.makeText(BBVideoHelperActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                BBUtils.hideProgress();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<BBVideoHelpRequestResponse> call, Throwable t) {
                        Log.v("onFailure", "onFailure");
                        BBUtils.showToast(BBVideoHelperActivity.this, getResources().getString(R.string.server_error_bb));
                        BBUtils.hideProgress();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try{
                pbar.setVisibility(View.VISIBLE);
                view.loadUrl(url);
            view.loadUrl(url);
            } catch (Exception ex) {

            }
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

            pbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        try{
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed()) {
            if (webView.canGoBack()) {
                webView.goBack();
                Intent i = new Intent(BBVideoHelperActivity.this, Backbonebits.class);
                startActivity(i);
                finish();
                overridePendingTransition(0, 0);

            } else {
                // Standard back button implementation (for example this could close the app)
                super.onBackPressed();
                Intent i = new Intent(BBVideoHelperActivity.this, Backbonebits.class);
                startActivity(i);
                finish();
                overridePendingTransition(0, 0);

            }
        }
        } catch (Exception ex) {

        }
    }
    @Override
    public void onPause() {
        super.onPause();

        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(webView, (Object[]) null);

        } catch(ClassNotFoundException cnfe) {

        } catch(NoSuchMethodException nsme) {

        } catch(InvocationTargetException ite) {

        } catch (IllegalAccessException iae) {

        }
    }

}
