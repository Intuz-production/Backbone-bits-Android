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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.backbonebits.customviews.BBCustomBoldTextView;
import com.backbonebits.utils.BBCommon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BBScreenShotEditActivity extends Activity implements View.OnTouchListener {
    private LinearLayout ll_pallete;
    private RelativeLayout lnrTop;
    private BBDrawingView mBBDrawingView;
    private ViewGroup mBrushPanel;
    private ViewGroup mBrushColors;
    BBCustomBoldTextView imgBackBtn;
    private String imagePath = "";
    LinearLayout ll_color, ll_text, ll_undo;
    private String textToDraw = null;
    private boolean isTextModeOn = false;
    private ImageView sendButton;
    public static FrameLayout fl;
    EditText txtEnteredText = null;
    RelativeLayout relAddText;
    EditText edtAddText;
    ImageView imgCloseText;
    private LayoutInflater inflater;
    ImageView imgUndo;
    ImageView imgText;
    ImageView imgColor;
    TextView txtUndo;
    TextView txtText;
    TextView txtColor;
    TableRow tableRow;
    static int[] COLORS = {

            Color.rgb(229, 80, 35),
            Color.rgb(7, 0, 122),
            Color.rgb(211, 0, 45),
            Color.rgb(100, 149, 237),
            Color.rgb(142, 64, 32),
            Color.rgb(208, 152, 10),
            Color.rgb(162, 18, 24),
            Color.rgb(86, 86, 86),
            Color.rgb(95, 0, 134),
            Color.rgb(36, 62, 62),
            Color.rgb(88, 58, 196),
            Color.rgb(9, 121, 120),
            Color.rgb(170, 35, 92),
            Color.rgb(166, 96, 55),
            Color.rgb(22, 199, 46),
    };
    private boolean isBrushPanelVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.bb_activity_screen_shot_edit);
            inflater = LayoutInflater.from(this);
            if (getIntent().getStringExtra("path") != null) {
                imagePath = getIntent().getStringExtra("path");
            }
            fl = findViewById(R.id.fl);
            ll_pallete = findViewById(R.id.ll_pallete);
            lnrTop = findViewById(R.id.lnrTop);
            mBBDrawingView = findViewById(R.id.drawing_view);
            mBBDrawingView.setShape(loadFromFile(imagePath));
            mBBDrawingView.setDrawingColor(getResources().getColor(R.color.bb_defalut_brush_color));

            mBrushPanel = findViewById(R.id.brush_panel);
            mBrushColors = findViewById(R.id.brush_colors);
            mBBDrawingView.setDrawingStroke(15);
            imgBackBtn = findViewById(R.id.imgBackBtn);

            ll_color = findViewById(R.id.ll_color);
            ll_text = findViewById(R.id.ll_text);
            ll_undo = findViewById(R.id.ll_undo);
            sendButton = findViewById(R.id.imgsendBtn);
            imgColor = findViewById(R.id.imgColor);
            imgText = findViewById(R.id.imgText);
            imgUndo = findViewById(R.id.imgUndo);
            txtColor = findViewById(R.id.txtColor);
            txtText = findViewById(R.id.txtText);
            txtUndo = findViewById(R.id.txtUndo);
            imgBackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BBScreenShotEditActivity.this.finish();

                    try {
                        if (BBCommon.requestFrom == 0) {
                            Intent i = new Intent(BBScreenShotEditActivity.this, Backbonebits.class);
                            i.putExtra("openHelp", "yes");
                            startActivity(i);
                            finish();
                            overridePendingTransition(0, 0);

                        } else {
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (txtEnteredText != null) {
                        txtEnteredText.clearFocus();
                        txtEnteredText.setCursorVisible(false);
                        BBScreenShotEditActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                        txtEnteredText.setBackgroundResource(R.color.bb_transparentcolor);

                        txtEnteredText.setBackgroundResource(0);
                        txtEnteredText.setBackground(null);
                    }
                    fl.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    fl.setDrawingCacheEnabled(true);
                    fl.buildDrawingCache();
                    Bitmap viewCache = fl.getDrawingCache();
                    Bitmap bitmap = viewCache.copy(viewCache.getConfig(), false);
                    fl.setDrawingCacheEnabled(false);

                    new SaveTask().execute(bitmap);
                }
            });

            ll_color.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imgColor.setImageDrawable(getResources().getDrawable(R.drawable.colorpicker_slticon_bb, getApplicationContext().getTheme()));
                        imgText.setImageDrawable(getResources().getDrawable(R.drawable.text_icon_bb, getApplicationContext().getTheme()));
                        imgUndo.setImageDrawable(getResources().getDrawable(R.drawable.undo_icon_bb, getApplicationContext().getTheme()));

                    } else {
                        imgColor.setImageDrawable(getResources().getDrawable(R.drawable.colorpicker_slticon_bb));
                        imgText.setImageDrawable(getResources().getDrawable(R.drawable.text_icon_bb));
                        imgUndo.setImageDrawable(getResources().getDrawable(R.drawable.undo_icon_bb));

                    }
                    txtColor.setTextColor(getResources().getColor(R.color.bb_lightbluetextclor));
                    txtText.setTextColor(getResources().getColor(R.color.bb_whitecolor));
                    txtUndo.setTextColor(getResources().getColor(R.color.bb_whitecolor));
                    if (mBrushPanel.getVisibility() == View.VISIBLE) {
                        mBrushPanel.setVisibility(View.GONE);
                        isBrushPanelVisible = false;
                    } else {
                        mBrushPanel.setVisibility(View.VISIBLE);
                    }
                }
            });
            ll_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imgColor.setImageDrawable(getResources().getDrawable(R.drawable.colorpicker_icon_bb, getApplicationContext().getTheme()));
                        imgText.setImageDrawable(getResources().getDrawable(R.drawable.text_slticon_bb, getApplicationContext().getTheme()));
                        imgUndo.setImageDrawable(getResources().getDrawable(R.drawable.undo_icon_bb, getApplicationContext().getTheme()));

                    } else {
                        imgColor.setImageDrawable(getResources().getDrawable(R.drawable.colorpicker_icon_bb));
                        imgText.setImageDrawable(getResources().getDrawable(R.drawable.text_slticon_bb));
                        imgUndo.setImageDrawable(getResources().getDrawable(R.drawable.undo_icon_bb));

                    }
                    txtColor.setTextColor(getResources().getColor(R.color.bb_whitecolor));
                    txtText.setTextColor(getResources().getColor(R.color.bb_lightbluetextclor));
                    txtUndo.setTextColor(getResources().getColor(R.color.bb_whitecolor));
                    isTextModeOn = true;
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInputFromWindow(ll_text.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                    AddTextView(textToDraw);

                }
            });
            ll_undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imgColor.setImageDrawable(getResources().getDrawable(R.drawable.colorpicker_icon_bb, getApplicationContext().getTheme()));
                        imgText.setImageDrawable(getResources().getDrawable(R.drawable.text_icon_bb, getApplicationContext().getTheme()));
                        imgUndo.setImageDrawable(getResources().getDrawable(R.drawable.undo_slticon_bb, getApplicationContext().getTheme()));

                    } else {
                        imgColor.setImageDrawable(getResources().getDrawable(R.drawable.colorpicker_icon_bb));
                        imgText.setImageDrawable(getResources().getDrawable(R.drawable.text_icon_bb));
                        imgUndo.setImageDrawable(getResources().getDrawable(R.drawable.undo_slticon_bb));

                    }
                    txtColor.setTextColor(getResources().getColor(R.color.bb_whitecolor));
                    txtText.setTextColor(getResources().getColor(R.color.bb_whitecolor));
                    txtUndo.setTextColor(getResources().getColor(R.color.bb_lightbluetextclor));
                    mBBDrawingView.undoOperation(fl);
                }
            });



            mBrushPanel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mBrushPanel.getViewTreeObserver().removeOnPreDrawListener(this);
                    mBrushPanel.setTranslationY(isLandscape() ?
                            -mBrushPanel.getHeight() : mBrushPanel.getHeight());
                    return false;
                }
            });

            createBrushPanelContent();
        } catch (Exception ex) {

        }
        mBBDrawingView.setOnTouchListener(this);


    }

    private void hideTopBottom(View v, MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            ll_pallete.setVisibility(View.GONE);
            lnrTop.setVisibility(View.GONE);
            if (mBrushPanel.getVisibility() == View.VISIBLE) {
                mBrushPanel.setVisibility(View.GONE);
                isBrushPanelVisible = true;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            ll_pallete.setVisibility(View.VISIBLE);
            lnrTop.setVisibility(View.VISIBLE);
            if (isBrushPanelVisible)
                mBrushPanel.setVisibility(View.VISIBLE);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            ll_pallete.setVisibility(View.GONE);
            lnrTop.setVisibility(View.GONE);
            if (mBrushPanel.getVisibility() == View.VISIBLE) {
                mBrushPanel.setVisibility(View.GONE);
                isBrushPanelVisible = true;
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        hideTopBottom(v, ev);
        return false;
    }


    @SuppressWarnings("null")
    private void createBrushPanelContent() {
        try {

            final int rowLimit = isLandscape() ? 16 : 8;
            for (int i = 0; i < COLORS.length; i++) {
                tableRow = new TableRow(this);

                TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                mBrushColors.addView(tableRow, params);


                tableRow.addView(createToolButton(tableRow, R.drawable.bb_color_round, i));
            }
        } catch (Exception ex) {

        }
    }



    private boolean isLandscape() {
        return getResources().getBoolean(R.bool.bb_is_landscape);
    }

    private ImageButton createToolButton(ViewGroup parent, int drawableResId, int index) {
        ImageButton button = null;
        try {
            button = (ImageButton) getLayoutInflater().inflate(R.layout.bb_button_paint_spot, parent, false);

            button.setImageResource(drawableResId);
            if (index == 0) {
                button.setBackgroundResource(R.drawable.bb_color_bg_round);
            }


            button.setOnClickListener(mButtonClick);
            if (index != -1) {
                button.setTag(Integer.valueOf(index));
                button.setColorFilter(COLORS[index]);
            }
        } catch (Exception ex) {

        }
        return button;
    }

    private View.OnClickListener mButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                for (int i = 0; i < mBrushColors.getChildCount(); i++) {

                    ViewGroup child = (ViewGroup) mBrushColors.getChildAt(i);

                    child.getChildAt(0).setBackgroundResource(0);
                }
                mBBDrawingView.setDrawingColor(COLORS[((Integer) v.getTag()).intValue()]);
                v.setBackgroundResource(R.drawable.bb_color_bg_round);
                if (mBrushPanel.getVisibility() == View.VISIBLE) {
                    mBrushPanel.setVisibility(View.GONE);
                    isBrushPanelVisible = false;
                } else {
                    mBrushPanel.setVisibility(View.VISIBLE);
                }
            } catch (Exception ex) {

            }
        }
    };

    private class SaveTask extends AsyncTask<Bitmap, Void, File> {
        private ProgressDialog mProgressDialog;


        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(BBScreenShotEditActivity.this);
            mProgressDialog.setMessage(getString(R.string.saving_bb));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
            if (txtEnteredText != null) {
                txtEnteredText.setBackgroundResource(R.color.bb_transparentcolor);

                txtEnteredText.setBackgroundResource(0);
                txtEnteredText.setBackground(null);
            }
        }

        @Override
        protected void onPostExecute(File result) {
            try {
                mProgressDialog.dismiss();


                if (result != null) {
                    BBCommon.requestFrom = 1;
                    Intent i = new Intent(BBScreenShotEditActivity.this, BBNewRequestDialogActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                    overridePendingTransition(0, 0);

                }
            } catch (Exception ex) {

            }
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        protected File doInBackground(Bitmap... params) {
            File result = null;
            try {
                String name = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date());
                result = new File(Environment.getExternalStorageDirectory(), "BB_" + name + ".png");

                FileOutputStream stream = null;
                try {
                    try {
                        stream = new FileOutputStream(result);
                        if (params[0].compress(Bitmap.CompressFormat.PNG, 75, stream)) {
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(result)));
                        } else {
                            result = null;
                        }
                        BBCommon.CapturedScreen = result.getAbsolutePath();
                    } finally {
                        if (stream != null) {
                            stream.close();
                        }
                    }
                } catch (IOException e) {
                    result = null;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //
                }
            } catch (Exception ex) {

            }
            return result;
        }
    }

    public static Bitmap loadFromFile(String filename) {
        try {
            File f = new File(filename);
            if (!f.exists()) {
                return null;
            }
            Bitmap tmp = BitmapFactory.decodeFile(filename);
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BBScreenShotEditActivity.this.finish();
        new Backbonebits(Backbonebits.context).getStatusMenu(Backbonebits.context);
    }

    @SuppressLint("NewApi")
    void AddTextView(String strText) {
        try {



            txtEnteredText = new EditText(BBScreenShotEditActivity.this);
            txtEnteredText.setText(strText);
            txtEnteredText.setTextColor(mBBDrawingView.getDrawingColor());
            txtEnteredText.setTextSize(27);
            txtEnteredText.setPadding(10, 0, 10, 0);
            txtEnteredText.requestFocus();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = (int) getResources().getDimension(R.dimen._80sdp);
            params.leftMargin = (int) getResources().getDimension(R.dimen._80sdp);
            txtEnteredText.setLayoutParams(params);
            mBBDrawingView.drawText(txtEnteredText);
            txtEnteredText.setBackgroundResource(R.drawable.dash_border);
            ImageView minusImage = new ImageView(this);
            RelativeLayout.LayoutParams mp = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            mp.rightMargin = 5;
            minusImage.setLayoutParams(mp);
            minusImage.setImageResource(R.drawable.grey_close_bb);
            fl.addView(txtEnteredText);
            txtEnteredText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    fl.removeView(txtEnteredText);
                    return false;
                }
            });
            txtEnteredText.setOnTouchListener(new View.OnTouchListener() {

                @SuppressLint("NewApi")
                @Override
                public boolean onTouch(View v, MotionEvent event) {


                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        // Offsets are for centering the TextView on the touch location
                        v.setX(event.getRawX() - v.getWidth() / 2.0f);
                        v.setY(event.getRawY() - v.getHeight() / 2.0f);


                    } else if (event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {


                    } else if (event.getAction() == MotionEvent.ACTION_POINTER_UP) {

                    }
                    hideTopBottom(v, event);


                    return true;
                }

            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}