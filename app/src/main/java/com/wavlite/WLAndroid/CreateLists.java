package com.wavlite.WLAndroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.wavlite.WLAndroid.ui.MyLists;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by javen on 8/28/15.
 */
public class CreateLists {

    public void addNewItemToList(final Activity context) {
        View v = context.getLayoutInflater().inflate(R.layout.alert_first_list_title, null);
        final AlertDialog.Builder newTitle = new AlertDialog.Builder(context);
        newTitle.setView(v);

        // Edit the searchlist title name
        TextView newListAdd = (TextView)v.findViewById(R.id.alert_edit_title_text);
        newListAdd.setText(context.getString(R.string.ml_dialog_name));

        final EditText newListTitleAdd = (EditText)v.findViewById(R.id.first_title);
        newListTitleAdd.setHint(context.getString(R.string.ml_dialog_hint_name));

        newTitle.setNegativeButton(context.getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });  // newTitle.setNegativeButton

        newTitle.setPositiveButton(context.getString(R.string.btn_save), new DialogInterface.OnClickListener() {
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
                            // empty if body?
                        } else {
                            final AlertDialog.Builder success = new AlertDialog.Builder(context);
                            success.setTitle(context.getString(R.string.ml_dialog_save));
                            success.setMessage(context.getString(R.string.ml_dialog_msg_save));
                            success.setPositiveButton(context.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MyLists ml = new MyLists();
                                    ml.updatedListTitles();
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
}
