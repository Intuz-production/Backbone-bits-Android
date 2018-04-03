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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.backbonebits.R;
import com.backbonebits.customviews.BBCustomGallery;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

public class BBGalleryAdapter extends BaseAdapter {

    private LayoutInflater infalter;
    public ArrayList<BBCustomGallery> data = new ArrayList<BBCustomGallery>();
    ImageLoader imageLoader;
    String from;
    private boolean isActionMultiplePick;

    public BBGalleryAdapter(Context c, ImageLoader imageLoader, String from) {
        try {

            infalter = (LayoutInflater) c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.from = from;
            this.imageLoader = imageLoader;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public BBCustomGallery getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setMultiplePick(boolean isMultiplePick) {
        this.isActionMultiplePick = isMultiplePick;
    }

    public void selectAll(boolean selection) {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).isSeleted = selection;

        }
        notifyDataSetChanged();
    }

    public boolean isAllSelected() {
        boolean isAllSelected = true;

        for (int i = 0; i < data.size(); i++) {
            if (!data.get(i).isSeleted) {
                isAllSelected = false;
                break;
            }
        }

        return isAllSelected;
    }

    public boolean isAnySelected() {
        boolean isAnySelected = false;

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSeleted) {
                isAnySelected = true;
                break;
            }
        }

        return isAnySelected;
    }

    public ArrayList<BBCustomGallery> getSelected() {
        ArrayList<BBCustomGallery> dataT = new ArrayList<BBCustomGallery>();

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSeleted) {
                dataT.add(data.get(i));
            }
        }

        return dataT;
    }

    public void addAll(ArrayList<BBCustomGallery> files) {

        try {
            this.data.clear();
            this.data.addAll(files);

        } catch (Exception e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }

    public void changeSelection(View v, int position) {
        try {
            data.get(position).isSeleted = !data.get(position).isSeleted;

            ((ViewHolder) v.getTag()).imgQueueMultiSelected.setSelected(data
                    .get(position).isSeleted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            final ViewHolder holder;
            if (convertView == null) {

                convertView = infalter.inflate(R.layout.bb_gallery_item, null);
                holder = new ViewHolder();
                holder.imgQueue = convertView
                        .findViewById(R.id.img1);

                holder.imgQueueMultiSelected = convertView
                        .findViewById(R.id.imgQueueMultiSelected);
                holder.img_delete_1 = convertView
                        .findViewById(R.id.img_delete_1);
                if (from.equalsIgnoreCase("bb_gallery")) {
                    holder.img_delete_1.setVisibility(View.GONE);
                }
                holder.img_delete_1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        data.remove(position);
                        notifyDataSetChanged();
                    }
                });
                if (isActionMultiplePick) {
                    holder.imgQueueMultiSelected.setVisibility(View.VISIBLE);
                } else {
                    holder.imgQueueMultiSelected.setVisibility(View.GONE);
                }

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.imgQueue.setTag(position);

            try {

                imageLoader.displayImage("file://" + data.get(position).sdcardPath,
                        holder.imgQueue, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                holder.imgQueue
                                        .setImageResource(R.drawable.gallery_icon_bb);
                                super.onLoadingStarted(imageUri, view);
                            }
                        });

                if (isActionMultiplePick) {

                    holder.imgQueueMultiSelected
                            .setSelected(data.get(position).isSeleted);


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return convertView;
    }

    public class ViewHolder {
        ImageView imgQueue;
        ImageView imgQueueMultiSelected;
        ImageView img_delete_1;
    }

    public void clearCache() {
        imageLoader.clearDiscCache();
        imageLoader.clearMemoryCache();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}
