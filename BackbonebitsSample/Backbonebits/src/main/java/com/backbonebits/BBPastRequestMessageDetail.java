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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backbonebits.adapters.BBAddPictureJobAdapter;
import com.backbonebits.adapters.BBPastDetailMessageAdapter;
import com.backbonebits.customviews.BBCustomBoldTextView;
import com.backbonebits.customviews.BBCustomTextView;
import com.backbonebits.requestresponse.BBGetRespondDetailRequestResponse;
import com.backbonebits.requestresponse.BBSaveRespondRequestResponse;
import com.backbonebits.utils.LocationProvider;
import com.backbonebits.webservices.BBWebServiceCaller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BBPastRequestMessageDetail extends Activity implements LocationProvider.LocationCallback {
    private final static int REQUEST_LOCATION_PERMISSION = 111;
    String request_id = "";
    private RelativeLayout lnrTop;
    private BBCustomBoldTextView txtTitle;
    private LinearLayout llheader;
    private ImageView imgMessageType;
    private BBCustomBoldTextView txtRequestType;
    private BBCustomTextView txtId;
    private ListView detailMesageList;
    private BBCustomTextView txtNoDataJobList;
    private TextView txtNoData = null;
    private BBCustomBoldTextView imgBackBtn;
    private BBPastDetailMessageAdapter pastDetailMessageListAdapter = null;
    private ArrayList<BBGetRespondDetailRequestResponse.ResponderDetailData.ResponderDetailRequestData.TypeData> responderdetailArray = new ArrayList<>();

    private ArrayList<String> dateArray;
    private RecyclerView recyclerViewAttchmentImages;
    private BBAddPictureJobAdapter addPictureAttchmentAdapter;
    private ImageView imgAttchment;
    private TextView txtSend;
    String address = "";
    private TextView txtMessage;
    private String username = "";
    private String useremail = "";
    private String requestType = "";
    public LocationProvider locationProvider;
    @Override
    protected void onResume() {
        super.onResume();
        if (BBAddPictureJobAdapter.attachmentList.size() == 0) {

            imgAttchment.setImageDrawable(getResources().getDrawable(R.drawable.bb_attachment_icon_bb));
        } else {
            imgAttchment.setImageDrawable(getResources().getDrawable(R.drawable.bb_attachment_slticon_bb));

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bb_activity_past_request_message_detail);
        request_id = getIntent().getStringExtra("request_id");

        lnrTop = findViewById(R.id.lnrTop);
        imgBackBtn = findViewById(R.id.imgBackBtn);
        txtTitle = findViewById(R.id.txtTitle);
        llheader = findViewById(R.id.llheader);
        imgMessageType = findViewById(R.id.imgMessageType);
        txtRequestType = findViewById(R.id.txtRequestType);
        txtId = findViewById(R.id.txtId);
        detailMesageList = findViewById(R.id.detailMesageList);
        txtNoDataJobList = findViewById(R.id.txtNoDataJobList);
        recyclerViewAttchmentImages = findViewById(R.id.recyclerAttachImages);
        imgAttchment = findViewById(R.id.imgAttchment);
        txtSend = findViewById(R.id.txtSend);
        if (checkLocationPermission()) {
            locationProvider = new LocationProvider(this, this);
            locationProvider.connect();
        } else {
            requestLocationPermission();
        }
        try {
            txtMessage = findViewById(R.id.txtMessage);
            if (BBUtils.isExistKeyinPref(BBPastRequestMessageDetail.this, BBUtils.NAME)) {
                username = BBUtils.getSharedPreString(BBPastRequestMessageDetail.this, BBUtils.NAME);
            }
            if (BBUtils.isExistKeyinPref(BBPastRequestMessageDetail.this, BBUtils.EMAIL)) {
                useremail = BBUtils.getSharedPreString(BBPastRequestMessageDetail.this, BBUtils.EMAIL);
            }
            imgBackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(BBPastRequestMessageDetail.this, BBPastRequestActivity.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(0, 0);

                }
            });

            getPastequestDetail();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerViewAttchmentImages.setLayoutManager(linearLayoutManager);
            ArrayList<String> requestList = new ArrayList<>();

            addPictureAttchmentAdapter = new BBAddPictureJobAdapter(this, requestList, recyclerViewAttchmentImages);
            recyclerViewAttchmentImages.setAdapter(addPictureAttchmentAdapter);
            imgAttchment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (BBAddPictureJobAdapter.attachmentList.size() == 0) {
                        addPictureAttchmentAdapter.pickImage();
                        imgAttchment.setImageDrawable(getResources().getDrawable(R.drawable.bb_attachment_icon_bb));
                    } else {
                        imgAttchment.setImageDrawable(getResources().getDrawable(R.drawable.bb_attachment_slticon_bb));
                        if (recyclerViewAttchmentImages.getVisibility() == View.VISIBLE) {
                            recyclerViewAttchmentImages.setVisibility(View.GONE);
                        } else {
                            recyclerViewAttchmentImages.setVisibility(View.VISIBLE);

                        }
                    }
                }
            });
            txtSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveRespondData();
                }
            });
        } catch (Exception ex) {

        }
    }
    public boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(BBPastRequestMessageDetail.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(BBPastRequestMessageDetail.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationProvider = new LocationProvider(this, this);
                locationProvider.connect();
            } else {
                address = "";
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (locationProvider != null)
            locationProvider.disconnect();
        super.onDestroy();
    }
    private void getPastequestDetail() {
        try {
            if (!BBUtils.isNetworkAvailable(BBPastRequestMessageDetail.this)) {
                BBUtils.showToast(BBPastRequestMessageDetail.this, getResources().getString(R.string.no_internet_connection_bb));
            } else

            {
                BBUtils.showProgress(BBPastRequestMessageDetail.this);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();

                Call<BBGetRespondDetailRequestResponse> call = service.getRespondDetail(BBUtils.Key, BBUtils.packageName, String.valueOf(request_id), "listdetail");
                call.enqueue(new Callback<BBGetRespondDetailRequestResponse>() {

                    @Override
                    public void onResponse(Call<BBGetRespondDetailRequestResponse> call, Response<BBGetRespondDetailRequestResponse> response) {
                        BBUtils.hideProgress();
                        if (response.isSuccessful()) {
                            BBGetRespondDetailRequestResponse result = response.body();
                            if (result.getStatus() == 1) {
                                requestType = result.getData().getRequest_type();
                                if (result.getData().getRequest_type() != null && result.getData().getRequest_type().equalsIgnoreCase("bug")) {
                                    imgMessageType.setImageResource(R.drawable.b_bug_bb);
                                }
                                if (result.getData().getRequest_type() != null && result.getData().getRequest_type().equalsIgnoreCase("query")) {
                                    imgMessageType.setImageResource(R.drawable.b_query_bb);

                                }
                                if (result.getData().getRequest_type() != null && result.getData().getRequest_type().equalsIgnoreCase("feedback")) {
                                    imgMessageType.setImageResource(R.drawable.b_feedback_bb);

                                }
                                String rType = result.getData().getRequest_type();
                                String requestTy = rType.substring(0, 1).toUpperCase() + rType.substring(1);
                                txtRequestType.setText(requestTy);

                                txtId.setText(request_id);
                                if (result.getData().getRequest_data().size() > 0) {
                                    dateArray = new ArrayList<>();

                                    for (String key : result.getData().getRequest_data().keySet()) {
                                        System.out.println(key);

                                        for (int j = 0; j < result.getData().getRequest_data().get(key).size(); j++) {
                                            if(j==0)
                                            {
                                                dateArray.add(key);
                                            }
                                            else

                                            {
                                                dateArray.add("");
                                            }
                                            responderdetailArray.add(result.getData().getRequest_data().get(key).get(j));
                                        }


                                    }


                                    Parcelable state = detailMesageList.onSaveInstanceState();
                                    if (responderdetailArray.size() > 0) {
                                        int currentPosition = detailMesageList.getFirstVisiblePosition();
                                        pastDetailMessageListAdapter = new BBPastDetailMessageAdapter(BBPastRequestMessageDetail.this, responderdetailArray, dateArray);
                                        detailMesageList.setAdapter(pastDetailMessageListAdapter);
                                        // Setting new scroll position
                                        detailMesageList.setSelectionFromTop(currentPosition, 0);
                                    } else {

                                        pastDetailMessageListAdapter.notifyDataSetChanged();
                                        detailMesageList.setVisibility(View.GONE);
                                        txtNoData.setVisibility(View.VISIBLE);
                                    }

                                    detailMesageList.onRestoreInstanceState(state);
                                }


                            } else {
                                detailMesageList.setVisibility(View.GONE);
                                txtNoData.setVisibility(View.VISIBLE);
                                Toast.makeText(BBPastRequestMessageDetail.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                            if (result.getStatus() == 1) {


                            } else {
                            }

                        } else {
                            BBUtils.hideProgress();
                        }
                    }

                    @Override
                    public void onFailure(Call<BBGetRespondDetailRequestResponse> call, Throwable t) {
                        Log.v("onFailure", "onFailure");
                        BBUtils.showToast(BBPastRequestMessageDetail.this, getResources().getString(R.string.server_error_bb));
                        BBUtils.hideProgress();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            addPictureAttchmentAdapter.onActivityResult(requestCode, resultCode, data);
        } catch (Exception ex) {

        }
    }

    private void saveRespondData() {
        try {
            if (!BBUtils.isNetworkAvailable(BBPastRequestMessageDetail.this)) {
                BBUtils.showToast(BBPastRequestMessageDetail.this, getResources().getString(R.string.no_internet_connection_bb));
            } else if (txtMessage.getText().toString().trim().length() == 0) {
                BBUtils.showToast(BBPastRequestMessageDetail.this, getResources().getString(R.string.txtMessageString_bb));

            } else

            {
                //final ProgressDialog dialog = ProgressDialog.show(this, "", "loading...");
                Map<String, RequestBody> map = new HashMap<>();

                for (int i = 0; i < BBAddPictureJobAdapter.attachmentList.size(); i++) {

                    File file = new File(BBAddPictureJobAdapter.attachmentList.get(i).toString());
                    RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
                    map.put("attachments[]\"; filename=\"" + file.getName() , fileBody);

                }
                Boolean deviceType = "google_sdk".equals(android.os.Build.PRODUCT);

                if (deviceType) {
                    Log.v("emulator", "Do this that intended for Emulator");

                } else {
                    Log.v("Real Device", "Do this that intended for Real Device");

                }

                RequestBody secret_key = BBUtils.textToBody(BBUtils.Key);
                RequestBody app_id = BBUtils.textToBody(BBUtils.packageName);
                final RequestBody request_ids = BBUtils.textToBody(String.valueOf(request_id));
                RequestBody request_type = BBUtils.textToBody(requestType);
                RequestBody name = BBUtils.textToBody(username);
                RequestBody email = BBUtils.textToBody(useremail);
                RequestBody message = BBUtils.textToBody(txtMessage.getText().toString().trim());
                RequestBody region = BBUtils.textToBody(address);
                RequestBody version = BBUtils.textToBody(Build.VERSION.RELEASE);
                RequestBody app_version = BBUtils.textToBody(BBUtils.version);
                RequestBody device = BBUtils.textToBody(Build.MODEL);
                RequestBody subject = BBUtils.textToBody("");
                RequestBody phone = BBUtils.textToBody("");
                RequestBody os_type = BBUtils.textToBody("android");
                RequestBody device_id = BBUtils.textToBody(BBUtils.androidID);
                RequestBody device_token = BBUtils.textToBody("");


                BBUtils.showProgress(BBPastRequestMessageDetail.this);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();
                RequestBody is_live;
                if(BBUtils.IS_LIVE==false)
                {
                    is_live = BBUtils.textToBody("0");
                }
                else
                {
                    is_live = BBUtils.textToBody("1");
                }
                Call<BBSaveRespondRequestResponse> call = service.saveRespondData(
                        secret_key, app_id, request_ids, request_type, name, email, message, region, version, app_version, device, subject, phone, os_type, map, device_id, device_token,is_live);
                call.enqueue(new Callback<BBSaveRespondRequestResponse>() {

                    @Override
                    public void onResponse(Call<BBSaveRespondRequestResponse> call, Response<BBSaveRespondRequestResponse> response) {
                        BBUtils.hideProgress();
                        if (response.isSuccessful()) {
                            BBSaveRespondRequestResponse result = response.body();
                            if (result.getStatus() == 1) {
                                BBAddPictureJobAdapter.attachmentList.clear();

                                BBUtils.setSharedPreString(BBPastRequestMessageDetail.this, BBUtils.ID, String.valueOf(result.getId()));
                                Toast.makeText(BBPastRequestMessageDetail.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(BBPastRequestMessageDetail.this, BBPastRequestMessageDetail.class);
                                i.putExtra("request_id", String.valueOf(request_id));
                                startActivity(i);
                                finish();
                                overridePendingTransition(0, 0);


                            } else {
                                Toast.makeText(BBPastRequestMessageDetail.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                            }
//
                        } else {
                            BBUtils.hideProgress();
                        }
                    }

                    @Override
                    public void onFailure(Call<BBSaveRespondRequestResponse> call, Throwable t) {
                        Log.v("onFailure", "onFailure");
                        BBUtils.showToast(BBPastRequestMessageDetail.this, getResources().getString(R.string.server_error_bb));
                        BBUtils.hideProgress();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleNewLocation(Location location) {
        if (location != null) {
            BBUtils.lat = location.getLatitude();
            BBUtils.lon = location.getLongitude();
            new ReverseGeocodingTask(BBPastRequestMessageDetail.this).execute();
        }
    }

    private class ReverseGeocodingTask extends AsyncTask<Void, Void, String> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            Geocoder geocoder = new Geocoder(mContext);


            List<Address> addresses = null;
            String addressText = "";

            try {
                if (BBUtils.lat != 0 && BBUtils.lon != 0) {
                    addresses = geocoder.getFromLocation(BBUtils.lat, BBUtils.lon, 1);
                } else {
                    addresses = geocoder.getFromLocation(0, 0, 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
            }

            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            if (BBUtils.lat != 0 && BBUtils.lon != 0)
                address = addressText;
            else
                address = "";
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(BBPastRequestMessageDetail.this, BBPastRequestActivity.class);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);

    }


}
