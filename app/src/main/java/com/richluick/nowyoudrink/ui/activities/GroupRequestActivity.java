package com.richluick.nowyoudrink.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

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
    protected ArrayList<ParseUser> mGroupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_group_request);

        Utilities.setContext(this); //set the utilities context to this

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
                    Utilities.getErrorAlertDialog();
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

                            //query the group members
                            ParseQuery<ParseUser> query = mMemberRelation.getQuery();
                            query.findInBackground(new FindCallback<ParseUser>() {
                                @Override
                                public void done(List<ParseUser> members, ParseException e) {
                                    if (e == null) {
                                        mGroupMembers = (ArrayList<ParseUser>) members;
                                    }
                                    else {
                                        Utilities.getErrorAlertDialog();
                                    }
                                }
                            });

                        }
                        else { //error. group no longer exists. finish to MainActivity
                            Utilities.getNoGroupAlertDialog(mMessage);
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
                            Utilities.getErrorAlertDialog();
                        }
                    }
                });

                //add to list of groups the user is a part of (inverse group relation
                mMemberOfGroupRelation.add(mGroup);
                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Utilities.getErrorAlertDialog();
                        }
                    }
                });
                Utilities.deleteMessage(mMessage);

                //sends a push to all group members saying the user joined
                Utilities.sendPushNotifications(null, mGroupMembers,
                        ParseUser.getCurrentUser().getUsername() + " joined the group " + mGroupName + "!",
                        "mr");

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
                            Utilities.getErrorAlertDialog();
                        }
                    }
                });
                Utilities.deleteMessage(mMessage);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utilities.setContext(null); //set context to null to prevent leak
    }
}
