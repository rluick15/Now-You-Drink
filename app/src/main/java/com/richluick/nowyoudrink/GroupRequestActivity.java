package com.richluick.nowyoudrink;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;


public class GroupRequestActivity extends Activity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected TextView mRequestText;
    protected Button mAcceptButton;
    protected Button mRejectButton;
    protected String mGroupId;
    protected String mMessageId;
    protected ParseObject mMessage;
    protected ParseObject mGroup;
    protected String mGroupName;
    protected String mSenderUsername;
    protected ParseRelation<ParseUser> mMemberRelation;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_group_request);

        mCurrentUser = ParseUser.getCurrentUser();

        mRequestText = (TextView) findViewById(R.id.text);
        mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mRejectButton = (Button) findViewById(R.id.rejectButton);

        //Get Intents
        mMessageId = getIntent().getStringExtra(ParseConstants.KEY_ID);

        //gets the full message object based on senderId from the message
        ParseQuery<ParseObject> messageQuery = ParseQuery.getQuery("Messages");
        messageQuery.getInBackground(mMessageId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, com.parse.ParseException e) {
                if (e == null) {
                    mMessage = parseObject;
                    mSenderUsername = mMessage.get(ParseConstants.KEY_SENDER_NAME).toString();
                    mGroupId = ((ParseObject) mMessage.get(ParseConstants.KEY_GROUP)).getObjectId();
                }
                else { //error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupRequestActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        //gets the full group object based on senderId from the message
        ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Groups");
        groupQuery.getInBackground(mGroupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, com.parse.ParseException e) {
                if (e == null) {
                    mGroup = parseObject;
                    mGroupName = mGroup.get(ParseConstants.KEY_GROUP_NAME).toString();
                    mGroupName = mGroupName.replace("[", "");
                    mGroupName = mGroupName.replace("]", "");
                    mRequestText.setText(mSenderUsername + " has invited you to the group " + mGroupName + "\"!");
                    mMemberRelation = mGroup.getRelation(ParseConstants.KEY_MEMBER_RELATION);
                }
                else { //error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupRequestActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMemberRelation.add(mCurrentUser);
                mGroup.saveInBackground();
            }
        });

    }
}
