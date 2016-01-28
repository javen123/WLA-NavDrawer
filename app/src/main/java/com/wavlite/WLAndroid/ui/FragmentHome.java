package com.wavlite.WLAndroid.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.wavlite.WLAndroid.R;

/**
 * Created by javen on 1/8/16.
 */
public class FragmentHome extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // set up webview
        WebView webview = (WebView)rootView.findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("http://www.wavlite.com/api/videoPlayer.html");

        loadMusicGenreButtons(rootView);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }


    private void loadMusicGenreButtons (View view) {

        Button btnRock = (Button)view.findViewById(R.id.btn_rock);
        btnRock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+rock");

            }
        });
        Button btnJazz = (Button)view.findViewById(R.id.btn_jazz);
        btnJazz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+jazz");

            }
        });
        Button btnHipHop = (Button)view.findViewById(R.id.btn_hiphop);
        btnHipHop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+hiphop");

            }
        });
        Button btnCountry = (Button) view.findViewById(R.id.btn_country);
        btnCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+country");

            }
        });
        Button btnBlues = (Button)view.findViewById(R.id.btn_blues);
        btnBlues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+blues");

            }
        });
        Button btnRNB = (Button) view.findViewById(R.id.btn_rnb);
        btnRNB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGenreSearchParams("top+new+R&B");

            }
        });
    }

    private void setGenreSearchParams (String genre){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("YTSEARCH", genre).apply();
    }
}
