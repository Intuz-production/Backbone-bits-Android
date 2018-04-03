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

import java.util.ArrayList;
import java.util.List;


public class BBGetRespondRequestResponse {

    private List<ResponderData> data = new ArrayList<ResponderData>();
    private int  status;
    private String msg;

    public List<ResponderData> getData() {
        return data;
    }

    public void setData(List<ResponderData> data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public class ResponderData {
        private String type;
        private String message_id;
        private String date;
        private String timestamp_date;
        private String timestamp;
        private String request_by;
        private String owner_name;
        private String request_type;
        private String os;
        private String version;
        private String message;
        private String name;
        private int message_count;
        private int unread_count;


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

        public String getTimestamp_date() {
            return timestamp_date;
        }

        public void setTimestamp_date(String timestamp_date) {
            this.timestamp_date = timestamp_date;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getRequest_by() {
            return request_by;
        }

        public void setRequest_by(String request_by) {
            this.request_by = request_by;
        }

        public String getOwner_name() {
            return owner_name;
        }

        public void setOwner_name(String owner_name) {
            this.owner_name = owner_name;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getMessage_count() {
            return message_count;
        }

        public void setMessage_count(int message_count) {
            this.message_count = message_count;
        }

        public int getUnread_count() {
            return unread_count;
        }

        public void setUnread_count(int unread_count) {
            this.unread_count = unread_count;
        }

        private class AttachmentLatestThumb {
        }

        private class AttachmentLatest {
        }

        private class Attachment {
        }
    }
}
