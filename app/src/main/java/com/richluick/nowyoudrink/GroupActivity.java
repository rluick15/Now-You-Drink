package com.richluick.nowyoudrink;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class GroupActivity extends Activity {

    protected String mGroupId;
    protected ParseObject mGroup;
    protected String mGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setTitle("");

        mGroupId = getIntent().getStringExtra(ParseConstants.KEY_GROUP_ID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_GROUPS);
        query.getInBackground(mGroupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject group, ParseException e) {
                mGroup = group;
                mGroupName = group.get(ParseConstants.KEY_GROUP_NAME).toString()
                        .replace("[", "").replace("]", "");
                setTitle(mGroupName);
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
