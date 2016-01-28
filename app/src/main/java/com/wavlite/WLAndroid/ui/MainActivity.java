package com.wavlite.WLAndroid.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;
import com.wavlite.WLAndroid.AlertDialogFragment;
import com.wavlite.WLAndroid.R;
import com.wavlite.WLAndroid.TrialPeriodTimer;

import java.util.Arrays;
import java.util.Date;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    public static final String TAG = MainActivity.class.getSimpleName();

    private TrialPeriodTimer trialPeriodTimer;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_drawer);


        //replace actionbar with toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //find drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar,R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nvView);
        nvDrawer.setNavigationItemSelectedListener(this);

        if (!isNetworkAvailable()) {
            AlertDialogFragment.dataConnection(this);
        }

        //set up fragment manager
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame,new FragmentHome()).commit();


        // confirm youTube API Check
        checkYouTubeApi();

        // grab user info

        if (ParseUser.getCurrentUser() == null){
            parseLoginHelper();
        }
    }  // onCreate

    @Override
    protected void onResume() {
        super.onResume();

        checkForSignup();

        if (ParseUser.getCurrentUser() != null){

            AppEventsLogger.activateApp(this);

            //        trial period status check

            ParseUser curUser = ParseUser.getCurrentUser();

            long dateToday = new Date().getTime();

            trialPeriodTimer = new TrialPeriodTimer();
            trialPeriodTimer.setEndDate(curUser.getCreatedAt());
            trialPeriodTimer.setIsTrialOver(curUser.getBoolean("isTrialOver"));

             if (trialPeriodTimer.getEnddate() < dateToday){
                if (trialPeriodTimer.getIsTrialOver() != false){
                    AlertDialogFragment.trialAlert(this);
                }
            }


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    private void checkYouTubeApi() {
        YouTubeInitializationResult errorReason = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.cancel();
                }
            });
        }
        else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            String errorMessage = String.format(getString(R.string.ma_error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, LENGTH_LONG).show();  // error initializing
        }
    }  // checkYouTubeApi


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }  // onCreateOptionsMenu


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.home:
                com.wavlite.WLAndroid.ui.MyLists.myArrayTitles.clear();
                ParseUser.logOut();
                navigateToLogin();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }  // onOptionsItemSelected

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void navigateToLogin() {
        MyLists.myArrayTitles.clear();
        parseLoginHelper();
    }  // navigateToLogin


    private void parseLoginHelper() {
        ParseLoginBuilder builder = new ParseLoginBuilder(MainActivity.this);
        // ic_launcher_192 should not be used. Use ic_login (not yet available) instead
        // These can be put into string resources as well
        Intent parseLoginIntent = builder.setAppLogo(R.drawable.ic_launcher_192)
                .setParseLoginEnabled(true)
                .setParseLoginButtonText(getString(R.string.parse_LoginButtonText))
                .setParseSignupButtonText(getString(R.string.parse_SignupButtonText))
                .setParseLoginHelpText(getString(R.string.parse_LoginHelpText))
                .setParseLoginInvalidCredentialsToastText(getString(R.string.parse_LoginInvalid))
                .setParseLoginEmailAsUsername(true)
                .setParseSignupSubmitButtonText(getString(R.string.parse_SignupSubmitButtonText))
                .setTwitterLoginEnabled(true)
                .setFacebookLoginEnabled(true)
                .setFacebookLoginPermissions(Arrays.asList("public_profile","email"))
                .build();
        startActivityForResult(parseLoginIntent, 0);
    }  // parseLoginHelper


    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }  // isNetworkAvailable





    private void checkForSignup(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean signedup = sharedPreferences.getBoolean("ISSIGNEDUP", false);
        if(signedup){
            AlertDialogFragment.trialBeginAlert(this);
            sharedPreferences.edit().putBoolean("ISSIGNEDUP", false).apply();
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        Fragment ft = fragmentManager.findFragmentById(R.id.content_frame);

        int id = menuItem.getItemId();
        if (id == R.id.nav_home){
            if (ft instanceof FragmentHome){

            } else {
                fragmentManager.beginTransaction().replace(R.id.content_frame,new FragmentHome()).addToBackStack(null).commit();
            }
        } else if (id == R.id.nav_my_lists){
            if(ft instanceof FragmentMyLists){

            } else {
                fragmentManager.beginTransaction().replace(R.id.content_frame,new FragmentMyLists()).addToBackStack(null).commit();
            }
        } else {
            com.wavlite.WLAndroid.ui.MyLists.myArrayTitles.clear();
            ParseUser.logOut();
            navigateToLogin();
        }

        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
        return true;
    }
}  // MainActivity
