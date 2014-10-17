package com.richluick.nowyoudrink.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.richluick.nowyoudrink.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rich on 10/1/2014.
 */
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

    //set the colors for the custom dialogs
    public static void customDialog(AlertDialog dialog) {
        //custom divider color
        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = dialog.findViewById(dividerId);
        divider.setBackgroundColor(context.getResources().getColor(R.color.main_color));

        //custom title color
        int textViewId = dialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        TextView tv = (TextView) dialog.findViewById(textViewId);
        tv.setTextColor(context.getResources().getColor(R.color.main_color));
    }

}
