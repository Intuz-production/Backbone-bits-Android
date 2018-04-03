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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.backbonebits.adapters.BBExpandableListAdapter;
import com.backbonebits.customviews.BBCustomBoldTextView;
import com.backbonebits.customviews.BBCustomEdiText;
import com.backbonebits.permissionManagerViews.Permission;
import com.backbonebits.permissionManagerViews.PermissionManagerInstance;
import com.backbonebits.permissionManagerViews.PermissionManagerListener;
import com.backbonebits.requestresponse.BBFaqHelpRequestResponse;
import com.backbonebits.requestresponse.BBFaqs;
import com.backbonebits.requestresponse.BBSaveRespondRequestResponse;
import com.backbonebits.utils.BBCommon;
import com.backbonebits.utils.LocationProvider;
import com.backbonebits.webservices.BBWebServiceCaller;
import com.nostra13.universalimageloader.core.ImageLoader;

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

public class BBNewRequestDialogActivity extends Activity implements LocationProvider.LocationCallback {

    private final static int REQUEST_LOCATION_PERMISSION = 111;
    private ImageView imgGallery;
    private ImageView imgCamera;
    private ImageView imgVideo;
    private ImageView imgQuery;
    private ImageView imgBug;
    private ImageView imgFeedback;
    private RelativeLayout lnrTop;
    private BBCustomBoldTextView imgBackBtn;
    private ImageView imgSendBtn;
    private BBCustomBoldTextView txtTitle;
    private LinearLayout llTakeImage;
    private LinearLayout llGallery;
    private LinearLayout llScreenshot;
    private LinearLayout llTakeVideo;
    private FrameLayout frameLayout;
    private ImageView imgIssueSupportAttachment;
    private ImageView imgClose;
    private BBCustomEdiText txtName;
    private BBCustomEdiText txtEmail;
    private LinearLayout llQuery;
    private LinearLayout llBug;
    private LinearLayout feedback;
    private BBCustomEdiText txtMessage;
    String requestType = "";
    ArrayList multipartData;
    String address = "";
    private final int ACCESS_STORAGE_PERMISSION_REQUEST_CODE = 224;
    private static final int SELECT_PICTURE = 100;
    private Uri imageURI;
    private BBExpandableListAdapter listAdapter;
    private ExpandableListView myList;
    private ArrayList<BBFaqs> alldataList = new ArrayList<BBFaqs>();
    private ArrayList<BBFaqs> allFaqDataList = new ArrayList<BBFaqs>();
    private ArrayList<BBFaqs> originalList;
    TextView txtFeedback;
    TextView txtQuery;
    TextView txtBug;
    public LocationProvider locationProvider;
    private PermissionManagerInstance mPermissionManagerInstance;
    private String[] permissionArray1 = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ImageLoader imageloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bb_new_request_dialog);
        multipartData = new ArrayList();
        mPermissionManagerInstance = new PermissionManagerInstance(this);

        lnrTop = findViewById(R.id.lnrTop);
        imgBackBtn = findViewById(R.id.imgBackBtn);
        imgSendBtn = findViewById(R.id.imgSendBtn);
        txtTitle = findViewById(R.id.txtTitle);
        llTakeImage = findViewById(R.id.llTakeImage);
        llGallery = findViewById(R.id.llGallery);
        llScreenshot = findViewById(R.id.llScreenshot);
        llTakeVideo = findViewById(R.id.llTakeVideo);
        frameLayout = findViewById(R.id.frameLayout);
        imgIssueSupportAttachment = findViewById(R.id.imgIssueSupportAttachment);
        imgClose = findViewById(R.id.imgClose);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        llQuery = findViewById(R.id.llQuery);
        llBug = findViewById(R.id.llBug);
        feedback = findViewById(R.id.Feedback);
        txtMessage = findViewById(R.id.txtMessage);
        imgGallery = findViewById(R.id.imgGallery);
        imgCamera = findViewById(R.id.imgCamera);
        imgVideo = findViewById(R.id.imgVideo);
        imgQuery = findViewById(R.id.imgQuery);
        imgBug = findViewById(R.id.imgBug);
        imgFeedback = findViewById(R.id.imgFeedback);
        txtFeedback = findViewById(R.id.txtFeedback);
        txtQuery = findViewById(R.id.txtQuery);
        txtBug = findViewById(R.id.txtBug);
        requestType = "Feedback";
        if (checkLocationPermission()) {
            locationProvider = new LocationProvider(this, this);
            locationProvider.connect();
        } else {
            requestLocationPermission();
        }
        try {
            if (BBUtils.isExistKeyinPref(this, BBUtils.NAME) && !BBUtils.getSharedPreString(this, BBUtils.NAME).equalsIgnoreCase("")) {
                txtName.setText(BBUtils.getSharedPreString(this, BBUtils.NAME));
            } else {
                txtName.setText("user" + BBUtils.getSharedPreString(this, BBUtils.ANDROID_ID).substring(0, 6));

            }
            if (BBUtils.isExistKeyinPref(this, BBUtils.EMAIL) && !BBUtils.getSharedPreString(this, BBUtils.EMAIL).equalsIgnoreCase("")) {
                txtEmail.setText(BBUtils.getSharedPreString(this, BBUtils.EMAIL));
            }
            imgGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            mPermissionManagerInstance = new PermissionManagerInstance(BBNewRequestDialogActivity.this);
                            mPermissionManagerInstance.requestForPermissions(permissionArray1, new PermissionManagerListener() {
                                @Override
                                public void permissionCallback(String[] permissions, Permission[] grantResults, boolean allGranted) {
                                    if (allGranted) {
                                        Intent intent = new Intent();
                                        // Show only images, no videos or anything else
                                        intent.setType("image/*");
                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                        // Always show the chooser (if there are multiple options available)
                                        multipartData.clear();
                                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
                                    } else {

                                    }


                                }
                            });

                        } else {
                            Intent intent = new Intent();
                            // Show only images, no videos or anything else
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            // Always show the chooser (if there are multiple options available)
                            multipartData.clear();
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });

            imgCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(BBNewRequestDialogActivity.this, Backbonebits.class);
                    i.putExtra("sendScreenshot", "yes");
                    startActivity(i);
                    finish();
                    overridePendingTransition(0, 0);

                }
            });

            imgVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(BBNewRequestDialogActivity.this, Backbonebits.class);
                    i.putExtra("sendVideo", "yes");
                    startActivity(i);
                    finish();
                    overridePendingTransition(0, 0);

                }
            });


            imgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    llTakeImage.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.GONE);
                }
            });
            llQuery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestType = "Query";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imgQuery.setImageDrawable(getResources().getDrawable(R.drawable.query_slt_bb, getApplicationContext().getTheme()));
                        imgBug.setImageDrawable(getResources().getDrawable(R.drawable.bug_bb, getApplicationContext().getTheme()));
                        imgFeedback.setImageDrawable(getResources().getDrawable(R.drawable.feedback_bb, getApplicationContext().getTheme()));

                    } else {
                        imgQuery.setImageDrawable(getResources().getDrawable(R.drawable.query_slt_bb));
                        imgBug.setImageDrawable(getResources().getDrawable(R.drawable.bug_bb));
                        imgFeedback.setImageDrawable(getResources().getDrawable(R.drawable.feedback_bb));

                    }
                    txtQuery.setTextColor(getResources().getColor(R.color.bb_lightblackcolor));
                    txtBug.setTextColor(getResources().getColor(R.color.bb_textcolordark));
                    txtFeedback.setTextColor(getResources().getColor(R.color.bb_textcolordark));
                }
            });
            llBug.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestType = "Bug";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imgQuery.setImageDrawable(getResources().getDrawable(R.drawable.query_bb, getApplicationContext().getTheme()));
                        imgBug.setImageDrawable(getResources().getDrawable(R.drawable.bug_slt_bb, getApplicationContext().getTheme()));
                        imgFeedback.setImageDrawable(getResources().getDrawable(R.drawable.feedback_bb, getApplicationContext().getTheme()));


                    } else {
                        imgQuery.setImageDrawable(getResources().getDrawable(R.drawable.query_bb));
                        imgBug.setImageDrawable(getResources().getDrawable(R.drawable.bug_slt_bb));
                        imgFeedback.setImageDrawable(getResources().getDrawable(R.drawable.feedback_bb));

                    }
                    txtQuery.setTextColor(getResources().getColor(R.color.bb_textcolordark));
                    txtBug.setTextColor(getResources().getColor(R.color.bb_lightblackcolor));
                    txtFeedback.setTextColor(getResources().getColor(R.color.bb_textcolordark));
                }
            });
            feedback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestType = "Feedback";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imgQuery.setImageDrawable(getResources().getDrawable(R.drawable.query_bb, getApplicationContext().getTheme()));
                        imgBug.setImageDrawable(getResources().getDrawable(R.drawable.bug_bb, getApplicationContext().getTheme()));
                        imgFeedback.setImageDrawable(getResources().getDrawable(R.drawable.feedback_slt_bb, getApplicationContext().getTheme()));

                    } else {
                        imgQuery.setImageDrawable(getResources().getDrawable(R.drawable.query_bb));
                        imgBug.setImageDrawable(getResources().getDrawable(R.drawable.bug_bb));
                        imgFeedback.setImageDrawable(getResources().getDrawable(R.drawable.feedback_slt_bb));

                    }
                    txtQuery.setTextColor(getResources().getColor(R.color.bb_textcolordark));
                    txtBug.setTextColor(getResources().getColor(R.color.bb_textcolordark));
                    txtFeedback.setTextColor(getResources().getColor(R.color.bb_lightblackcolor));
                }
            });

            if (BBCommon.requestFrom == 1) {
                llTakeImage.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                imageloader = BackbonebitsUILApplication.initImageLoader(BBNewRequestDialogActivity.this);
                imageloader.displayImage("file://" + BBCommon.CapturedScreen, imgIssueSupportAttachment);
                multipartData.add(BBCommon.CapturedScreen);
            } else if (BBCommon.requestFrom == 2) {
                llTakeImage.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                multipartData.add(BBCommon.strVideoPath);

                imageloader = BackbonebitsUILApplication.initImageLoader(BBNewRequestDialogActivity.this);
                imageloader.displayImage("file://" + BBCommon.strVideoPath, imgIssueSupportAttachment);
            } else {
                llTakeImage.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE);
            }

            imgIssueSupportAttachment.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (BBCommon.requestFrom == 1) {
                        openVideoDialog(1);
                    } else if (BBCommon.requestFrom == 2) {
                        openVideoDialog(2);
                    } else {
                        openVideoDialog(0);
                    }

                }
            });

            imgBackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        if (BBCommon.requestFrom == 0) {
                            Intent i = new Intent(BBNewRequestDialogActivity.this, Backbonebits.class);
                            i.putExtra("openHelp", "yes");
                            startActivity(i);
                            finish();
                            overridePendingTransition(0, 0);

                        } else {
                            alertDialog("Are you sure want to go back, All your data will be lost?", "video");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });
            imgSendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkAllValidation() == true) {
                        saveRespondData();
                    }


                }
            });

            txtMessage.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterData(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            getHelperFaqs();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(BBNewRequestDialogActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(BBNewRequestDialogActivity.this,
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

    private Bitmap getThumnailOfVideo(String path) {
        Bitmap thumb = null;
        try {
            thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return thumb;
    }

    private void saveRespondData() {
        try {
            if (!BBUtils.isNetworkAvailable(BBNewRequestDialogActivity.this)) {
                BBUtils.showToast(BBNewRequestDialogActivity.this, getResources().getString(R.string.no_internet_connection_bb));
            } else

            {
                Map<String, RequestBody> map = new HashMap<>();

                for (int i = 0; i < multipartData.size(); i++) {
                    RequestBody fileBody = null;
                    File file = null;
                    if (BBCommon.requestFrom == 2) {
                        file = new File(multipartData.get(i).toString());
                        fileBody = RequestBody.create(MediaType.parse("video/*"), file);
                    } else {
                        file = new File(multipartData.get(i).toString());
                        fileBody = RequestBody.create(MediaType.parse("image/*"), file);
                    }
                    map.put("attachments[]\"; filename=\"" + file.getName() + "\" ", fileBody);

                }
                Boolean deviceType = "google_sdk".equals(android.os.Build.PRODUCT);

                if (deviceType) {
                    Log.v("emulator", "Do this that intended for Emulator");

                } else {
                    Log.v("Real Device", "Do this that intended for Real Device");

                }

                BBUtils.setSharedPreString(BBNewRequestDialogActivity.this, BBUtils.NAME, txtName.getText().toString().trim());
                BBUtils.setSharedPreString(BBNewRequestDialogActivity.this, BBUtils.EMAIL, txtEmail.getText().toString().trim());
                RequestBody secret_key = BBUtils.textToBody(BBUtils.Key);
                RequestBody app_id = BBUtils.textToBody(BBUtils.packageName);
                RequestBody request_id = BBUtils.textToBody("0");
                RequestBody request_type = BBUtils.textToBody(requestType);
                RequestBody name = BBUtils.textToBody(txtName.getText().toString().trim());
                RequestBody email = BBUtils.textToBody(txtEmail.getText().toString().trim());
                RequestBody message = BBUtils.textToBody(txtMessage.getText().toString().trim());
                RequestBody region = BBUtils.textToBody(address);
                RequestBody version = BBUtils.textToBody(Build.VERSION.RELEASE);
                RequestBody app_version = BBUtils.textToBody(BBUtils.version);
                RequestBody device = BBUtils.textToBody(Build.MODEL);
                RequestBody subject = BBUtils.textToBody("");
                RequestBody phone = BBUtils.textToBody("");
                RequestBody os_type = BBUtils.textToBody("android");
                RequestBody device_id = BBUtils.textToBody(BBUtils.androidID);
                RequestBody device_token = BBUtils.textToBody(BBUtils.getSharedPreString(BBNewRequestDialogActivity.this, BBUtils.GCM_ID));
                RequestBody is_live;
                if (BBUtils.IS_LIVE == false) {
                    is_live = BBUtils.textToBody("0");
                } else {
                    is_live = BBUtils.textToBody("1");
                }


                BBUtils.showProgress(BBNewRequestDialogActivity.this);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();

                Call<BBSaveRespondRequestResponse> call = service.saveRespondData(
                        secret_key, app_id, request_id, request_type, name, email, message, region, version, app_version, device, subject, phone, os_type, map, device_id, device_token, is_live);
                call.enqueue(new Callback<BBSaveRespondRequestResponse>() {

                    @Override
                    public void onResponse(Call<BBSaveRespondRequestResponse> call, Response<BBSaveRespondRequestResponse> response) {
                        BBUtils.hideProgress();
                        if (response.isSuccessful()) {
                            BBSaveRespondRequestResponse result = response.body();
                            if (result.getStatus() == 1) {
                                BBUtils.setSharedPreString(BBNewRequestDialogActivity.this, BBUtils.ID, String.valueOf(result.getId()));
                                Toast.makeText(BBNewRequestDialogActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(BBNewRequestDialogActivity.this, BBPastRequestActivity.class);
                                startActivity(i);
                                finish();
                                overridePendingTransition(0, 0);


                            } else {
                                Toast.makeText(BBNewRequestDialogActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                            }
//
                        } else {
                            BBUtils.hideProgress();
                        }
                    }

                    @Override
                    public void onFailure(Call<BBSaveRespondRequestResponse> call, Throwable t) {
                        Log.v("onFailure", "onFailure");
                        t.printStackTrace();

                        BBUtils.showToast(BBNewRequestDialogActivity.this, getResources().getString(R.string.server_error_bb));
                        BBUtils.hideProgress();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkAllValidation() {
        boolean isValidate = false;
        try {
            if (txtName.getText().toString().trim().length() == 0) {
                BBUtils.showToast(BBNewRequestDialogActivity.this, getString(R.string.txtNameString_bb));
                isValidate = false;
            } else if (BBUtils.isValidEmail(txtEmail.getText().toString().trim()) == false) {
                BBUtils.showToast(BBNewRequestDialogActivity.this, getString(R.string.txtEmailString_bb));
                isValidate = false;
            } else if (requestType.length() == 0) {
                BBUtils.showToast(BBNewRequestDialogActivity.this, getString(R.string.txtRequestString_bb));
                isValidate = false;
            } else if (txtMessage.getText().toString().trim().length() == 0) {
                BBUtils.showToast(BBNewRequestDialogActivity.this, getString(R.string.txtMessageString_bb));
                isValidate = false;
            } else {
                isValidate = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isValidate;
    }

    @Override
    public void handleNewLocation(Location location) {
        if (location != null) {
            Log.e("location>>", location + "");
            BBUtils.lat = location.getLatitude();
            BBUtils.lon = location.getLongitude();
            new ReverseGeocodingTask(BBNewRequestDialogActivity.this).execute();
        }
    }

    private class ReverseGeocodingTask extends AsyncTask<Void, Void, String> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            String addressText = "";
            try {
                Geocoder geocoder = new Geocoder(mContext);
                List<Address> addresses = null;
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            Log.e("addresstext", addressText);
            if (BBUtils.lat != 0 && BBUtils.lon != 0)
                address = addressText;
            else
                address = "";
        }
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri uri = data.getData();
                imageURI = uri;
                try {
                    llTakeImage.setVisibility(View.GONE);
                    frameLayout.setVisibility(View.VISIBLE);

                    imageloader = BackbonebitsUILApplication.initImageLoader(BBNewRequestDialogActivity.this);

                    String path = null;

                    if (Build.VERSION.SDK_INT < 11) {
                        path = getRealPathFromURI_BelowAPI11(BBNewRequestDialogActivity.this, imageURI);
                    } else if (Build.VERSION.SDK_INT < 19) {
                        path = getRealPathFromURI_API11to18(BBNewRequestDialogActivity.this, imageURI);
                    } else {
                        path = getRealPathFromURI_API19(BBNewRequestDialogActivity.this, imageURI);
                    }

                    if (getMimeType(path).toLowerCase().startsWith("video")) {
                        imgIssueSupportAttachment.setImageBitmap(getThumnailOfVideo(path));
                        BBCommon.strVideoPath = path;
                        BBCommon.requestFrom = 2;

                    } else {
                        imageloader.displayImage(String.valueOf(uri), imgIssueSupportAttachment);
                        BBCommon.CapturedScreen = path;
                        BBCommon.requestFrom = 1;

                    }

                    multipartData.add(path);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }


    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    public void onBackPressed() {

        try {
            if (BBCommon.requestFrom == 0) {
                Intent i = new Intent(BBNewRequestDialogActivity.this, Backbonebits.class);
                i.putExtra("openHelp", "yes");
                startActivity(i);
                finish();
                overridePendingTransition(0, 0);

            } else {
                alertDialog("Are you sure want to go back, All your data will be lost?", "video");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public void openVideoDialog(int which) {
        try {
            final Dialog videodialog = new Dialog(BBNewRequestDialogActivity.this, R.style.BBAppTheme);
            videodialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            videodialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            videodialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            videodialog.setContentView(R.layout.bb_attachmentdialog);
            ImageView imgCloseBtn = videodialog.findViewById(R.id.close_btn_image);
            ImageView image = videodialog.findViewById(R.id.imageView);
            VideoView videoView = videodialog.findViewById(R.id.videoView);

            imgCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (videodialog != null && videodialog.isShowing()) {
                        videodialog.dismiss();
                    }

                }
            });

            if (which == 1) {
                image.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                image.setImageBitmap(BBScreenShotEditActivity.loadFromFile(BBCommon.CapturedScreen));
            }
            if (which == 0) {
                image.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                    image.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (which == 2) {
                image.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                videoView.setVideoPath(BBCommon.strVideoPath);
                videoView.requestFocus();
                videoView.start();
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        videodialog.dismiss();
                    }
                });
            }
            if (videodialog != null && !videodialog.isShowing())
                videodialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getHelperFaqs() {
        try {

            BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();
            Call<BBFaqHelpRequestResponse> call = service.getFaqHelper(BBUtils.Key, BBUtils.packageName, "faq", BBUtils.version);
            call.enqueue(new Callback<BBFaqHelpRequestResponse>() {
                @Override
                public void onResponse(Call<BBFaqHelpRequestResponse> call, Response<BBFaqHelpRequestResponse> response) {
                    if (response.isSuccessful()) {
                        BBFaqHelpRequestResponse result = response.body();
                        if (result.getStatus() == 1) {
                            if (result.getData().getFaq().size() > 0) {
                                for (int i = 0; i < result.getData().getFaq().size(); i++) {
                                    BBFaqs faq = new BBFaqs(result.getData().getFaq().get(i).getQuestion(), result.getData().getFaq().get(i).getAnswer(), result.getData().getFaq().get(i).getId());
                                    alldataList.add(faq);
                                }
                                originalList = new ArrayList<BBFaqs>();
                                originalList.addAll(alldataList);
                                allFaqDataList.addAll(alldataList);
                                myList = findViewById(R.id.expandableList);

                                listAdapter = new BBExpandableListAdapter(BBNewRequestDialogActivity.this, alldataList);
                                myList.setAdapter(listAdapter);
                                setListViewHeightBasedOnItems(myList);
                                myList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                                    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                                        Intent i = new Intent(BBNewRequestDialogActivity.this, BBFaqHelperActivity.class);
                                        i.putExtra("alldata", allFaqDataList);
                                        i.putExtra("pos", alldataList.get(groupPosition).getId());
                                        startActivity(i);
                                        overridePendingTransition(0, 0);

                                        return true;
                                    }
                                });


                            }


                        } else {
                        }

                    } else {
                    }
                }

                @Override
                public void onFailure(Call<BBFaqHelpRequestResponse> call, Throwable t) {

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void filterData(String query) {
        try {
            query = query.toLowerCase();
            originalList.clear();
            myList.setVisibility(View.VISIBLE);
            if (query.isEmpty()) {
                alldataList.clear();
                setListViewHeightBasedOnItems(myList);
                listAdapter.notifyDataSetChanged();
            } else {
                ArrayList<BBFaqs> newList = new ArrayList<BBFaqs>();
                for (BBFaqs country : allFaqDataList) {
                    if (country.getQuestion().toLowerCase().contains(query) ||
                            country.getAnswer().toLowerCase().contains(query)) {
                        newList.add(country);
                    }
                }
                if (newList.size() > 0) {
                    originalList = newList;
                }
                alldataList.clear();
                alldataList.addAll(originalList);
                if (listAdapter == null) {
                    listAdapter = new BBExpandableListAdapter(BBNewRequestDialogActivity.this, alldataList);
                    myList.setAdapter(listAdapter);
                    setListViewHeightBasedOnItems(myList);

                } else {
                    setListViewHeightBasedOnItems(myList);
                    listAdapter.notifyDataSetChanged();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean setListViewHeightBasedOnItems(ExpandableListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

    public void alertDialog(String msg, final String type) {
        try {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BBNewRequestDialogActivity.this);
            alertDialogBuilder.setMessage(msg);

            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    if (type.equalsIgnoreCase("video")) {


                    } else if (type.equalsIgnoreCase("screenshot")) {


                    }
                    BBNewRequestDialogActivity.this.finish();
                    new Backbonebits(Backbonebits.context).getStatusMenu(Backbonebits.context);


                }
            });

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            if (alertDialog != null && !alertDialog.isShowing())
                alertDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
