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
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.backbonebits.adapters.BBExpandableListAdapter;
import com.backbonebits.customviews.BBCustomBoldTextView;
import com.backbonebits.requestresponse.BBFaqHelpRequestResponse;
import com.backbonebits.requestresponse.BBFaqs;
import com.backbonebits.webservices.BBWebServiceCaller;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BBFaqHelperActivity extends Activity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private SearchView search;
    private BBExpandableListAdapter listAdapter;
    private ExpandableListView myList;
    private int lastExpandedPosition = -1;
    BBCustomBoldTextView imgBackBtn;
    private ArrayList<BBFaqs> alldataList = new ArrayList<BBFaqs>();
    SearchManager searchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bb_activity_faq_help);
        imgBackBtn = findViewById(R.id.imgBackBtn);
        imgBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BBFaqHelperActivity.this, Backbonebits.class);
                startActivity(i);
                finish();
                overridePendingTransition(0, 0);

            }
        });
        searchManager= (SearchManager) getSystemService(SEARCH_SERVICE);
        search = findViewById(R.id.search);
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        search.setIconifiedByDefault(false);
        search.setOnQueryTextListener(this);
        search.setOnCloseListener(this);
        int searchPlateId = search.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        View searchPlateView = search.findViewById(searchPlateId);
        if (searchPlateView != null) {
            searchPlateView.setBackgroundColor(Color.WHITE);
        }

        if (getIntent().getSerializableExtra("alldata") != null) {
            ArrayList<BBFaqs> allFaqDataList = (ArrayList<BBFaqs>) getIntent().getSerializableExtra("alldata");
            myList = findViewById(R.id.expandableList);
            listAdapter = new BBExpandableListAdapter(BBFaqHelperActivity.this, allFaqDataList);
            //attach the adapter to the list
            myList.setAdapter(listAdapter);
            int pos= 0;
            if(getIntent().getStringExtra("pos")!=null)
            {
                 String id = getIntent().getStringExtra("pos");

                for(int i= 0 ; i<allFaqDataList.size();i++)
                {
                    if(allFaqDataList.get(i).getId().equalsIgnoreCase(id))
                    {
                        pos = i;
                        break;
                    }
                }

                myList.expandGroup(pos);

            }
            myList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                @Override
                public void onGroupExpand(int groupPosition) {
                    if (lastExpandedPosition != -1
                            && groupPosition != lastExpandedPosition) {
                        myList.collapseGroup(lastExpandedPosition);
                    }
                    lastExpandedPosition = groupPosition;
                }
            });

        } else {
            getHelperFaqs();
        }
    }



    private void getHelperFaqs() {
        try {
            if (!BBUtils.isNetworkAvailable(BBFaqHelperActivity.this)) {
                BBUtils.showToast(BBFaqHelperActivity.this, getResources().getString(R.string.no_internet_connection_bb));
            } else

            {
                BBUtils.showProgress(BBFaqHelperActivity.this);
                BBWebServiceCaller.WebServiceApiInterface service = BBWebServiceCaller.getClient();

                Call<BBFaqHelpRequestResponse> call = service.getFaqHelper(BBUtils.Key, BBUtils.packageName, "faq", BBUtils.version);
                call.enqueue(new Callback<BBFaqHelpRequestResponse>() {

                    @Override
                    public void onResponse(Call<BBFaqHelpRequestResponse> call, Response<BBFaqHelpRequestResponse> response) {
                        BBUtils.hideProgress();
                        if (response.isSuccessful()) {
                            BBFaqHelpRequestResponse result = response.body();
                            if (result.getStatus() == 1) {

                                if (result.getData().getFaq().size() > 0) {


                                    for (int i = 0; i < result.getData().getFaq().size(); i++) {
                                        BBFaqs faq = new BBFaqs(result.getData().getFaq().get(i).getQuestion(), result.getData().getFaq().get(i).getAnswer(),result.getData().getFaq().get(i).getId());
                                        alldataList.add(faq);
                                    }

                                    myList = findViewById(R.id.expandableList);
                                    listAdapter = new BBExpandableListAdapter(BBFaqHelperActivity.this, alldataList);
                                    //attach the adapter to the list
                                    myList.setAdapter(listAdapter);
                                    myList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                                        @Override
                                        public void onGroupExpand(int groupPosition) {
                                            if (lastExpandedPosition != -1
                                                    && groupPosition != lastExpandedPosition) {
                                                myList.collapseGroup(lastExpandedPosition);
                                            }
                                            lastExpandedPosition = groupPosition;
                                        }
                                    });

                                }


                            } else {
                                Toast.makeText(BBFaqHelperActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            BBUtils.hideProgress();
                        }
                    }

                    @Override
                    public void onFailure(Call<BBFaqHelpRequestResponse> call, Throwable t) {
                        Log.v("onFailure", "onFailure");
                        BBUtils.showToast(BBFaqHelperActivity.this, getResources().getString(R.string.server_error_bb));
                        BBUtils.hideProgress();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onClose() {
        listAdapter.filterData("");
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        listAdapter.filterData(query);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        listAdapter.filterData(query);
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(BBFaqHelperActivity.this, Backbonebits.class);
        startActivity(i);
        finish();
        overridePendingTransition(0, 0);

    }
}
