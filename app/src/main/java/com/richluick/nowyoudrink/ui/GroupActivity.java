package com.richluick.nowyoudrink.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
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


public class GroupActivity extends ListActivity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected String mGroupId;
    protected ParseObject mGroup;
    protected String mGroupName;
    protected String mCurrentDrinker;
    protected String mPreviousDrinker;
    protected TextView mCurrentDrinkerView;
    protected TextView mPreviousDrinkerView;
    protected Button mDrinkButton;
    protected ParseRelation<ParseUser> mMemberRelation;
    protected ParseRelation<ParseObject> mMemberOfGroupRelation;
    protected ParseUser mCurrentUser;
    protected String mGroupAdmin;
    protected List<ParseUser> mMembers;
    protected ParseUser mNextDrinker;
    protected MenuItem mRefreshMenuItem;
    protected MenuItem mDeleteMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_group);

        Utilities.setContext(this); //set the utilities context to this

        mCurrentDrinkerView = (TextView) findViewById(R.id.currentDrinkerUser);
        mPreviousDrinkerView = (TextView) findViewById(R.id.previousDrinkerUser);
        mDrinkButton = (Button) findViewById(R.id.drinkButton);

        mCurrentUser = ParseUser.getCurrentUser();
        mGroupId = getIntent().getStringExtra(ParseConstants.KEY_GROUP_ID);

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE); //only one user can be selected
    }

    @Override
    protected void onResume() {
        super.onResume();

        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_GROUPS);
        query.getInBackground(mGroupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject group, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if(e == null) {
                    mGroup = group;
                    mMemberRelation = mGroup.getRelation(ParseConstants.KEY_MEMBER_RELATION);
                    mMemberOfGroupRelation = mCurrentUser.getRelation(ParseConstants.KEY_MEMBER_OF_GROUP_RELATION);

                    //only the admin can delete the group
                    mGroupAdmin = mGroup.get(ParseConstants.KEY_GROUP_ADMIN).toString();
                    mGroupAdmin = Utilities.removeCharacters(mGroupAdmin);
                    if ((mCurrentUser.getUsername()).equals(mGroupAdmin)) {
                        mDeleteMenuItem.setVisible(true);
                    }

                    mGroupName = group.get(ParseConstants.KEY_GROUP_NAME).toString();
                    mGroupName = Utilities.removeCharacters(mGroupName);
                    setTitle(mGroupName);

                    mCurrentDrinker = mGroup.get(ParseConstants.KEY_CURRENT_DRINKER).toString();
                    mCurrentDrinker = Utilities.removeCharacters(mCurrentDrinker);
                    mCurrentDrinkerView.setText(mCurrentDrinker);

                    mPreviousDrinker = mGroup.get(ParseConstants.KEY_PREVIOUS_DRINKER).toString();
                    mPreviousDrinker = Utilities.removeCharacters(mPreviousDrinker);
                    mPreviousDrinkerView.setText(mPreviousDrinker);

                    listViewQuery(mMemberRelation);

                    //activate the button for the current drinker and let them know they are up
                    if ((mCurrentUser.getUsername()).equals(mCurrentDrinker)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                        builder.setTitle(getString(R.string.now_you_drink_title))
                                .setMessage(getString(R.string.now_you_drink_message))
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        Utilities.customDialog(dialog);
                    }
                }
                else {
                    Utilities.getNoGroupAlertDialog(null);
                }
            }
        });

        mDrinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mNextDrinker.getUsername().equals(mCurrentUser.getUsername())
                        || (mNextDrinker.getUsername()).equals(mPreviousDrinker)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                    builder.setTitle(getString(R.string.group_bad_selection_title))
                            .setMessage(getString(R.string.group_bad_selection_message))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    Utilities.customDialog(dialog);
                }
                else {
                    mGroup.remove(ParseConstants.KEY_CURRENT_DRINKER);
                    mGroup.remove(ParseConstants.KEY_PREVIOUS_DRINKER);
                    mGroup.add(ParseConstants.KEY_PREVIOUS_DRINKER, mCurrentDrinker);
                    mGroup.add(ParseConstants.KEY_CURRENT_DRINKER, mNextDrinker.getUsername());
                    mGroup.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null) {
                                Utilities.getErrorAlertDialog();
                            }
                        }
                    });

                    //create and send the Now you drink message
                    ParseObject message = createMessage();
                    if(message == null) { //error
                        Utilities.getErrorAlertDialog();
                    }
                    else { //sends the message and closes the activity
                        send(message);
                        finish();
                        startActivity(getIntent());
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utilities.setContext(null); //set context to null to prevent leak
    }

    //queries the users in the group and formats them for the list view
    protected void listViewQuery(ParseRelation<ParseUser> mMemberRelation) {
        ParseQuery<ParseUser> query = mMemberRelation.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if(e == null) {
                    mMembers = users;
                    String[] usernames = new String[mMembers.size()];
                    int i = 0;
                    for(ParseUser user : mMembers) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    //Setup List View Adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            GroupActivity.this,
                            android.R.layout.simple_list_item_1,
                            usernames);
                    setListAdapter(adapter);
                }
                else { //error message dialog if the query fails
                    Utilities.getErrorAlertDialog();
                }
            }
        });
    }

    //Friend Request Message is create with relevant information
    protected ParseObject createMessage() {
        //format single variables appropriatly. most cases the field is an array
        ArrayList<ParseUser> nextDrinker = new ArrayList<ParseUser>();
        nextDrinker.add(mNextDrinker);
        ArrayList<String> groupName = new ArrayList<String>();
        groupName.add(mGroupName);

        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER, ParseUser.getCurrentUser());
        message.put(ParseConstants.KEY_GROUP_ID, mGroupId);
        message.put(ParseConstants.KEY_GROUP_NAME, groupName);
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, nextDrinker);
        message.put(ParseConstants.KEY_MESSAGE_TYPE, ParseConstants.TYPE_DRINK_REQUEST);

        return message;
    }

    //message is sent to recipients
    protected void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    //success
                    Toast.makeText(GroupActivity.this, getString(R.string.message_success_drink_request), Toast.LENGTH_LONG).show();
                    Utilities.sendPushNotifications(mNextDrinker, null,
                            getString(R.string.push_drink_request_message), "sr");
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
        if (l.isItemChecked(position)) mNextDrinker = mMembers.get(position);

        //only displays the button if at least one friend is selected
        if(l.getCheckedItemCount() > 0 && (mCurrentUser.getUsername()).equals(mCurrentDrinker)) {
            mDrinkButton.setEnabled(true);
        }
        else mDrinkButton.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group, menu);
        mRefreshMenuItem = menu.getItem(0);
        mDeleteMenuItem = menu.getItem(3);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_refresh) {
            finish();
            startActivity(getIntent());
        }
        else if(id == R.id.action_edit_members) {
            Intent intent = new Intent(this, EditMembersActivity.class);
            intent.putExtra(ParseConstants.KEY_GROUP_ID, mGroupId);
            intent.putExtra(ParseConstants.KEY_GROUP_NAME, mGroupName);
            startActivity(intent);
        }
        else if(id == R.id.action_leave_group) {
            leaveGroupMessages();
        }
        else if(id == R.id.action_delete_group) {
            String message = getString(R.string.message_delete_group);
            deleteGroupDialog(message);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void leaveGroupMessages() {
        if((mCurrentUser.getUsername()).equals(mCurrentDrinker)){
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
            builder.setTitle(R.string.error_title)
                    .setMessage(getString(R.string.current_drinker_leaving_dialog))
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            Utilities.customDialog(dialog);
        }
        else if ((mCurrentUser.getUsername()).equals(mGroupAdmin)) {
            String message = getString(R.string.message_admin_leave_group);
            deleteGroupDialog(message);
        }
        else {
            //remove relations from the current user
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
            builder.setTitle(getString(R.string.message_title_leave_group))
                    .setMessage(getString(R.string.message_leave_group))
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mMemberRelation.remove(mCurrentUser);
                            mGroup.saveInBackground();
                            mMemberOfGroupRelation.remove(mGroup);
                            mCurrentUser.saveInBackground();
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            Utilities.customDialog(dialog);
        }
    }

    protected void deleteGroupDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
        builder.setTitle(getString(R.string.message_title_delete_group))
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mGroup.deleteInBackground();
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        Utilities.customDialog(dialog);
    }
}
