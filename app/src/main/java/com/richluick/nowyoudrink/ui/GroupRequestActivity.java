package com.richluick.nowyoudrink.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.richluick.nowyoudrink.utils.Utilities;
import com.richluick.nowyoudrink.utils.ParseConstants;
import com.richluick.nowyoudrink.R;


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
    protected ParseRelation<ParseUser> mPendingMemberRelation;
    protected ParseRelation<ParseObject> mMemberOfGroupRelation;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_group_request);

        mCurrentUser = ParseUser.getCurrentUser();
        mMemberOfGroupRelation = mCurrentUser.getRelation(ParseConstants.KEY_MEMBER_OF_GROUP_RELATION);

        mRequestText = (TextView) findViewById(R.id.text);
        mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mRejectButton = (Button) findViewById(R.id.rejectButton);

        //Get Intents
        mMessageId = getIntent().getStringExtra(ParseConstants.KEY_ID);

        //gets the full message object based on senderId from the message
        ParseQuery<ParseObject> messageQuery = ParseQuery.getQuery(ParseConstants.CLASS_MESSAGES);
        messageQuery.getInBackground(mMessageId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, com.parse.ParseException e) {
                if (e == null) {
                    mMessage = parseObject;
                    mGroupId = mMessage.get(ParseConstants.KEY_GROUP_ID).toString();
                }
                else { //error is found return to previous activity
                    Log.e(TAG, e.getMessage());
                    finish();
                }

                //gets the full group object based on senderId from the message
                ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery(ParseConstants.CLASS_GROUPS);
                groupQuery.getInBackground(mGroupId, new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject group, com.parse.ParseException e) {
                        if (e == null) { //add text to screen
                            mGroup = group;
                            mGroupName = mGroup.get(ParseConstants.KEY_GROUP_NAME).toString();
                            mGroupName = Utilities.removeCharacters(mGroupName);
                            mSenderUsername = mMessage.get(ParseConstants.KEY_SENDER_NAME).toString();
                            mRequestText.setText(mSenderUsername + " has invited you to join the group \""
                                    + mGroupName + "\"!");

                            //define relations
                            mMemberRelation = mGroup.getRelation(ParseConstants.KEY_MEMBER_RELATION);
                            mPendingMemberRelation = mGroup.getRelation(ParseConstants.KEY_PENDING_MEMBER_RELATION);
                        }
                        else { //error. group no longer exists. finish to MainActivity
                            deleteMessageDialog();
                        }
                    }
                });
            }
        });

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add relations if user accepts
                mMemberRelation.add(mCurrentUser);
                mPendingMemberRelation.remove(mCurrentUser);
                mGroup.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });

                //add to list of groups the user is a part of (inverse group relation
                mMemberOfGroupRelation.add(mGroup);
                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
                Utilities.deleteMessage(mMessage);

                Intent intent = new Intent(GroupRequestActivity.this, GroupActivity.class);
                intent.putExtra(ParseConstants.KEY_GROUP_ID, mGroupId);
                startActivity(intent);
                finish();
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //remove pending relation if user accepts
                mPendingMemberRelation.remove(mCurrentUser);
                mGroup.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
                Utilities.deleteMessage(mMessage);
                finish();
            }
        });

    }

    private void deleteMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_title_nonexistent_group))
                .setMessage(getString(R.string.message_nonexistent_group))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utilities.deleteMessage(mMessage);
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        Utilities.customDialog(dialog);
    }
}
