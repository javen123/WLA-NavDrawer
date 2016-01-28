package com.wavlite.WLAndroid;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YoutubeConnector {
    private YouTube youtube;
    private YouTube.Search.List query;
    private static final long MAX_RESULTS = 50;

    // Your developer key goes here
    public static final String KEY = DeveloperKey.DEVELOPER_KEY;

    public YoutubeConnector(Context context) {
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName(context.getString(R.string.app_name)).build();

        try{
            query = youtube.search().list("id,snippet");
            query.setKey(KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/thumbnails/default/url)");
        }catch(IOException e){
            Log.d("YC", "Could not initialize: " + e);
        }
    }

    public List<VideoItem> search(String keywords){
        query.setMaxResults(MAX_RESULTS);
        query.setQ(keywords);
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(SearchResult result:results){
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnail(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());
                item.setSelected(false);
                items.add(item);
            }
            return items;
        }catch(IOException e){
            Log.d("YC", "Could not search: "+e);
            return null;
        }
    }

    public List<VideoItem> idSearch (String vidIds){

        try {

            YouTube.Videos.List listVideoRequest = youtube.videos().list("id,snippet").setId(vidIds);
            listVideoRequest.setKey(KEY);
            listVideoRequest.setFields("items(id,snippet/title,snippet/thumbnails/default/url)");

            VideoListResponse listResponse = listVideoRequest.execute();

            List<Video> results = listResponse.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(Video result:results){
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnail(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId());
                item.setSelected(false);
                items.add(item);
            }
            return items;

        } catch (IOException e) {
            Log.d("YC:", e.getLocalizedMessage());
            return null;
        }
    }
}