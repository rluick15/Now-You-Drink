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

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

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

                mGroupName = group.get(ParseConstants.KEY_GROUP_NAME).toString()
                        .replace("[", "").replace("]", "");
                setTitle(mGroupName);

                mCurrentDrinker = mGroup.get(ParseConstants.KEY_CURRENT_DRINKER).toString()
                        .replace("[", "").replace("]", "");
                mCurrentDrinkerView.setText(mCurrentDrinker);

                mPreviousDrinker = mGroup.get(ParseConstants.KEY_PREVIOUS_DRINKER).toString()
                        .replace("[", "").replace("]", "");
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

                mDrinkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mGroup.remove(ParseConstants.KEY_CURRENT_DRINKER);
                        mGroup.remove(ParseConstants.KEY_PREVIOUS_DRINKER);
                        mGroup.add(ParseConstants.KEY_PREVIOUS_DRINKER, mCurrentDrinker);
                        mGroup.add(ParseConstants.KEY_CURRENT_DRINKER, mNextDrinker.getUsername());
                        mGroup.saveInBackground();
                    }
                });
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
