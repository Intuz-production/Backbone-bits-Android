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
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backbonebits.adapters.BBPastMessageAdapter;
import com.backbonebits.customviews.BBCustomBoldTextView;
import com.backbonebits.requestresponse.BBGetRespondRequestResponse;
import com.backbonebits.webservices.BBWebServiceCaller;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BBPastRequestActivity extends Activity {
    private ListView lstMessageList;
    private BBPastMessageAdapter pastMessageListAdapter = null;
    private ArrayList<BBGetRespondRequestResponse.ResponderData> responderArray = new ArrayList<BBGetRespondRequestResponse.ResponderData>();
    private TextView txtNoData = null;
    private BBCustomBoldTextView imgBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bb_activity_past_request);
        lstMessageList = findViewById(R.id.listMesageList);
        txtNoData = findViewById(R.id.txtNoDataJobList);
        imgBackBtn = findViewById(R.id.imgBackBtn);
        imgBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BBPastRequestActivity.this, Backbonebits.class);
                startActivity(i);
                finish();
                overridePendingTransition(0, 0);

            }
        });
        getPastequests();
    }

    private void getPastequests() {
        try {
            if (!BBUtils.isNetworkAvailable(BBPastRequestActivity.this)) {
                BBUtils.showToast(BBPastRequestActivity.this, getResources().getString(R.string.no_internet_connection_bb));
            } else

            {
                BBUtils.showProgress(BBPastRequestActivity.this);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();

                Call<BBGetRespondRequestResponse> call = service.getRespondData(BBUtils.Key, BBUtils.packageName, "list", BBUtils.androidID);
                call.enqueue(new Callback<BBGetRespondRequestResponse>() {

                    @Override
                    public void onResponse(Call<BBGetRespondRequestResponse> call, Response<BBGetRespondRequestResponse> response) {
                        BBUtils.hideProgress();
                        if (response.isSuccessful()) {
                            BBGetRespondRequestResponse result = response.body();
                            if (result.getStatus() == 1) {

                                if (result.getData().size() > 0) {


                                    for (int i = 0; i < result.getData().size(); i++) {
                                        responderArray.add(result.getData().get(i));
                                    }

                                    Parcelable state = lstMessageList.onSaveInstanceState();
                                    if (responderArray.size() > 0) {
                                        int currentPosition = lstMessageList.getFirstVisiblePosition();
                                        pastMessageListAdapter = new BBPastMessageAdapter(BBPastRequestActivity.this, responderArray, BBPastRequestActivity.this);
                                        lstMessageList.setAdapter(pastMessageListAdapter);
                                        // Setting new scroll position
                                        lstMessageList.setSelectionFromTop(currentPosition, 0);
                                    } else {

                                        pastMessageListAdapter.notifyDataSetChanged();
                                        lstMessageList.setVisibility(View.GONE);
                                        txtNoData.setVisibility(View.VISIBLE);
                                    }

                                    lstMessageList.onRestoreInstanceState(state);
                                }


                            } else {
                                lstMessageList.setVisibility(View.GONE);
                                txtNoData.setVisibility(View.VISIBLE);
                                Toast.makeText(BBPastRequestActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            BBUtils.hideProgress();
                        }
                    }

                    @Override
                    public void onFailure(Call<BBGetRespondRequestResponse> call, Throwable t) {
                        Log.v("onFailure", "onFailure");
                        BBUtils.showToast(BBPastRequestActivity.this, getResources().getString(R.string.server_error_bb));
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
        Intent i = new Intent(BBPastRequestActivity.this, Backbonebits.class);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);

    }
}
