package com.richluick.nowyoudrink.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import static com.richluick.nowyoudrink.utils.ParseConstants.TYPE_FRIEND_REQUEST_CONFIRM;


public class ViewFriendRequestActivity extends Activity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected TextView mRequestText;
    protected Button mAcceptButton;
    protected Button mRejectButton;
    protected String mSenderId;
    protected ParseUser mSender;
    protected String mUsername;
    protected ParseRelation<ParseUser> mPendingRelation;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_view_friend_request);

        Utilities.setContext(this); //set the utilities context to this

        mCurrentUser = ParseUser.getCurrentUser();
        //Define Relations
        mPendingRelation = mCurrentUser.getRelation(ParseConstants.KEY_PENDING_RELATION);
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        mRequestText = (TextView) findViewById(R.id.text);
        mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mRejectButton = (Button) findViewById(R.id.rejectButton);

        mSenderId = getIntent().getStringExtra(ParseConstants.KEY_SENDER_ID);

        //gets the full user object based on senderId from the message
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(mSenderId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, com.parse.ParseException e) {
                if (e == null) {
                    mUsername = parseUser.getUsername();
                    mRequestText.setText(mUsername + " has sent you a friend request!");
                    mSender = parseUser;
                }
                else { //error
                    Utilities.getErrorAlertDialog();
                }
            }
        });


        //User Clicks on the accept button
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend();

                String type = TYPE_FRIEND_REQUEST_CONFIRM;
                ParseObject message = createMessage(type);

                if(message == null) { //error
                    Utilities.getErrorAlertDialog();
                }
                else { //sends the message and closes the activity
                    send(message, type);
                    returnIntent();
                }
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectRequest();

                String type = ParseConstants.TYPE_FRIEND_REQUEST_DENY;
                ParseObject message = createMessage(type);

                if(message == null) { //error
                    Utilities.getErrorAlertDialog();
                }
                else { //sends the message and closes the activity
                    send(message, type);
                    returnIntent();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utilities.setContext(null); //set context to null to prevent leak
    }

    protected void rejectRequest() {
        mPendingRelation.remove(mSender); //remove from Pending

        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Utilities.getErrorAlertDialog();
                }
            }
        });
    }

    protected void addFriend() {
        mFriendsRelation.add(mSender); //add to friends list
        mPendingRelation.remove(mSender); //remove from Pending

        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Utilities.getErrorAlertDialog();
                }
            }
        });
    }

    protected void returnIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(ParseConstants.KEY_SENDER_ID, mSenderId);
        startActivity(intent);
    }

    protected ParseObject createMessage(String type) {
        ArrayList<ParseUser> sender = new ArrayList<ParseUser>();
        sender.add(mSender);

        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER, ParseUser.getCurrentUser());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, sender);
        message.put(ParseConstants.KEY_MESSAGE_TYPE, type);
        message.put(ParseConstants.KEY_GROUP_ID, "");

        return message;
    }

    //message is sent to recipients
    protected void send(ParseObject message, final String type) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //success
                    if (type.equals(TYPE_FRIEND_REQUEST_CONFIRM)) {
                        Toast.makeText(ViewFriendRequestActivity.this, getString(R.string.success_message_accept_request), Toast.LENGTH_LONG).show();
                        Utilities.sendPushNotifications(mSender, null,
                                ParseUser.getCurrentUser().getUsername() + " has accepted your friend request!",
                                "sr");
                    } else {
                        Toast.makeText(ViewFriendRequestActivity.this, getString(R.string.success_message_reject_request), Toast.LENGTH_LONG).show();
                    }
                    //sendPushNotifications();
                } else { //error sending message
                    Utilities.getErrorAlertDialog();
                }
            }
        });
    }
}