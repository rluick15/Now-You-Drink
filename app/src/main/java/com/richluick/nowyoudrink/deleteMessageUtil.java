package com.richluick.nowyoudrink;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rich on 10/1/2014.
 */
public class deleteMessageUtil {
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
}
