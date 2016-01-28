package com.wavlite.WLAndroid;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by javen on 7/21/15.
 */
public class ListTuple implements Serializable {

    String objectId;
    ArrayList<String> videoIds;



    public ArrayList<String> getVideoIds() {
        return videoIds;
    }

    public void setVideoIds(ArrayList<String> videoIds) {
        this.videoIds = videoIds;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public ListTuple(String objectId, ArrayList<String> videoIds){
        this.objectId = objectId;
        this.videoIds = videoIds;
    }
}
