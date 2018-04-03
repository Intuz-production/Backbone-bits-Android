//  Copyright 2018 Intuz Solutions Pvt Ltd.

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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.backbonebits.R;
import com.backbonebits.requestresponse.BBFaqs;

import java.util.ArrayList;

public class BBExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<BBFaqs> alldataList;
    private ArrayList<BBFaqs> originalList;


    public BBExpandableListAdapter(Context context, ArrayList<BBFaqs> alldataList) {
        this.context = context;
        this.alldataList = alldataList;
        this.originalList = new ArrayList<BBFaqs>();
        this.originalList.addAll(alldataList);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        BBFaqs countryList = alldataList.get(groupPosition);
        return countryList.getAnswer();

    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View view, ViewGroup parent) {
        try {
            String country = (String) getChild(groupPosition, childPosition);

            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.bb_child_row, null);
            }

            TextView code = view.findViewById(R.id.code);
            code.setText(Html.fromHtml(country));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return alldataList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return alldataList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view,
                             ViewGroup parent) {
        try {

            BBFaqs continent = (BBFaqs) getGroup(groupPosition);

            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.bb_group_row, null);
            }
            ImageView icon_expand = view.findViewById(R.id.icon_expand);
            icon_expand.setVisibility(View.VISIBLE);
            TextView heading = view.findViewById(R.id.heading);
            heading.setText(Html.fromHtml(continent.getQuestion().trim()));
            if (isLastChild) {

                icon_expand.setImageResource(R.drawable.down_arrow_bb);
                heading.setTextColor(context.getResources().getColor(R.color.bb_mediumbluecolor));
            } else {

                icon_expand.setImageResource(R.drawable.right_arrow_bb);
                heading.setTextColor(context.getResources().getColor(R.color.bb_text_list_color));

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void filterData(String query) {
        try {
            query = query.toLowerCase();
            alldataList.clear();

            if (query.isEmpty()) {
                alldataList.addAll(originalList);
            } else {

                ArrayList<BBFaqs> newList = new ArrayList<BBFaqs>();
                for (BBFaqs country : originalList) {
                    if (country.getQuestion().toLowerCase().contains(query) ||
                            country.getAnswer().toLowerCase().contains(query)) {
                        newList.add(country);
                    }
                }
                if (newList.size() > 0) {
                    alldataList.addAll(newList);
                }

            }
            notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
