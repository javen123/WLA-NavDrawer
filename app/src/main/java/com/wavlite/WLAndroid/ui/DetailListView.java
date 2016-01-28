package com.wavlite.WLAndroid.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.wavlite.WLAndroid.AlertDialogFragment;
import com.wavlite.WLAndroid.CreateLists;
import com.wavlite.WLAndroid.DeveloperKey;
import com.wavlite.WLAndroid.ListTuple;
import com.wavlite.WLAndroid.R;
import com.wavlite.WLAndroid.VideoItem;
import com.wavlite.WLAndroid.YoutubeConnector;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class DetailListView extends AppCompatActivity {
    private Bundle args;  // received bundle from MyLists page
    private Handler handler;
    private CheckBox check;
    private Boolean checkActivated = false;
    private ViewGroup deleteBtnView;

    private static List<VideoItem> searchResults;
    private static ListView detailList;
    private static ArrayAdapter adapter;
    private static ProgressBar mProgressBar2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list_view);

        //set progress bar
        mProgressBar2 = (ProgressBar)findViewById(R.id.progressBar2);
        mProgressBar2.setVisibility(View.INVISIBLE);


        // array handler
        handler = new Handler();
        Intent intent = this.getIntent();

        // Grab intents
        args = intent.getBundleExtra("myListids");
        String title = intent.getStringExtra("title");

        // update page title
        this.setTitle(title);

        // convert intent ids to youtube searchable string
        String ids = convertIntentToVideoIds(args);
        detailList = (ListView) findViewById(R.id.detail_list_view);
        YoutubeConnector yc = new YoutubeConnector(DetailListView.this);
        toggleProgressBar();
        searchOnYoutube(ids);
    }  // onCreate


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_list_view, menu);
        return true;
    }  // onCreateOptionsMenu


    private void onSearchPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
    }  // onSearchPressed


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_logout:
                com.wavlite.WLAndroid.ui.MyLists.myArrayTitles.clear();
                ParseUser.logOut();
                navigateToLogin();
                return true;
            case R.id.menu_create_list1:
                CreateLists cl = new CreateLists();
                cl.addNewItemToList(DetailListView.this);
                return true;
            case R.id.edit_list_bulk:
                addDelete();
                return true;
            case R.id.menu_search3:
                ActivityCompat.startActivityForResult(this, new Intent(this, SearchViewActivity.class), 0, null);
                finish();
            case R.id.my_search_lists:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }  // onOptionsItemSelected


    private void navigateToLogin() {
        MyLists.myArrayTitles.clear();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
        this.finish();
    }  // navigateToLogin


    private void searchOnYoutube(final String keywords) {
        new Thread() {
            public void run() {
                YoutubeConnector yc = new YoutubeConnector(DetailListView.this);
                searchResults = yc.idSearch(keywords);
                handler.post(new Runnable() {
                    public void run() {
                        toggleProgressBar();
                        updateVideosFound();
                        Log.d("ITR", ""+ args);
                    }
                });
            }  // run
        }.start();  // Thread
    }  // searchOnYoutube


    private void updateVideosFound() {
        adapter = new ArrayAdapter<VideoItem>(DetailListView.this, R.layout.list_video_item, searchResults) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.list_video_item, parent, false);

                // row item tapped action / send to YouTube player / delete video on longPress
                addRowClickListener();

                // Update listView items
                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.detail_thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.detail_title);
                VideoItem searchResult = searchResults.get(position);
                Picasso.with(getApplicationContext()).load(searchResult.getThumbnail().trim()).resize(106, 60).centerCrop().into(thumbnail);
                title.setText(searchResult.getTitle());
                Log.i("YOU", "Update video");

                // activate checkbox
                check = (CheckBox)convertView.findViewById(R.id.checkBox1);
                if (checkActivated) {
                    check.setVisibility(View.VISIBLE);
                    check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                searchResults.get(position).setSelected(true);
                            }
                            if (!isChecked) {
                                searchResults.get(position).setSelected(false);
                            }
                        }  // onCheckedChanged
                    });  // check.setOnCheckedChangeListener
                }  // if (checkActivated)
                else {
                    check.setVisibility(View.INVISIBLE);
                }
                return convertView;
              }  // getView
        };  // adapter = new ArrayAdapter
        detailList.setAdapter(adapter);
    }  // updateVideosFound


    private void addRowClickListener() {
        if(detailList != null) {
            if (checkActivated == false) {
                detailList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> av, View v, final int pos, long id) {


                        if(YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(DetailListView.this).equals(YouTubeInitializationResult.SUCCESS)){

                            List<String> vidIds = convertSearchResultsToIntentIds(searchResults);
                            Intent intent = YouTubeStandalonePlayer.createVideosIntent(DetailListView.this, DeveloperKey.DEVELOPER_KEY, vidIds, pos, 10, true, true);
                            startActivity(intent);

                        }else if (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(DetailListView.this).equals(YouTubeInitializationResult.SERVICE_INVALID)){

                            AlertDialogFragment.problemWithYouTube(DetailListView.this);

                        } else if (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(DetailListView.this).equals(YouTubeInitializationResult.SERVICE_VERSION_UPDATE_REQUIRED)){
                            AlertDialogFragment.problemWithYouTube(DetailListView.this);

                        } else if(YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(DetailListView.this).equals(YouTubeInitializationResult.SERVICE_MISSING)){
                            AlertDialogFragment.problemWithYouTube(DetailListView.this);

                        } else if (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(DetailListView.this).equals(YouTubeInitializationResult.SERVICE_DISABLED)){
                            AlertDialogFragment.problemWithYouTube(DetailListView.this);
                        }

                    }
                });  // detailList.setOnItemClickListener

                detailList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        String videoTitle = searchResults.get(position).getTitle();
                        final String listId = convertIntentToListId(args);
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetailListView.this);
                        builder.setIcon(R.drawable.ic_dialog);
                        builder.setTitle(getString(R.string.dlv_dialog_title));
                        builder.setMessage("" + videoTitle + "\n ");

                        // DELETE SELECTED SEARCHLIST : DELETE BUTTON
                        builder.setNeutralButton(getString(R.string.btn_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder delete = new AlertDialog.Builder(DetailListView.this);
                                delete.setTitle(getString(R.string.builder_del_sl_confirm));
                                delete.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                delete.setPositiveButton(getString(R.string.btn_delete), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
                                        query.whereEqualTo("objectId", listId);
                                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(ParseObject object, ParseException e) {
                                                if (e != null) {
                                                    Log.d("Parse:", e.getLocalizedMessage());
                                                } else {
                                                    updateListView(DetailListView.this, position, object);
                                                }
                                            }
                                        });
                                    }
                                });
                                AlertDialog a = delete.create();
                                a.show();
                            }  // onClick
                        });  // builder.setNeutralButton

                        // CANCEL BUTTON
                        builder.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        // MOVE TO BUTTON
                        builder.setPositiveButton(getString(R.string.btn_move), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                final String videoId = searchResults.get(position).getId().toString();
                                final ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
                                query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                                query.orderByAscending("listTitle");
                                query.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> list, ParseException e) {
                                        if (e != null) {
                                            Log.d("Error with list pull: ", e.getLocalizedMessage());
                                        } else {
                                            if (list.isEmpty()) {
                                                // do something (This should be logged so there is evidence of it)
                                            } else {
                                                try {
                                                    AlertDialogFragment.adjustListItems(position, list, DetailListView.this, videoId, getApplicationContext());
                                                } catch (Exception e1) {
                                                    throw e1;
                                                } finally {
                                                    searchResults.remove(position);

                                                    final ArrayList<String> temp = new ArrayList<>();
                                                    for (VideoItem x : searchResults) {
                                                        temp.add(x.getId());
                                                    }
                                                    ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Lists");
                                                    query2.getInBackground(listId, new GetCallback<ParseObject>() {
                                                        @Override
                                                        public void done(ParseObject object, ParseException e) {
                                                            object.put("myLists", temp);
                                                            try {
                                                                object.save();
                                                            } catch (ParseException e1) {
                                                                e1.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    }  // done
                                });  // query.findInBackground
                            }  // onClick
                        });  // builder.setPositiveButton
                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;
                    }  // onItemLongClick
                });  // detailList.setOnItemLongClickListener
            }  // if (checkActivated == false)
        }  // if(detailList != null)
    }  // addRowClickListener


    //: HELPERS
    private String convertIntentToVideoIds(Bundle info) {
        ArrayList<ListTuple> object = (ArrayList<ListTuple>) info.getSerializable("ArrayList");
        ListTuple ids = object.get(0);
        ArrayList vIds = ids.getVideoIds();
        String mVIds = TextUtils.join(",", vIds);
        return mVIds;
    }  // convertIntentToVideoIds


    public List<String> convertSearchResultsToIntentIds(List<VideoItem> curList) {
        List<String> temp = new ArrayList<>();
        for(VideoItem x : curList){
            temp.add(x.getId().toString());
        }
        return temp;
    }  // convertSearchResultsToIntentIds


    private String convertIntentToListId(Bundle info) {
        ArrayList<ListTuple> object = (ArrayList<ListTuple>) info.getSerializable("ArrayList");
        ListTuple ids = object.get(0);
        String listId = ids.getObjectId();
        return listId;
    }  // convertIntentToListId


    public static void updateListView(final Activity activity, final int pos, ParseObject newList) {
        searchResults.remove(pos);
        toggleProgressBar();
        //Add back new array list
        if(searchResults.size() == 0) {
            newList.put("myLists", JSONObject.NULL);
            newList.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        // do something (This should be logged so there is evidence of it)
                    }
                    else {
                        activity.finish();
                    }
                }
            });  // newList.saveInBackground
        }
        else {
            ArrayList<String> temp = new ArrayList<>();
            for(VideoItem x : searchResults){
                temp.add(x.getId());
            }
            newList.put("myLists", temp);
            newList.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        //do something (This should be logged so there is evidence of it)
                    }
                    else {
                        adapter.notifyDataSetChanged();
                    }
                }
            });  // newList.saveInBackground
        }
        toggleProgressBar();
    }  // updateListView


    private static void toggleProgressBar() {
        if (mProgressBar2.getVisibility() == View.INVISIBLE) {
            mProgressBar2.setVisibility(View.VISIBLE);
            detailList.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar2.setVisibility(View.INVISIBLE);
            detailList.setVisibility(View.VISIBLE);
        }
    }  // toggleProgressBar

    private void addDelete() {
        checkActivated = true;
        adapter.notifyDataSetChanged();
        deleteBtnView = (ViewGroup)findViewById(R.id.delete_mass_view);
        deleteBtnView.setVisibility(View.VISIBLE);

        // SET CANCEL BUTTON
        Button cancel = (Button)findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkActivated = false;
                deleteBtnView.setVisibility(View.INVISIBLE);
                adapter.notifyDataSetChanged();
            }
        });  // cancel.setOnClickListener

        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("ITR", "IS checked:");
                }
            }
        });  // check.setOnCheckedChangeListener

        // SET COPY BUTTON
        Button copyTo = (Button)findViewById(R.id.copy_to_btn);
        copyTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List newList = getTappedIds();  // get tapped ids

                //get current title list and convert to new arraylist
                ArrayList<String> listTitles = new ArrayList<String>();
                for (ParseObject titles : MyLists.myArrayTitles) {
                    String title = (titles.get("listTitle").toString());
                    listTitles.add(title);
                }

                final CharSequence[] titles = listTitles.toArray(new CharSequence[listTitles.size()]);

                AlertDialog.Builder a = new AlertDialog.Builder(DetailListView.this);
                a.setTitle(getString(R.string.dlv_dialog_copy));
                a.setIcon(R.drawable.ic_dialog);
                a.setItems(titles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            // pull list title
                            String titleTapped = titles[which].toString();

                            //query by list title
                            ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Lists");
                            query1.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                            query1.orderByAscending("listTitle");
                            query1.whereEqualTo("listTitle", titleTapped);
                            query1.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(final ParseObject object, ParseException e) {
//                                     Array id = new Array();
                                    if (object.get("myLists") == null) {
                                        object.put("myLists", newList);
                                        object.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e != null) {
                                                    Log.d("Parse:", e.getLocalizedMessage());
                                                }
                                                else {
                                                    AlertDialogFragment.grabUserList(ParseUser.getCurrentUser());
                                                    Toast.makeText(DetailListView.this, getString(R.string.dlv_toast_saved), LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        //set up returned list
                                        List myList = object.getList("myLists");
                                        for (int i = 0; i < myList.size(); i++) {
                                            newList.add(myList.get(i).toString());
                                        }

                                        object.put("myLists", newList);
                                        object.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e != null) {
                                                    Log.d("Parse:", e.getLocalizedMessage());
                                                }
                                                else {
                                                    AlertDialogFragment.grabUserList(ParseUser.getCurrentUser());
                                                    Toast.makeText(DetailListView.this, getString(R.string.dlv_toast_saved), LENGTH_LONG).show();
                                                    deleteBtnView.setVisibility(View.INVISIBLE);
                                                    checkActivated = false;
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        catch (Exception e) {
                            throw e;
                        }
                    }
                });
                AlertDialog copy = a.create();
                copy.show();
            }  // onClick
        });  // copyTo.setOnClickListener

        // SET MOVE BUTTON
        Button moveTo = (Button)findViewById(R.id.move_to_btn);
        moveTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List newList = getTappedIds();  //get tapped ids

                //get current title list and convert to new arraylist
                ArrayList<String> listTitles = new ArrayList<String>();
                for (ParseObject titles : MyLists.myArrayTitles) {
                    String title = (titles.get("listTitle").toString());
                    listTitles.add(title);
                }

                final CharSequence[] titles = listTitles.toArray(new CharSequence[listTitles.size()]);

                AlertDialog.Builder a = new AlertDialog.Builder(DetailListView.this);
                a.setTitle(getString(R.string.dlv_dialog_move));
                a.setIcon(R.drawable.ic_dialog);
                a.setItems(titles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            // pull list title
                            String titleTapped = titles[which].toString();

                            // get and remove objects from current list
                            Iterator<VideoItem> x = searchResults.iterator();
                            while (x.hasNext()) {
                                VideoItem y = x.next();
                                if (y.isSelected()) {
                                    x.remove();
                                }
                            }

                            final ArrayList<String> temp = new ArrayList<>();
                            for (VideoItem x1 : searchResults) {
                                temp.add(x1.getId());
                            }

                            final String listId = convertIntentToListId(args);
                            ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Lists");
                            query2.getInBackground(listId, new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {
                                    object.put("myLists", temp);
                                    try {
                                        object.save();
                                    }
                                    catch (ParseException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });

                            //query by list title
                            ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Lists");
                            query1.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                            query1.orderByAscending("listTitle");
                            query1.whereEqualTo("listTitle", titleTapped);
                            query1.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(final ParseObject object, ParseException e) {
//                                    Array id = new Array();
                                    if (object.get("myLists") == null) {
                                        object.put("myLists", newList);
                                        object.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e != null) {
                                                    Log.d("Parse:", e.getLocalizedMessage());
                                                }
                                                else {
                                                    AlertDialogFragment.grabUserList(ParseUser.getCurrentUser());
                                                    Toast.makeText(DetailListView.this, getString(R.string.dlv_toast_saved), LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        // set up returned list
                                        List myList = object.getList("myLists");
                                        for (int i = 0; i < myList.size(); i++) {
                                            newList.add(myList.get(i).toString());
                                        }

                                        object.put("myLists", newList);
                                        object.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e != null) {
                                                    Log.d("Parse:", e.getLocalizedMessage());
                                                }
                                                else {
                                                    AlertDialogFragment.grabUserList(ParseUser.getCurrentUser());
                                                    Toast.makeText(DetailListView.this, getString(R.string.dlv_toast_saved), LENGTH_LONG).show();
                                                    deleteBtnView.setVisibility(View.INVISIBLE);
                                                    checkActivated = false;
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        catch (Exception e) {
                            throw e;
                        }
                    }
                });
                AlertDialog copy = a.create();
                copy.show();
            }
        });

        // SET DELETE BUTTON
        Button delete = (Button)findViewById(R.id.delete_mass_btn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder a = new AlertDialog.Builder(DetailListView.this);
                a.setTitle(getString(R.string.builder_mdel_confirm));
                a.setIcon(R.drawable.ic_dialog);
                a.setNegativeButton(getString(R.string.btn_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ArrayList<String> temp = new ArrayList<>();
                        final ArrayList<VideoItem> newList = new ArrayList<VideoItem>();
                        //add unchecked to temp array
                        for (VideoItem x : searchResults) {
                            if (x.isSelected() == false) {
                                temp.add(x.getId());
                                newList.add(x);
                            }
                        }
                        //update current view
                        searchResults = newList;
                        toggleProgressBar();

                        //query and get cur object
                        String listId = convertIntentToListId(args);
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
                        query.whereEqualTo("objectId", listId);
                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                if (e != null) {
                                    Log.d("Parse:", e.getLocalizedMessage());
                                }
                                else {
                                    object.put("myLists", temp);
                                    object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            checkActivated = false;
                                            updateVideosFound();
                                            toggleProgressBar();
                                        }
                                    });  // object.saveInBackground
                                }
                            }  // done
                        });  // query.getFirstInBackground
                        deleteBtnView.setVisibility(View.INVISIBLE);
                    }
                });
                AlertDialog del = a.create();
                del.show();
            }  // onClick
        });  //  delete.setOnClickListener
    }  // addDelete

    private List getTappedIds () {
        final ArrayList<String> temp = new ArrayList<>();
        final ArrayList<String> newList = new ArrayList<String>();
        //add unchecked to temp array
        for (VideoItem x : searchResults) {
            if (x.isSelected()) {
                newList.add(x.getId());
            }
        }
        return newList;
    }  // getTappedIds

}  // DetailListView
