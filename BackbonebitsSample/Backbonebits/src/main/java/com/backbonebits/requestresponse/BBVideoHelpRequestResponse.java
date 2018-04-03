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

public class BBVideoHelpRequestResponse {
    int status;
    String msg;
    VideoHelperData data;

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

    public VideoHelperData getData() {
        return data;
    }

    public void setData(VideoHelperData data) {
        this.data = data;
    }

    public class VideoHelperData {
        VideoList video;

        public VideoList getVideo() {
            return video;
        }

        public void set(VideoList video) {
            this.video = video;
        }

        public class VideoList {
            String video_name;
            String video_type;
            String live_date;
            String tutorial_video;

            public String getVideo_name() {
                return video_name;
            }

            public void setVideo_name(String video_name) {
                this.video_name = video_name;
            }

            public String getVideo_type() {
                return video_type;
            }

            public void setVideo_type(String video_type) {
                this.video_type = video_type;
            }

            public String getLive_date() {
                return live_date;
            }

            public void setLive_date(String live_date) {
                this.live_date = live_date;
            }

            public String getTutorial_video() {
                return tutorial_video;
            }

            public void setTutorial_video(String tutorial_video) {
                this.tutorial_video = tutorial_video;
            }
        }
    }
}
