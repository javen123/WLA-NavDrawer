package com.wavlite.WLAndroid.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wavlite.WLAndroid.ListTuple;
import com.wavlite.WLAndroid.R;
import com.wavlite.WLAndroid.VideoItem;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MyLists extends AppCompatActivity {
    private TextView noLists;
    private ArrayAdapter adapter;
    private ArrayList<VideoItem> xyz;
    private CheckBox checkBox;
    private Boolean checkActivated = false;
    private ViewGroup deleteBtnView;

    private static ListView myListView;
    private static ProgressBar progressBar;
    public static ArrayList<ParseObject> myArrayTitles = new ArrayList<ParseObject>();
    public static Boolean addToListFromDetail = false;  // return bool activated from detail list page

    // new array setup
    List<MyListItems> items;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        myListView = (ListView) findViewById(R.id.my_list_titles);
        noLists = (TextView) findViewById(R.id.no_list_text);


        progressBar = (ProgressBar)findViewById(R.id.progressBar3);
        progressBar.setVisibility(View.INVISIBLE);

        if (myArrayTitles.size() != 0) {
            noLists.setVisibility(View.INVISIBLE);
            // NEW ARRAY set up
//            convertParseArrayToClassItems(myArrayTitles);
//            newLoadListItems();

            //old array setup
            loadListNames();
        }
        else {
            updatedListTitles();
        }

        addRowClickListener();
    }  // onCreate



    @Override
    protected void onResume() {
        super.onResume();
        if (addToListFromDetail) {
            addNewItemToList();
            addToListFromDetail = false;
        }
        // This should probably be removed (void method)
        else {
            return;
        }
    }  // onResume


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.ml_action_logout) {
            myArrayTitles.clear();
            ParseUser.logOut();
            navigateToLogin();
        }
        if (id == R.id.ml_menu_search) {
            ActivityCompat.startActivityForResult(this, new Intent(this,SearchViewActivity.class),0,null);
            finish();
            return true;
        }
        if (id == R.id.ml_menu_create_list) {
//            addNewItemToList();
            addNewItemToList();
        }
//        if(id == R.id.ml_edit_my_list){
//            massDelete();
//        }
        return super.onOptionsItemSelected(item);
    }  // onOptionsItemSelected

    private void massDelete() {
        checkActivated = true;
        adapter.notifyDataSetChanged();
        deleteBtnView = (ViewGroup)findViewById(R.id.delete_myLists_view);
        deleteBtnView.setVisibility(View.VISIBLE);

        Button cancel = (Button)findViewById(R.id.mylist_massCancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkActivated = false;
                deleteBtnView.setVisibility(View.INVISIBLE);
                adapter.notifyDataSetChanged();
            }
        });  // cancel.setOnClickListener
    }  // massDelete


    private void navigateToLogin() {
        myArrayTitles.clear();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
        this.finish();
    }  // navigateToLogin


    private void loadListNames() {
        ArrayList<String> values = new ArrayList<String>();
        for (ParseObject object : myArrayTitles) {
            String title = (object.get("listTitle").toString());
            values.add(title);
        }

        adapter = new ArrayAdapter(MyLists.this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        noLists.setVisibility(View.INVISIBLE);
        myListView.setVisibility(View.VISIBLE);
        myListView.setAdapter(adapter);
    }  // loadListNames


    private void addRowClickListener() {
        if (myListView != null) {
            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                    ArrayList<ListTuple> temp = new ArrayList<ListTuple>();
                    String title = myArrayTitles.get(pos).get("listTitle").toString();
                    String vid = myArrayTitles.get(pos).getObjectId();
                    JSONArray myList = myArrayTitles.get(pos).getJSONArray("myLists");
                    ArrayList xList = (ArrayList) myArrayTitles.get(pos).getList("myLists");
                    ListTuple i;
                    i = new ListTuple(vid, xList);
                    temp.add(i);

                    if (xList != null) {
                        Intent intent = new Intent(MyLists.this, DetailListView.class);
                        Bundle args = new Bundle();
                        args.putSerializable("ArrayList", temp);
                        intent.putExtra("myListids", args);
                        intent.putExtra("title", title);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_empty_list), Toast.LENGTH_LONG).show();
                    }
                }  // onItemClick
            });  // myListView.setOnItemClickListener
        }  // if-myListView

        // Longpress event listener options
        myListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // create alert to give edit title options with long press
                AlertDialog.Builder builder = new AlertDialog.Builder(MyLists.this);
                // Get the searchlist title from the array
                builder.setTitle(getString(R.string.builder_title));
                builder.setMessage("" + myArrayTitles.get(position).get("listTitle").toString() + "\n ");

                // DELETE SEARCHLIST
                builder.setNeutralButton(getString(R.string.btn_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder delete = new AlertDialog.Builder(MyLists.this);
                        delete.setTitle(getString(R.string.builder_del_sl_confirm));
                        delete.setNegativeButton(getString(R.string.btn_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ParseObject deletedList = myArrayTitles.get(position);
                                deletedList.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.d("Parse Error: ", e.getLocalizedMessage());
                                        }
                                        else {
                                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
                                            query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
                                            query.addAscendingOrder("listTitle");
                                            query.findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> list, ParseException e) {
                                                    if (e != null) {
                                                        Log.d("Error with list pull: ", e.getLocalizedMessage());
                                                    }
                                                    else {
                                                        // clear current list and update with new material
                                                        myArrayTitles.clear();
                                                        for (ParseObject object : list) {
                                                            MyLists.myArrayTitles.add(object);
                                                        }

                                                        if (myArrayTitles.size() == 0) {
                                                            noLists.setVisibility(View.VISIBLE);
                                                            myListView.setVisibility(View.INVISIBLE);
                                                        }
                                                        else {
                                                            loadListNames();
                                                        }
                                                    }  // main if-else
                                                }  // done
                                            });  // query.findInBackground
                                        }  // main if-else
                                    }  // done
                                });  // deletedList.deleteInBackground
                            }
                        });
                        delete.setNeutralButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog a = delete.create();
                        a.show();
                    }  // onClick
                });  // builder.setNeutralButton


                builder.setPositiveButton(getString(R.string.btn_rename), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // pull reusable alert from layouts
                        View v = getLayoutInflater().inflate(R.layout.alert_first_list_title, null);
                        final AlertDialog.Builder editTitle = new AlertDialog.Builder(MyLists.this);

                        editTitle.setView(v);
                        TextView editAlertTitle = (TextView) v.findViewById(R.id.alert_edit_title_text);
                        final EditText newTitle = (EditText) v.findViewById(R.id.first_title);
                        editAlertTitle.setText(getString(R.string.ml_dialog_rename) + "\n ");

                        editTitle.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });  // editTitle.setNegativeButton

                        editTitle.setPositiveButton(getString(R.string.btn_save), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // grab new title name from user input
                                String mNewTitle = newTitle.getText().toString();

                                //grab parse object to update
                                ParseObject object = myArrayTitles.get(position);
                                object.put("listTitle", mNewTitle);
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.d("Parse: ", e.getLocalizedMessage());
                                        }
                                        else {
                                            // immediately grab new lists for new Listview
                                            myArrayTitles.clear();
                                            updatedListTitles();

                                            //return alert dialog box of success
                                            final AlertDialog.Builder success = new AlertDialog.Builder(MyLists.this);
                                            success.setTitle(getString(R.string.ml_dialog_updated));
                                            success.setMessage(getString((R.string.ml_dialog_msg_updated)));
                                            success.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });  // success.setPositiveButton
                                            AlertDialog alert = success.create();
                                            alert.show();
                                        }
                                    }  // done
                                });  // object.saveInBackground
                            }  // onClick
                        });  // editTitle.setPositiveButton
                        AlertDialog editTapped = editTitle.create();
                        editTapped.show();
                    }  // onClick
                });  // builder.setPositiveButton

                builder.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });  // myListView.setOnItemLongClickListener
    }  // addRowClickListener


//    private void addNewItemToList() {
//        View v = getLayoutInflater().inflate(R.layout.alert_first_list_title, null);
//        final AlertDialog.Builder newTitle = new AlertDialog.Builder(MyLists.this);
//        newTitle.setView(v);
//
//        // Edit the searchlist title name
//        TextView newListAdd = (TextView)v.findViewById(R.id.alert_edit_title_text);
//        newListAdd.setText(getString(R.string.ml_dialog_name));
//        final EditText newListTitleAdd = (EditText)v.findViewById(R.id.first_title);
//        newListTitleAdd.setHint(getString(R.string.ml_dialog_hint_name));
//
//        newTitle.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });  // newTitle.setNegativeButton
//
//        newTitle.setPositiveButton(getString(R.string.button_save), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                final String mTitle = newListTitleAdd.getText().toString();
//                ParseObject newListTitle = new ParseObject("Lists");
//                newListTitle.put("listTitle", mTitle);
//                ParseRelation<ParseObject> relation = newListTitle.getRelation("createdBy");
//                relation.add(ParseUser.getCurrentUser());
//
//                newListTitle.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        if (e != null) {
//                            // empty if body?
//                        }
//                        else {
//                            final AlertDialog.Builder success = new AlertDialog.Builder(MyLists.this);
//                            success.setTitle(getString(R.string.ml_dialog_save));
//                            success.setMessage(getString(R.string.ml_dialog_msg_save));
//                            success.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    updatedListTitles();
//                                }
//                            });  // success.setPositiveButton
//                            AlertDialog alert = success.create();
//                            alert.show();
//                        }
//                    }  // done
//                });  // newListTitle.saveInBackground
//            }  // onClick
//        });  // newTitle.setPositiveButton
//        AlertDialog alert = newTitle.create();
//        alert.show();
//    }  // addNewItemToList


    public void addNewItemToList() {
        View v = getLayoutInflater().inflate(R.layout.alert_first_list_title, null);
        final AlertDialog.Builder newTitle = new AlertDialog.Builder(this);
        newTitle.setView(v);

        // Edit the searchlist title name
        TextView newListAdd = (TextView)v.findViewById(R.id.alert_edit_title_text);
        newListAdd.setText(getString(R.string.ml_dialog_name));

        final EditText newListTitleAdd = (EditText)v.findViewById(R.id.first_title);
        newListTitleAdd.setHint(getString(R.string.ml_dialog_hint_name));

        newTitle.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });  // newTitle.setNegativeButton

        newTitle.setPositiveButton(getString(R.string.btn_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String mTitle = newListTitleAdd.getText().toString();
                ParseObject newListTitle = new ParseObject("Lists");
                newListTitle.put("listTitle", mTitle);
                ParseRelation<ParseObject> relation = newListTitle.getRelation("createdBy");
                relation.add(ParseUser.getCurrentUser());

                newListTitle.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            // This should be logged so we have evidence of it
                        }
                        else {
                            final AlertDialog.Builder success = new AlertDialog.Builder(MyLists.this);
                            success.setTitle(getString(R.string.ml_dialog_save));
                            success.setMessage(getString(R.string.ml_dialog_msg_save));
                            success.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updatedListTitles();
                                }
                            });  // success.setPositiveButton
                            AlertDialog alert = success.create();
                            alert.show();
                        }
                    }  // done
                });  // newListTitle.saveInBackground
            }  // onClick
        });  // newTitle.setPositiveButton
        AlertDialog alert = newTitle.create();
        alert.show();
    }  // addNewItemToList


    public void updatedListTitles() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Lists");
        query.whereEqualTo("createdBy", ParseUser.getCurrentUser());
        query.orderByAscending("listTitle");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null) {
                    Log.d("Error with list pull: ", e.getLocalizedMessage());
                }
                else {
                    // clear current list and update with new material
                    myArrayTitles.clear();
                    for (ParseObject object : list) {
                        MyLists.myArrayTitles.add(object);
                    }
                    if (myArrayTitles.isEmpty()) {
                        noLists.setVisibility(View.VISIBLE);
                        myListView.setVisibility(View.INVISIBLE);
                    }
                    else {
                        loadListNames();
                    }
                }
            }  // done
        });  // query.findInBackground
    }  // updatedListTitles


    private static void toggleProgressBar() {
        if (progressBar.getVisibility() == View.INVISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
            myListView.setVisibility(View.INVISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            myListView.setVisibility(View.VISIBLE);
        }
    }  // toggleProgressBar


    // NEW ARRAY Helper
    private void convertParseArrayToClassItems(ArrayList<ParseObject> objects){
        ArrayList<MyListItems> itemps = new ArrayList<>();
        for(ParseObject x : objects){
            MyListItems temp = new MyListItems(x.getParseObject("Lists"), false);
            itemps.add(temp);
        }
        items = itemps;
        itemps.clear();
    }  // convertParseArrayToClassItems


    private void newLoadListItems() {
        adapter = new ArrayAdapter<MyListItems>(MyLists.this, R.layout.my_list_item, items) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.my_list_item, parent, false);

                TextView title = (TextView) convertView.findViewById(R.id.list_title);
                MyListItems x = items.get(position);
                title.setText(x.getObject().get("listTitle").toString());

                // activate checkbox
                checkBox = (CheckBox) convertView.findViewById(R.id.checkBox2);

                // Toggle visibility
                if (checkActivated) {
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                items.get(position).setSelected(true);
                            }
                            if (!isChecked) {
                                items.get(position).setSelected(false);
                            }
                        }  // if (checkActivated)
                    });
                }
                else {
                    checkBox.setVisibility(View.INVISIBLE);
                }
                return convertView;
            }
        };
        myListView.setAdapter(adapter);
    }  // newLoadListItems


    // Private internal class
    private class MyListItems {
        ParseObject object;
        Boolean selected;

        public ParseObject getObject() {
            return object;
        }

        public void setObject(ParseObject object) {
            this.object = object;
        }

        public Boolean getSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }

        public MyListItems(ParseObject object, Boolean selected) {
            this.object = object;
            this.selected = selected;
        }  // (constructor)
    }  // MyListItems (class)

}  // MyLists
