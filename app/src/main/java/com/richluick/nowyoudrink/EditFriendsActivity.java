package com.richluick.nowyoudrink;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class EditFriendsActivity extends ListActivity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected List<ParseUser> mUsers;
    protected ArrayList<ParseUser> mPendingFriends;
    protected ParseRelation<ParseUser> mPendingRelation;
    protected ParseUser mCurrentUser;
    protected MenuItem mSendMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_friends);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mPendingFriends = new ArrayList<ParseUser>();
        mPendingRelation = mCurrentUser.getRelation(ParseConstants.KEY_PENDING_RELATION);

        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.whereNotEqualTo(ParseConstants.KEY_USERNAME, mCurrentUser.getUsername());
        //query.whereNotEqualTo(ParseConstants.KEY_PENDING_RELATION, "cgOJUDi5C0");
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if(e == null) {
                    mUsers = parseUsers;
                    String[] usernames = new String[mUsers.size()];
                    int i = 0;
                    for(ParseUser user : mUsers) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    //Setup List View Adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            EditFriendsActivity.this,
                            android.R.layout.simple_list_item_checked,
                            usernames);
                    setListAdapter(adapter);
                }
                else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_friends, menu);
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_send) {
            return sendFriendRequest();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean sendFriendRequest() {
        //Cycles through list of selected friends and adds as "Pending"
        for (ParseUser mPendingFriend : mPendingFriends) { //Cycles through list
            mPendingRelation.add(mPendingFriend);
        }

        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        ParseObject message = createMessage();
        if(message == null) { //error
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.error_friend_request))
                    .setTitle(getString(R.string.error_title))
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else { //sends the message and closes the activity
            send(message);
            finish();
        }
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        //adds the selected user to list of selected users in the list
        if (l.isItemChecked(position)) mPendingFriends.add(mUsers.get(position));
        else mPendingFriends.remove(mUsers.get(position)); //remove the user from the list

        if(l.getCheckedItemCount() > 0) mSendMenuItem.setVisible(true);
        else mSendMenuItem.setVisible(false);
    }

    //Friend Request Message is create with relevant information
    protected ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER, ParseUser.getCurrentUser());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_GROUP_ID, "");
        message.put(ParseConstants.KEY_RECIPIENT_IDS, mPendingFriends);
        message.put(ParseConstants.KEY_MESSAGE_TYPE, ParseConstants.TYPE_FRIEND_REQUEST);

        return message;
    }

    //message is sent to recipients
    protected void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    //success
                    Toast.makeText(EditFriendsActivity.this, getString(R.string.success_message_friend_request), Toast.LENGTH_LONG).show();
                    //sendPushNotifications();
                }
                else { //error sending message
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

//    protected void sendPushNotifications() {
//        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
//        query.whereContainedIn(ParseConstants.KEY_RECIPIENT_IDS, (java.util.Collection<?>) mPendingRelation);
//
//        //send push notification
//        ParsePush push = new ParsePush();
//        push.setQuery(query);
//        push.setMessage(getString(R.string.push_message, ParseUser.getCurrentUser().getUsername()));
//        push.sendInBackground();
//    }

}
