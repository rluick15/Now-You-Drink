package com.richluick.nowyoudrink;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class GroupActivity extends Activity {

    protected String mGroupId;
    protected ParseObject mGroup;
    protected String mGroupName;
    protected ParseUser mGroupAdmin;
    protected String mCurrentDrinker;
    protected String mPreviousDrinker;
    protected TextView mCurrentDrinkerView;
    protected TextView mPreviousDrinkerView;
    protected Button mDrinkButton;
    protected List<ParseUser> mGroupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        mCurrentDrinkerView = (TextView) findViewById(R.id.currentDrinkerUser);
        mPreviousDrinkerView = (TextView) findViewById(R.id.previousDrinkerUser);
        mDrinkButton = (Button) findViewById(R.id.drinkButton);

        mGroupId = getIntent().getStringExtra(ParseConstants.KEY_GROUP_ID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_GROUPS);
        query.getInBackground(mGroupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject group, ParseException e) {
                mGroup = group;
                mGroupName = group.get(ParseConstants.KEY_GROUP_NAME).toString()
                        .replace("[", "").replace("]", "");
                setTitle(mGroupName);

                mCurrentDrinker = mGroup.get(ParseConstants.KEY_CURRENT_DRINKER).toString()
                        .replace("[", "").replace("]", "");
                mCurrentDrinkerView.setText(mCurrentDrinker);

                mPreviousDrinker = mGroup.get(ParseConstants.KEY_PREVIOUS_DRINKER).toString()
                        .replace("[", "").replace("]", "");
                mPreviousDrinkerView.setText(mPreviousDrinker);
            }
        });
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
