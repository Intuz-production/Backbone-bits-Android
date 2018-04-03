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

package com.backbonebits.webservices;

import android.util.Log;

import com.backbonebits.BBUtils;
import com.backbonebits.requestresponse.BBFaqHelpRequestResponse;
import com.backbonebits.requestresponse.BBGetRespondDetailRequestResponse;
import com.backbonebits.requestresponse.BBGetRespondRequestResponse;
import com.backbonebits.requestresponse.BBImageHelpRequestResponse;
import com.backbonebits.requestresponse.BBMessageCountRequestResponse;
import com.backbonebits.requestresponse.BBSaveRespondRequestResponse;
import com.backbonebits.requestresponse.BBStatusMenuRequestResponse;
import com.backbonebits.requestresponse.BBVideoHelpRequestResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public class BBWebServiceCaller {

    private static WebServiceApiInterface webApiInterface;

    public static WebServiceApiInterface getClient() {
        if (webApiInterface == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okclient = new OkHttpClient.Builder().addInterceptor(logging).build();
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            try {
                Retrofit client = new Retrofit.Builder()
                        .baseUrl(BBUtils.BASE_URL)
                        .client(okclient.newBuilder().connectTimeout(10, TimeUnit.MINUTES).readTimeout(100, TimeUnit.SECONDS).writeTimeout(100, TimeUnit.SECONDS).addInterceptor(logging).build())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
                webApiInterface = client.create(WebServiceApiInterface.class);
            } catch (Exception e) {
                e.printStackTrace();
                if(e.getMessage().contains("Illegal URL"))
                {
                    Log.e(e.getMessage(),"Please replace your server URL in BBUtils Class of SDK.");
                }

            }
        }
        return webApiInterface;
    }

    public interface WebServiceApiInterface {

        @FormUrlEncoded
        @POST("get-status-menu.php")
        Call<BBStatusMenuRequestResponse> getStatusMenu(@Field("secret_key") String secret_key,
                                                        @Field("app_id") String app_id);

        @FormUrlEncoded
        @POST("get-help.php")
        Call<BBImageHelpRequestResponse> getImageHelper(@Field("secret_key") String secret_key,
                                                        @Field("app_id") String app_id,
                                                        @Field("flag") String flag,
                                                        @Field("ver_id") String ver_id);

        @FormUrlEncoded
        @POST("get-help.php")
        Call<BBVideoHelpRequestResponse> getVideoHelper(@Field("secret_key") String secret_key,
                                                        @Field("app_id") String app_id,
                                                        @Field("flag") String flag,
                                                        @Field("ver_id") String ver_id);

        @FormUrlEncoded
        @POST("get-help.php")
        Call<BBFaqHelpRequestResponse> getFaqHelper(@Field("secret_key") String secret_key,
                                                    @Field("app_id") String app_id,
                                                    @Field("flag") String flag,
                                                    @Field("ver_id") String ver_id);

        @FormUrlEncoded
        @POST("get-message-count.php")
        Call<BBMessageCountRequestResponse> getMessageCount(@Field("device_id") String ver_id);

        @Multipart
        @POST("save-respond.php")
        Call<BBSaveRespondRequestResponse> saveRespondData(@Part("secret_key") RequestBody secret_key,
                                                           @Part("app_id") RequestBody app_id,
                                                           @Part("request_id") RequestBody request_id,
                                                           @Part("request_type") RequestBody request_type,
                                                           @Part("name") RequestBody name,
                                                           @Part("email") RequestBody email,
                                                           @Part("message") RequestBody message,
                                                           @Part("region") RequestBody region,
                                                           @Part("version") RequestBody version,
                                                           @Part("app_version") RequestBody app_version,
                                                           @Part("device") RequestBody device,
                                                           @Part("subject") RequestBody subject,
                                                           @Part("phone") RequestBody phone,
                                                           @Part("os_type") RequestBody os_type,
                                                           @PartMap Map<String, RequestBody> attachments,
                                                           @Part("device_id") RequestBody device_id,
                                                           @Part("device_token") RequestBody device_token,
                                                           @Part("is_live") RequestBody is_live);


        @FormUrlEncoded
        @POST("get-respond.php")
        Call<BBGetRespondRequestResponse> getRespondData(@Field("secret_key") String secret_key,
                                                         @Field("app_id") String app_id,
                                                         @Field("flag") String flag,
                                                         @Field("device_id") String device_id

        );

        @FormUrlEncoded
        @POST("get-respond-detail.php")
        Call<BBGetRespondDetailRequestResponse> getRespondDetail(@Field("secret_key") String secret_key,
                                                                 @Field("app_id") String app_id,
                                                                 @Field("request_id") String request_id,
                                                                 @Field("flag") String flag

        );


    }
}