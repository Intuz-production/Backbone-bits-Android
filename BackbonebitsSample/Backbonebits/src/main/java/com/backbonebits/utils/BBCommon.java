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

package com.backbonebits.utils;

import android.os.Environment;
import android.view.View;

import java.io.File;

public class BBCommon {

	
	public static String strPathForVideos = Environment.getExternalStorageDirectory().toString()+"/Backbonebits/Video" ;
	public static String strPathForImages = Environment.getExternalStorageDirectory().toString()+"/Backbonebits/Images" ;

	public static String strRoot = Environment.getExternalStorageDirectory().toString() ;
	public static String strSubPathForVideos = "/Backbonebits/Video" ;
	public static boolean isServiceRunning = false;
	
	public static View view;
	
	public static File ScreenCapturedFile = null;
    public static int requestFrom =0;  // 0 = normal  , 1= image  , 2=video
	public static String strImageName = "";
	public static String CapturedScreen = null;
	public static String strVideoPath = "";
	public static boolean isMenuShowing = false;
	public static boolean isMenuCodeExecute = false;
	

}
