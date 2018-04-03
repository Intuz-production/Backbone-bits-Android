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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.backbonebits.BBPastRequestMessageDetail;
import com.backbonebits.BBUtils;
import com.backbonebits.R;
import com.backbonebits.customviews.BBCustomBoldTextView;
import com.backbonebits.customviews.BBCustomTextView;
import com.backbonebits.requestresponse.BBGetRespondRequestResponse;

import java.util.ArrayList;

public class BBPastMessageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BBGetRespondRequestResponse.ResponderData> messageList;
    private Activity activity;

    public BBPastMessageAdapter(Context context, ArrayList<BBGetRespondRequestResponse.ResponderData> messageList, Activity activity) {
        this.context = context;
        this.messageList = messageList;
        this.activity=activity;
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
        try{

        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.bb_pastrequestadapter, null);
            holder.imgMessageType = convertView.findViewById(R.id.imgMessageType);
            holder.txtId = convertView.findViewById(R.id.txtId);
            holder.txtMessage = convertView.findViewById(R.id.txtMessage);
            holder.txtMessageHistory = convertView.findViewById(R.id.txtMessageHistory);
            holder.txtDate = convertView.findViewById(R.id.txtDate);
            holder.txtTime = convertView.findViewById(R.id.txtTime);
            holder.txtCount = convertView.findViewById(R.id.txtCount);
            holder.msgIcon = convertView.findViewById(R.id.msgIcon);
            holder.lnrmain = convertView.findViewById(R.id.lnrMain);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final BBGetRespondRequestResponse.ResponderData data = messageList.get(position);
        if (data.getRequest_type() != null && data.getRequest_type().equalsIgnoreCase("bug")) {
            holder.imgMessageType.setBackgroundResource(R.drawable.b_bug_bb);
        }
        if (data.getRequest_type() != null && data.getRequest_type().equalsIgnoreCase("query")) {
            holder.imgMessageType.setBackgroundResource(R.drawable.b_query_bb);

        }
        if (data.getRequest_type() != null && data.getRequest_type().equalsIgnoreCase("feedback")) {
            holder.imgMessageType.setBackgroundResource(R.drawable.b_feedback_bb);

        }

        if(data.getType().equalsIgnoreCase("request"))
        {
            holder.msgIcon.setBackgroundResource(R.drawable.message_icon_bb);
        }
        else
        {
            holder.msgIcon.setBackgroundResource(R.drawable.reply_icon_bb);

        }
        if (data.getMessage_id() != null) {
            holder.txtId.setText(data.getMessage_id());
        }
        if (data.getMessage_id() != null) {
            holder.txtId.setText(data.getMessage_id());
        }
        if (data.getMessage() != null) {
            holder.txtMessage.setText(data.getMessage());
        }

        if (data.getDate() != null) {
            holder.txtMessageHistory.setText(data.getDate());
        }
        if (data.getTimestamp_date() != null) {
            holder.txtDate.setText(data.getTimestamp_date());
        }
        if (data.getTimestamp() != null) {
            holder.txtTime.setText(BBUtils.getLocalTime(data.getTimestamp()));
        }

        holder.txtCount.setText(String.valueOf(data.getMessage_count()));

        holder.lnrmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, BBPastRequestMessageDetail.class);
                i.putExtra("request_id", data.getMessage_id());
                context.startActivity(i);
                activity.finish();
                activity.overridePendingTransition(0, 0);


            }
        });
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return convertView;
    }

    static class ViewHolder {
        private ImageView imgMessageType;
        private BBCustomTextView txtId;
        private BBCustomBoldTextView txtMessage;
        private BBCustomTextView txtMessageHistory;
        private BBCustomTextView txtDate;
        private BBCustomTextView txtTime;
        private TextView txtCount;
        private ImageView msgIcon;
        private LinearLayout lnrmain;
    }


}