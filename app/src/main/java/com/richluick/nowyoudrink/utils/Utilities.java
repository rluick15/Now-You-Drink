package com.richluick.nowyoudrink.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.richluick.nowyoudrink.R;

import java.util.ArrayList;
import java.util.List;

public class Utilities {

    private static Context context = null;

    public static void setContext(Context context)
    {
        Utilities.context = context;
    }

    //method to remove first and last characters of a string. used in other activities
    public static String removeCharacters(String string) {
        return string.substring(1, string.length()-1);
    }

    //utilities method to delete messages after being viewed
    public static void deleteMessage(ParseObject message) {
        //Delete the message
        List<ParseUser> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);
        if(ids.size() == 1) {
            //Last recipient. delete whole message
            message.deleteInBackground();
        }
        else { //remove just the recipient
            ids.remove(ParseUser.getCurrentUser());

            ArrayList<ParseUser> idsToRemove = new ArrayList<ParseUser>();
            idsToRemove.add(ParseUser.getCurrentUser());

            message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
            message.saveInBackground();
        }
    }

    //standard error alert dialog
    public static void getErrorAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error_title))
            .setMessage(context.getString(R.string.alert_standard_error_message))
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((Activity) context).finish();
                }
            });
        AlertDialog dialog = builder.create();
        dialog.show();
        customDialog(dialog);
    }

    public static void getNoGroupAlertDialog(final ParseObject message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.message_title_nonexistent_group))
                .setMessage(context.getString(R.string.message_nonexistent_group))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(message != null) {
                            deleteMessage(message);
                        }
                        ((Activity) context).finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        customDialog(dialog);
    }

    //set the colors for the custom dialogs
    public static void customDialog(AlertDialog dialog) {
        //custom divider color unless the context is not carried over
        if(context != null) {
            int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = dialog.findViewById(dividerId);
            divider.setBackgroundColor(context.getResources().getColor(R.color.main_color));

            //custom title color
            int textViewId = dialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
            TextView tv = (TextView) dialog.findViewById(textViewId);
            tv.setTextColor(context.getResources().getColor(R.color.main_color));
        }
    }

}
