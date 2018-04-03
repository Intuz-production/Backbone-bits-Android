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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.backbonebits.BBUtils;
import com.backbonebits.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

;

public class BBAddPictureRequestAdapter extends RecyclerView.Adapter<BBAddPictureRequestAdapter.ViewHolder> {

    private Context context;
    public ArrayList<String> requestList;
    public ArrayList<String> requestFullImageList;
    public ArrayList<String> requestAllList;

    private final int ACCESS_STORAGE_PERMISSION_REQUEST_CODE = 224;
    private static final int SELECT_PICTURE = 100;
    private String imgPath = "";
    private DisplayImageOptions options;

    public BBAddPictureRequestAdapter(Context context, ArrayList<String> requestList, ArrayList<String> requestFullImageList, ArrayList<String> requestAllList) {
        this.context = context;
        try {
            this.requestList = requestList;
            this.requestFullImageList = requestFullImageList;
            this.requestAllList = requestAllList;
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.gallery_icon_bb)
                    .showImageForEmptyUri(R.drawable.gallery_icon_bb)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bb_gallery_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {

            holder.imageView.setEnabled(true);
            holder.imageView.setClickable(true);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (requestList.get(position).contains(".mp4") || requestList.get(position).contains(".MP4")) {
                        openVideoDialog(requestFullImageList.get(position));
                    } else {
                        openImageDialog(requestFullImageList.get(position));
                    }

                }
            });
            if (requestList.get(position).contains(".mp4") || requestList.get(position).contains(".MP4")) {
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER);
                holder.framelayout.setBackgroundResource(0);
                holder.imageView.setImageResource(R.drawable.mp4_bb);


            } else {
                holder.framelayout.setBackgroundResource(R.drawable.bb_whitegray_rounded_corner);

                ImageLoader.getInstance().displayImage(requestFullImageList.get(position), holder.imageView, options);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        FrameLayout framelayout;

        public ViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.item_image);
            framelayout = v.findViewById(R.id.framelayout);
        }
    }

    public void openVideoDialog(String url) {
        try {

            final Dialog videodialog = new Dialog(context, R.style.BBAppTheme);
            videodialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            videodialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            videodialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            videodialog.setCancelable(false);
            videodialog.setContentView(R.layout.bb_videodialog);
            ImageView close_btn = videodialog.findViewById(R.id.close_btn_video);
            final ProgressBar progressbar = videodialog.findViewById(R.id.progressbar);
            close_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videodialog.dismiss();
                }
            });

            final VideoView videoview = videodialog.findViewById(R.id.videoView);
            // Execute StreamVideo AsyncTask

            // Create a progressbar
            BBUtils.showProgress(context);

            try {
                // Start the MediaController
                progressbar.setVisibility(View.VISIBLE);

                MediaController mediacontroller = new MediaController(context);
                mediacontroller.setAnchorView(videoview);
                // Get the URL from String VideoURL
                Uri video = Uri.parse(url);
                videoview.setMediaController(mediacontroller);
                videoview.setVideoURI(video);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            videoview.requestFocus();

            videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                // Close the progress bar and play the video
                public void onPrepared(MediaPlayer mp) {
                    BBUtils.hideProgress();
                    videoview.start();
                    String manufacturer = android.os.Build.MANUFACTURER;
                    if(manufacturer.equalsIgnoreCase("samsung"))
                    {
                        progressbar.setVisibility(View.GONE);

                    }
                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {

                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int arg1, int arg2) {

                            progressbar.setVisibility(View.GONE);
                            videoview.start();
                        }
                    });

                }
            });
            videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videodialog.dismiss();
                }
            });
            if(videodialog!=null && !videodialog.isShowing())
            videodialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openImageDialog(String path) {
        try {
            final Dialog videodialog = new Dialog(context, R.style.BBAppTheme);
            videodialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            videodialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            videodialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            videodialog.setCancelable(false);
            videodialog.setContentView(R.layout.bb_imagedialog);
            final ImageView imageView = videodialog.findViewById(R.id.imageView);
            ImageView close_btn = videodialog.findViewById(R.id.close_btn_image);

            close_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videodialog.dismiss();
                }
            });
            try {
                ImageLoader.getInstance().displayImage(path, imageView, options);


            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            if(videodialog!=null && !videodialog.isShowing())
            videodialog.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
