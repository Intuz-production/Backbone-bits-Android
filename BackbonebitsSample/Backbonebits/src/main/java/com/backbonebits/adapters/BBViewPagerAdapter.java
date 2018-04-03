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
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.backbonebits.BackbonebitsUILApplication;
import com.backbonebits.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

public class BBViewPagerAdapter extends PagerAdapter {

    private Context mContext;
    ArrayList<String> mResources;
    private DisplayImageOptions options;
    private ImageLoader imageloader;

    public BBViewPagerAdapter(Context mContext, ArrayList<String> mResources) {
        try
        {
        this.mContext = mContext;
        this.mResources = mResources;
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .cacheOnDisk(true)
                .showImageOnLoading(R.drawable.gallery_icon_bb)
                .showImageForEmptyUri(R.drawable.gallery_icon_bb)
                .showImageOnFail(R.drawable.gallery_icon_bb)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        imageloader = BackbonebitsUILApplication.initImageLoader(mContext);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = null;
        try{
         itemView = LayoutInflater.from(mContext).inflate(R.layout.bb_pager_item, container, false);

        ImageView imageView = itemView.findViewById(R.id.img_pager_item);

        imageloader.displayImage(mResources.get(position), imageView, options);


        container.addView(itemView);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}