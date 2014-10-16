package com.richluick.nowyoudrink.ui;

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
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.nowyoudrink.utils.ParseConstants;
import com.richluick.nowyoudrink.R;

import java.util.ArrayList;
import java.util.List;


public class EditMembersActivity extends ListActivity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected String mGroupId;
    protected String mGroupName;
    protected MenuItem mSendMenuItem;
    protected List<ParseUser> mFriends;
    protected ArrayList<ParseUser> mPendingMembers;
    protected ParseUser mCurrentUser;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseRelation<ParseUser> mPendingMemberRelation;
    protected ParseRelation<ParseUser> mMemberRelation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_members);

        mGroupId = getIntent().getStringExtra(ParseConstants.KEY_GROUP_ID);
        mGroupName = getIntent().getStringExtra(ParseConstants.KEY_GROUP_NAME);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mCurrentUser = ParseUser.getCurrentUser();
        mPendingMembers = new ArrayList<ParseUser>();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        //this query gets the group object to exclude members from the list
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_GROUPS);
        query.getInBackground(mGroupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject group, ParseException e) {
                mMemberRelation = group.getRelation(ParseConstants.KEY_MEMBER_RELATION);
                mPendingMemberRelation = group.getRelation(ParseConstants.KEY_PENDING_MEMBER_RELATION);

                //query current users friends to populate list
                listQuery();
            }
        });
    }

    protected void listQuery() {
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.whereDoesNotMatchKeyInQuery(ParseConstants.KEY_USERNAME,
                ParseConstants.KEY_USERNAME, mMemberRelation.getQuery());
        query.whereDoesNotMatchKeyInQuery(ParseConstants.KEY_EMAIL,
                ParseConstants.KEY_EMAIL, mPendingMemberRelation.getQuery());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if(e == null) {
                    mFriends = parseUsers;
                    String[] usernames = new String[mFriends.size()];
                    int i = 0;
                    for(ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    //Setup List View Adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            EditMembersActivity.this,
                            android.R.layout.simple_list_item_checked,
                            usernames);
                    setListAdapter(adapter);
                }
                else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditMembersActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    customDialog(dialog);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_members, menu);
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
            setProgressBarIndeterminateVisibility(true);
            return createMemberRequest();
        }
        return super.onOptionsItemSelected(item);
    }

    //create the group member request message and send to selected recipients
    private boolean createMemberRequest() {
        setProgressBarIndeterminateVisibility(false);

        //query the group to add relation and intent extras to request
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_GROUPS);
        query.getInBackground(mGroupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject group, ParseException e) {
                //add selectees to mending member relation
                mPendingMemberRelation = group.getRelation(ParseConstants.KEY_PENDING_MEMBER_RELATION);
                for (ParseUser member : mPendingMembers) {
                    mPendingMemberRelation.add(member);
                }
                group.saveInBackground();

                ParseObject message = createMessage(group);
                if(message == null) { //error
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditMembersActivity.this);
                    builder.setMessage(getString(R.string.error_friend_request))
                            .setTitle(getString(R.string.error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    customDialog(dialog);
                }
                else { //sends the message and goes to the new group
                    send(message);
                    finish();
                }
            }
        });

        return true;
    }

    private ParseObject createMessage(ParseObject group) {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER, ParseUser.getCurrentUser());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, mPendingMembers);
        message.put(ParseConstants.KEY_MESSAGE_TYPE, ParseConstants.TYPE_GROUP_REQUEST);
        message.put(ParseConstants.KEY_GROUP, group);
        message.put(ParseConstants.KEY_GROUP_ID, mGroupId);

        return message;
    }

    private void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    //success
                    Toast.makeText(EditMembersActivity.this, getString(R.string.success_group_request), Toast.LENGTH_LONG).show();
                    sendPushNotifications();
                }
                else { //error sending message
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    //send push notification to selected pending group members
    protected void sendPushNotifications() {
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER, mPendingMembers);

        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(ParseUser.getCurrentUser().getUsername() + " invited you to join the group " +  mGroupName + "!");
        push.sendInBackground();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        //adds the selected user to list of selected users in the list
        if (l.isItemChecked(position)) mPendingMembers.add(mFriends.get(position));
        else mPendingMembers.remove(mFriends.get(position)); //remove the user from the list

        if(l.getCheckedItemCount() > 0) mSendMenuItem.setVisible(true);
        else mSendMenuItem.setVisible(false);
    }

    //set the colors for the custom dialogs
    protected void customDialog(AlertDialog dialog) {
        //custom divider color
        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = dialog.findViewById(dividerId);
        divider.setBackgroundColor(getResources().getColor(R.color.main_color));

        //custom title color
        int textViewId = dialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        TextView tv = (TextView) dialog.findViewById(textViewId);
        tv.setTextColor(getResources().getColor(R.color.main_color));
    }
}
