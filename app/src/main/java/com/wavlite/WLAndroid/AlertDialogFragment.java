package com.wavlite.WLAndroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wavlite.WLAndroid.ui.MyLists;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by javen on 7/7/15.
 * Edits by Jay Bardeleben
 */
public class AlertDialogFragment extends DialogFragment {

    // Button text strings for static builder methods
    public static String btn_add = "ADD SEARCHLIST";
    public static String btn_cancel = "Cancel";  // Unused. Is set for possible future usage
    public static String btn_create = "CREATE NEW SEARCHLIST";
    public static String btn_gotIt = "GOT IT!";
    public static String btn_ok = "OK";

    public static String toastSaved = "Video Saved";

    // Dialog title and message strings for static builder methods
    public static String errTitle = "Oops!";  // change to 'Something went wrong'? May be more clear
    public static String alertDataConnTitle = "Connection Problem!";
    public static String alertDataConnMsg = "You must have an internet connection to view this content";
    public static String selectTitle = "Select Searchlist";
    public static String successTitle = "Success!";
    public static String successMsg = "Your new searchlist has been created with this "
            + "video added. You can now search Wavlite and add more to this searchlist or go to "
            + "\"My Searchlists\" and enjoy!\n" + "";


    public static void adjustListItems(final int pos, final List<ParseObject> list, final Activity activity, final String videoId, final Context context) {
        // get current title list and convert to new arraylist
        ArrayList<String> listTitles = new ArrayList<String>();
        for (ParseObject titles : list) {
            String title = (titles.get("listTitle").toString());
            listTitles.add(title);
        }

        final CharSequence[] titles = listTitles.toArray(new CharSequence[listTitles.size()]);

        // Set text/color manipulation here?
        final AlertDialog.Builder success = new AlertDialog.Builder(activity);
        success.setIcon(R.drawable.ic_dialog);
        success.setTitle(selectTitle);
        success.setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                try {
                    String titleTapped = titles[which].toString();  // pull list title

                    // convert id to Parse array type
                    final ArrayList<String> moreToAdd;
                    moreToAdd = new ArrayList<String>();
                    moreToAdd.add(videoId);

                    // query by list title
                    ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Lists");
                    query1.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                    query1.orderByAscending("listTitle");
                    query1.whereEqualTo("listTitle", titleTapped);
                    query1.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(final ParseObject object, ParseException e) {
//                            Array id = new Array();
                            if (object.get("myLists") == null) {
                                object.put("myLists", moreToAdd);
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.d("Parse:", e.getLocalizedMessage());
                                        }
                                        else {
                                            grabUserList(ParseUser.getCurrentUser());
                                            Toast.makeText(context, toastSaved, LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            else {
                                ArrayList<String> addMore = new ArrayList<String>();
                                JSONArray myList = object.getJSONArray("myLists");

                                // if the list is empty
                                if (myList == null) {
                                    addMore.add(videoId);
                                }
                                else {
                                    for (int i = 0; i < myList.length(); i++) {
                                        try {
                                            addMore.add(myList.get(i).toString());
                                        }
                                        catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }
                                addMore.add(videoId);
                                object.put("myLists", addMore);
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.d("Parse:", e.getLocalizedMessage());
                                        }
                                        else {
                                            grabUserList(ParseUser.getCurrentUser());
                                            Toast.makeText(context, toastSaved, LENGTH_LONG).show();
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
        success.setNeutralButton(btn_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listHelper(activity, videoId, successTitle, successMsg);
            }
        });
        AlertDialog alert = success.create();
        alert.show();
    }  // adjustListItems


    public static void addItemAndList(final Activity activity, final String vidId, final String title, final String message) {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
        query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
        query.orderByAscending("listTitle");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null) {
                    Log.d("Error with list pull: ", e.getLocalizedMessage());
                } else {
                    listHelper(activity, vidId, title, message);
                }
            }
        });
    }  // addItemAndList


    public static void grabUserList(ParseUser currentUser) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
        String curUser = currentUser.getObjectId();
        query.whereEqualTo("createdBy", curUser);
        query.orderByAscending("listTitle");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null) {
                    Log.d("Error with list pull: ", e.getLocalizedMessage());
                }
                else {
                    Log.d("User's saved list: ", list.toString());
                    MyLists.myArrayTitles.clear();
                    for (ParseObject object : list) {
                        ParseObject temp = object;
                        MyLists.myArrayTitles.add(object);
                    }
                }
            }
        });
    }  // grabUserList


    public static void listHelper(final Activity activity, final String id, final String title, final String message) {
        View v = activity.getLayoutInflater().inflate(R.layout.alert_first_list_title, null);
        final AlertDialog.Builder newTitle = new AlertDialog.Builder(activity);
        newTitle.setIcon(R.drawable.ic_dialog);
        newTitle.setView(v);
        final EditText userTitleView = (EditText) v.findViewById(R.id.first_title);
        final AlertDialog.Builder builder = newTitle.setPositiveButton(btn_add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get user added title from alert dialog
                String mTitle = userTitleView.getText().toString();

                //instantiate new array for videoId to load to Parse
                ArrayList<String> firstIdToAdd;
                firstIdToAdd = new ArrayList<String>();
                firstIdToAdd.add(id);

                // initiate Parse object to being adding
                ParseObject firstList = new ParseObject("Lists");
                firstList.put("listTitle", mTitle);
                firstList.put("myLists", firstIdToAdd);

                // create user relation
                ParseRelation<ParseObject> relation = firstList.getRelation("createdBy");
                relation.add(ParseUser.getCurrentUser());
                firstList.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            final AlertDialog.Builder success = new AlertDialog.Builder(activity);
                            success.setTitle(title);
                            success.setMessage(message);
                            success.setPositiveButton(btn_gotIt, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    grabUserList(ParseUser.getCurrentUser());
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alert = success.create();
                            alert.show();
                        }
                        else {
                            final AlertDialog.Builder success = new AlertDialog.Builder(activity);
                            success.setTitle(errTitle);
                            success.setMessage("" + e.getLocalizedMessage());
                            success.setPositiveButton(btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alert = success.create();
                            alert.show();
                        }
                    }
                });
            }
        });
        AlertDialog addTitle = newTitle.create();
        addTitle.show();
    }  // listHelper


    public static void dataConnection(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(alertDataConnTitle);
        builder.setMessage(alertDataConnMsg);
        builder.setNegativeButton(btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                activity.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }  // dataConnection

    public static void trialAlert(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Your trial is over");
        builder.setMessage("Please contact Wavlite to re-up");
        builder.setNegativeButton(btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                activity.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void problemWithYouTube(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Oops");
        builder.setMessage("There was a problem connecting to YouTube");
        builder.setNegativeButton(btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                activity.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void trialBeginAlert(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Welcome to Wavlite");
        builder.setMessage("Your current trial will last for 28 days. " +
                "Please contact us with any questions or comments as your feedback is greatly appreciated");
        builder.setNegativeButton(btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}  // AlertDialogFragment
