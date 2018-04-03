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

package com.backbonebits.requestresponse;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class BBGetRespondDetailRequestResponse {
    private ResponderDetailData data;
    private int status;
    private String server_place;
    private String msg;

    public ResponderDetailData getData() {
        return data;
    }

    public void setData(ResponderDetailData data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getServerPlace() {
        return server_place;
    }

    public void setServerPlace(String serverPlace) {
        this.server_place = serverPlace;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public class ResponderDetailData {
        private String request_type;
        private String email;
        private String name;
        private Map<String, List<ResponderDetailRequestData.TypeData>> request_data;

        public String getRequest_type() {
            return request_type;
        }

        public void setRequest_type(String request_type) {
            this.request_type = request_type;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, List<ResponderDetailRequestData.TypeData>> getRequest_data() {
            return request_data;
        }

        public void setRequest_data(Map<String, List<ResponderDetailRequestData.TypeData>> request_data) {
            this.request_data = request_data;
        }

        public class ResponderDetailRequestData {
            @SerializedName("request_data")
            Map<String, List<TypeData>> request_data;

            public Map<String, List<TypeData>> getResult() {
                return request_data;
            }

            public void setResult(Map<String, List<TypeData>> request_data) {
                this.request_data = request_data;
            }


                public class TypeData {
                    private String type;
                    private String message_id;
                    private String date;
                    private String timestamp;
                    private String name;
                    private String request_by;
                    private String request_type;
                    private String os;
                    private String version;
                    private String message;
                    private List<String> attachment_thumb = new ArrayList<String>();
                    private List<String> attachment_full = new ArrayList<String>();


                    public List<String> getAttachment_thumb() {
                        return attachment_thumb;
                    }

                    public void setAttachment_thumb(List<String> attachment_thumb) {
                        this.attachment_thumb = attachment_thumb;
                    }

                    public List<String> getAttachment_full() {
                        return attachment_full;
                    }

                    public void setAttachment_full(List<String> attachment_full) {
                        this.attachment_full = attachment_full;
                    }



                    public String getType() {
                        return type;
                    }

                    public void setType(String type) {
                        this.type = type;
                    }

                    public String getMessage_id() {
                        return message_id;
                    }

                    public void setMessage_id(String message_id) {
                        this.message_id = message_id;
                    }

                    public String getDate() {
                        return date;
                    }

                    public void setDate(String date) {
                        this.date = date;
                    }

                    public String getTimestamp() {
                        return timestamp;
                    }

                    public void setTimestamp(String timestamp) {
                        this.timestamp = timestamp;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getRequest_by() {
                        return request_by;
                    }

                    public void setRequest_by(String request_by) {
                        this.request_by = request_by;
                    }

                    public String getRequest_type() {
                        return request_type;
                    }

                    public void setRequest_type(String request_type) {
                        this.request_type = request_type;
                    }

                    public String getOs() {
                        return os;
                    }

                    public void setOs(String os) {
                        this.os = os;
                    }

                    public String getVersion() {
                        return version;
                    }

                    public void setVersion(String version) {
                        this.version = version;
                    }

                    public String getMessage() {
                        return message;
                    }

                    public void setMessage(String message) {
                        this.message = message;
                    }


                }

        }
    }
}
