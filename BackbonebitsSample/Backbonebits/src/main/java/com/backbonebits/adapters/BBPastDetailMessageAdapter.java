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

package com.backbonebits.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.backbonebits.BBUtils;
import com.backbonebits.R;
import com.backbonebits.customviews.BBCustomTextView;
import com.backbonebits.requestresponse.BBGetRespondDetailRequestResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class BBPastDetailMessageAdapter extends BaseAdapter {

    private Context context;
    ArrayList<String> dateArray;
    private ArrayList<BBGetRespondDetailRequestResponse.ResponderDetailData.ResponderDetailRequestData.TypeData> messageList;
    private BBAddPictureRequestAdapter BBAddPictureRequestAdapter;
    ArrayList<String> requestAllList = new ArrayList<>();

    public BBPastDetailMessageAdapter(Context context, ArrayList<BBGetRespondDetailRequestResponse.ResponderDetailData.ResponderDetailRequestData.TypeData> messageList, ArrayList<String> dateArray) {
        this.context = context;
        this.dateArray = dateArray;
        this.messageList = messageList;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {

            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.bb_pastrequestdetailadapter, null);
                holder.txtDate = convertView.findViewById(R.id.txtDate);
                holder.txtMessageHistory = convertView.findViewById(R.id.txtMessageHistory);
                holder.txtTime = convertView.findViewById(R.id.txtTime);
                holder.view = convertView.findViewById(R.id.view);
                holder.msgIcon = convertView.findViewById(R.id.msgIcon);
                holder.txtUserName = convertView.findViewById(R.id.txtUserName);
                holder.txtMessage = convertView.findViewById(R.id.txtMessage);
                holder.imageView = convertView.findViewById(R.id.imageView);
                holder.recyclerImages = convertView.findViewById(R.id.recyclerImages);
                holder.lnrBottom = convertView.findViewById(R.id.lnrBottom);
                holder.lnrFirst = convertView.findViewById(R.id.lnrFirst);
                holder.viewattchment = convertView.findViewById(R.id.viewattchment);
                holder.view1 = convertView.findViewById(R.id.view1);
                holder.view2 = convertView.findViewById(R.id.view2);
                holder.view3 = convertView.findViewById(R.id.view3);
                holder.view4 = convertView.findViewById(R.id.view4);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BBGetRespondDetailRequestResponse.ResponderDetailData.ResponderDetailRequestData.TypeData data = messageList.get(position);
            try {
                holder.lnrFirst.setVisibility(View.VISIBLE);
                if (dateArray.get(position).toString().equalsIgnoreCase("")) {
                    holder.txtDate.setVisibility(View.GONE);
                } else {
                    holder.txtDate.setVisibility(View.VISIBLE);
                    holder.txtDate.setText(getFormatedDate(dateArray.get(position)).toString());
                }


            } catch (Exception e) {
                e.printStackTrace();
                holder.lnrFirst.setVisibility(View.GONE);
            }


            if (data.getRequest_by() != null && data.getType().equalsIgnoreCase("user")) {
                holder.msgIcon.setBackgroundResource(R.drawable.reply_icon_bb);
            } else {
                holder.msgIcon.setBackgroundResource(R.drawable.message_icon_bb);

            }

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerImages.setLayoutManager(linearLayoutManager);
            ArrayList<String> requestList = new ArrayList<>();
            ArrayList<String> requestFullImageList = new ArrayList<>();


            try {
                for (int i = 0; i < data.getAttachment_thumb().size(); i++) {
                    requestAllList.add(data.getAttachment_full().get(i));
                    requestList.add(data.getAttachment_thumb().get(i));
                    requestFullImageList.add(data.getAttachment_full().get(i));
                }
                if (requestList.size() > 0) {
                    holder.lnrBottom.setVisibility(View.VISIBLE);
                    BBAddPictureRequestAdapter = new BBAddPictureRequestAdapter(context, requestList, requestFullImageList, requestAllList);
                    holder.recyclerImages.setAdapter(BBAddPictureRequestAdapter);

                } else {
                    holder.lnrBottom.setVisibility(View.GONE);

                }
            } catch (Exception ex) {
                holder.lnrBottom.setVisibility(View.GONE);
            }
            if (data.getMessage() != null) {
                holder.txtMessage.setText(data.getMessage());
            }

            if (data.getDate() != null) {
                holder.txtMessageHistory.setText(data.getDate());
            }

            if (data.getTimestamp() != null) {
                holder.txtTime.setText(BBUtils.getLocalTime(data.getTimestamp()));
            }
            if (data.getName() != null) {
                holder.txtUserName.setText(data.getName());
            }
            holder.txtMessage.post(new Runnable() {
                @Override
                public void run() {
                    int height = holder.txtMessage.getHeight(); //height is ready
                    holder.view1.getLayoutParams().height = height / 2;
                    holder.view1.requestLayout();
                    holder.view2.getLayoutParams().height = height / 2;
                    holder.view2.requestLayout();
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return convertView;
    }

    static class ViewHolder {
        private LinearLayout linearLayout;
        private BBCustomTextView txtDate;
        private BBCustomTextView txtMessageHistory;
        private BBCustomTextView txtTime;
        private View view;
        private ImageView msgIcon;
        private BBCustomTextView txtUserName;
        private BBCustomTextView txtMessage;
        private View hview;
        private ImageView imageView;
        private RecyclerView recyclerImages;
        private LinearLayout lnrBottom;
        private LinearLayout lnrFirst;
        View viewattchment;
        View view1;
        View view2;
        View view3;
        View view4;
    }

    public static String getFormatedDate(String time) {
        SimpleDateFormat inputFormate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormate = new SimpleDateFormat("dd MMM yyyy");
        String output = null;
        try {
            Date date = inputFormate.parse(time);
            output = outputFormate.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return output;
    }

}