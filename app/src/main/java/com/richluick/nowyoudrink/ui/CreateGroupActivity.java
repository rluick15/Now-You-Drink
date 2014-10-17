package com.richluick.nowyoudrink.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.nowyoudrink.R;
import com.richluick.nowyoudrink.utils.ParseConstants;
import com.richluick.nowyoudrink.utils.Utilities;

import java.util.ArrayList;
import java.util.List;


public class CreateGroupActivity extends ListActivity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ArrayList<ParseUser> mPendingMembers;
    protected ParseUser mCurrentUser;
    protected EditText mGroupNameField;
    protected String mGroupName;
    protected String mGroupId;
    protected Button mCreateGroupButton;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseRelation<ParseUser> mPendingMemberRelation;
    protected ParseRelation<ParseUser> mMemberRelation;
    ParseRelation<ParseObject> mMemberOfGroupRelation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_create_group);

        Utilities.setContext(this); //set the utilities context to this

        mGroupNameField = (EditText) findViewById(R.id.groupTitleField);
        mCreateGroupButton = (Button) findViewById(R.id.createGroupButton);

        mCurrentUser = ParseUser.getCurrentUser();
        mPendingMembers = new ArrayList<ParseUser>();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        mMemberOfGroupRelation = mCurrentUser.getRelation(ParseConstants.KEY_MEMBER_OF_GROUP_RELATION);

        setProgressBarIndeterminateVisibility(true);

        //queries all the users friends in the listview
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
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
                            CreateGroupActivity.this,
                            android.R.layout.simple_list_item_checked,
                            usernames);
                    setListAdapter(adapter);
                }
                else { //error message dialog if the query fails
                    Utilities.getErrorAlertDialog();
                }
            }
        });

        //creates groups if fields are all filled out
        mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProgressBarIndeterminateVisibility(true);

                mGroupName = mGroupNameField.getText().toString();
                if (mGroupName.isEmpty()) {
                    setProgressBarIndeterminateVisibility(false);

                    //Checks if the user left the group name field blank and displays an alert message
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateGroupActivity.this);
                    builder.setTitle(getString(R.string.error_title))
                            .setMessage(getString(R.string.create_group_error_message))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    Utilities.customDialog (dialog);
                }
                else {
                    createGroupAndMessage();
                }
            }
        });

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utilities.setContext(null); //set context to null to prevent leak
    }

    private void createGroupAndMessage() {
        setProgressBarIndeterminateVisibility(false);

        //create the group and save it in background and add pending members
        //get the object ID in the callback and create the new group
        final ParseObject group = createGroup();
        group.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mGroupId = group.getObjectId();
                    mMemberOfGroupRelation.add(group);
                    mCurrentUser.saveInBackground();

                    ParseObject message = createMessage(group);
                    if (message == null) { //error
                        Utilities.getErrorAlertDialog();
                    }
                    else { //sends the message and goes to the new group
                        send(message);
                        Intent intent = new Intent(CreateGroupActivity.this, GroupActivity.class);
                        intent.putExtra(ParseConstants.KEY_GROUP_ID, mGroupId);
                        startActivity(intent);
                        finish();
                    }
                }
                else {
                    Utilities.getErrorAlertDialog();
                }
            }
        });
    }

    private ParseObject createGroup() {
        ParseObject group = new ParseObject(ParseConstants.CLASS_GROUPS);
        group.add(ParseConstants.KEY_GROUP_ADMIN, mCurrentUser.getUsername());
        group.add(ParseConstants.KEY_GROUP_NAME, mGroupName);
        group.add(ParseConstants.KEY_CURRENT_DRINKER, mCurrentUser.getUsername());
        group.add(ParseConstants.KEY_PREVIOUS_DRINKER, "");
        group.put(ParseConstants.KEY_MESSAGE_TYPE, ParseConstants.TYPE_GROUP);

        //Add Pending members to group and add current user as group memeber
        mMemberRelation = group.getRelation(ParseConstants.KEY_MEMBER_RELATION);
        mMemberRelation.add(mCurrentUser);
        mPendingMemberRelation = group.getRelation(ParseConstants.KEY_PENDING_MEMBER_RELATION);
        for (ParseUser member : mPendingMembers) {
            mPendingMemberRelation.add(member);
        }

        return group;
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
                if (e == null) {
                    //success
                    Toast.makeText(CreateGroupActivity.this, getString(R.string.success_message_group_create), Toast.LENGTH_LONG).show();
                    Utilities.sendPushNotifications(null, mPendingMembers,
                            ParseUser.getCurrentUser().getUsername()
                                    + " invited you to join the group " +  mGroupName + "!",
                            "mr");
                }
                else { //error sending message
                    Utilities.getErrorAlertDialog();
                }
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        //adds the selected user to list of selected users in the list
        if (l.isItemChecked(position)) mPendingMembers.add(mFriends.get(position));
        else mPendingMembers.remove(mFriends.get(position)); //remove the user from the list

        //only displays the button if at least one friend is selected
        if(l.getCheckedItemCount() > 0) mCreateGroupButton.setEnabled(true);
        else mCreateGroupButton.setEnabled(false);
    }
}
