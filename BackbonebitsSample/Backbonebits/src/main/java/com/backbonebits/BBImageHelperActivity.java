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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.backbonebits.adapters.BBViewPagerAdapter;
import com.backbonebits.customviews.BBCustomBoldTextView;
import com.backbonebits.requestresponse.BBImageHelpRequestResponse;
import com.backbonebits.viewpager.BBDirectionalBBViewPager;
import com.backbonebits.webservices.BBWebServiceCaller;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BBImageHelperActivity extends Activity implements ViewPager.OnPageChangeListener {
    private LinearLayout pager_indicator;
    private int dotsCount;
    private ImageView[] dots;
    private BBViewPagerAdapter mAdapter;
    ArrayList<String> imgArray;
    BBCustomBoldTextView imgBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // To make activity full screen.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bb_activity_image_help);
        pager_indicator = findViewById(R.id.viewPagerCountDots);
        imgBackBtn = findViewById(R.id.imgBackBtn);
        imgBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BBImageHelperActivity.this, Backbonebits.class);
                startActivity(i);
                finish();
                overridePendingTransition(0, 0);

            }
        });
        getHelperImages();

    }

    private void setUiPageViewController() {
        try {
            dotsCount = mAdapter.getCount();
            dots = new ImageView[dotsCount];

            for (int i = 0; i < dotsCount; i++) {
                dots[i] = new ImageView(this);
                dots[i].setImageDrawable(getResources().getDrawable(R.drawable.light_dot_bb));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(4, 0, 4, 0);

                pager_indicator.addView(dots[i], params);
            }

            dots[0].setImageDrawable(getResources().getDrawable(R.drawable.dark_dot_bb));
        } catch (Exception ex) {

        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        try{
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.light_dot_bb));
        }

        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.dark_dot_bb));

        if (position + 1 == dotsCount) {

        } else {

        }
        }catch(Exception ex)
        {

        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void getHelperImages() {
        try {
            if (!BBUtils.isNetworkAvailable(BBImageHelperActivity.this)) {
                BBUtils.showToast(BBImageHelperActivity.this, getResources().getString(R.string.no_internet_connection_bb));
            } else

            {
                BBUtils.showProgress(BBImageHelperActivity.this);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();

                Call<BBImageHelpRequestResponse> call = service.getImageHelper(BBUtils.Key, BBUtils.packageName, "image", BBUtils.version);
                call.enqueue(new Callback<BBImageHelpRequestResponse>() {

                    @Override
                    public void onResponse(Call<BBImageHelpRequestResponse> call, Response<BBImageHelpRequestResponse> response) {
                        BBUtils.hideProgress();
                        if (response.isSuccessful()) {
                            BBImageHelpRequestResponse result = response.body();
                            if (result.getStatus() == 1) {
                                String animationStyle = result.getData().getImage_animation();

                                if (result.getData().getImages().size() > 0) {
                                    imgArray = new ArrayList<String>();
                                    for (int i = 0; i < result.getData().getImages().size(); i++) {
                                        imgArray.add(result.getData().getImages().get(i).getImg());
                                    }

                                    mAdapter = new BBViewPagerAdapter(BBImageHelperActivity.this, imgArray);

                                    final BBDirectionalBBViewPager pager = findViewById(R.id.pager);

                                    pager.setAdapter(mAdapter);
                                    if (animationStyle.equalsIgnoreCase("Swipe top to bottom") || animationStyle.equalsIgnoreCase("Swipe bottom to top")) {
                                        pager.setOrientation(BBDirectionalBBViewPager.VERTICAL);
                                    } else {
                                        pager.setOrientation(BBDirectionalBBViewPager.HORIZONTAL);

                                    }
                                    setUiPageViewController();
                                    pager.setOnPageChangeListener(new com.backbonebits.viewpager.BBViewPager.OnPageChangeListener() {
                                        @Override
                                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                        }

                                        @Override
                                        public void onPageSelected(int position) {
                                            for (int i = 0; i < dotsCount; i++) {
                                                dots[i].setImageDrawable(getResources().getDrawable(R.drawable.light_dot_bb));
                                            }

                                            dots[position].setImageDrawable(getResources().getDrawable(R.drawable.dark_dot_bb));

                                            if (position + 1 == dotsCount) {

                                            } else {

                                            }
                                        }

                                        @Override
                                        public void onPageScrollStateChanged(int state) {

                                        }
                                    });

                                }


                            } else {
                                Toast.makeText(BBImageHelperActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                            }


                        } else {
                            BBUtils.hideProgress();
                        }
                    }

                    @Override
                    public void onFailure(Call<BBImageHelpRequestResponse> call, Throwable t) {
                        Log.v("onFailure", "onFailure");
                        BBUtils.showToast(BBImageHelperActivity.this, getResources().getString(R.string.server_error_bb));
                        BBUtils.hideProgress();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(BBImageHelperActivity.this, Backbonebits.class);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);

    }
}
