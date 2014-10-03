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
    protected List<ParseUser> mMembers;
    protected ParseUser mNextDrinker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_group);

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

                mGroup = group;
                mMemberRelation = mGroup.getRelation(ParseConstants.KEY_MEMBER_RELATION);
                mMemberOfGroupRelation = mCurrentUser.getRelation(ParseConstants.KEY_MEMBER_OF_GROUP_RELATION);

                mGroupName = group.get(ParseConstants.KEY_GROUP_NAME).toString();
                mGroupName = MainActivity.removeCharacters(mGroupName);
                setTitle(mGroupName);

                mCurrentDrinker = mGroup.get(ParseConstants.KEY_CURRENT_DRINKER).toString();
                mCurrentDrinker = MainActivity.removeCharacters(mCurrentDrinker);
                mCurrentDrinkerView.setText(mCurrentDrinker);

                mPreviousDrinker = mGroup.get(ParseConstants.KEY_PREVIOUS_DRINKER).toString();
                mPreviousDrinker = MainActivity.removeCharacters(mPreviousDrinker);
                mPreviousDrinkerView.setText(mPreviousDrinker);

                listViewQuery(mMemberRelation);

                //activate the button for the current drinker and let them know they are up
                if((mCurrentUser.getUsername()).equals(mCurrentDrinker)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                    builder.setTitle(getString(R.string.now_you_drink_title))
                            .setMessage(getString(R.string.now_you_drink_message))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
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
                }
                else {
                    mGroup.remove(ParseConstants.KEY_CURRENT_DRINKER);
                    mGroup.remove(ParseConstants.KEY_PREVIOUS_DRINKER);
                    mGroup.add(ParseConstants.KEY_PREVIOUS_DRINKER, mCurrentDrinker);
                    mGroup.add(ParseConstants.KEY_CURRENT_DRINKER, mNextDrinker.getUsername());
                    mGroup.saveInBackground();

                    //create and send the Now you drink message
                    ParseObject message = createMessage();
                    if(message == null) { //error
                        AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                        builder.setMessage(getString(R.string.message_drink_request_error))
                                .setTitle(getString(R.string.error_message_title))
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
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

    //queries the users in the group and formats them for the list view
    private void listViewQuery(ParseRelation<ParseUser> mMemberRelation) {
        ParseQuery<ParseUser> query = mMemberRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
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
                            android.R.layout.simple_list_item_activated_1,
                            usernames);
                    setListAdapter(adapter);
                }
                else { //error message dialog if the query fails
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    //Friend Request Message is create with relevant information
    protected ParseObject createMessage() {
        ArrayList<ParseUser> nextDrinker = new ArrayList<ParseUser>();
        nextDrinker.add(mNextDrinker);

        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER, ParseUser.getCurrentUser());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, nextDrinker);
        message.put(ParseConstants.KEY_MESSAGE_TYPE, ParseConstants.TYPE_DRINK_REQUEST);
        message.saveInBackground();

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
                    //sendPushNotifications();
                }
                else { //error sending message
                    Log.e(TAG, e.getMessage());
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
