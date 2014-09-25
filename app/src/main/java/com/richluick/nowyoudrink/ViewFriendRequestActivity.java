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
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;


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
    protected ParseRelation<ParseUser> mPendingSenderRelation;
    protected ParseRelation<ParseUser> mFriendsSenderRelation;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_view_friend_request);

        mCurrentUser = ParseUser.getCurrentUser();

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
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewFriendRequestActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        //Define Relations
        mPendingRelation = mCurrentUser.getRelation(ParseConstants.KEY_PENDING_RELATION);
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
//        mPendingSenderRelation = mSender.getRelation(ParseConstants.KEY_PENDING_RELATION);
//        mFriendsSenderRelation = mSender.getRelation(ParseConstants.KEY_FRIENDS_RELATION);


        //User Clicks on the accept button
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend();
                finish();
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectRequest();
                finish();
            }
        });

    }

    private void rejectRequest() {
        mPendingRelation.remove(mSender); //remove from Pending

        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void addFriend() {
        mFriendsRelation.add(mSender); //add to friends list
        mPendingRelation.remove(mSender); //remove from Pending
//        mFriendsSenderRelation.add(mCurrentUser);
//        mPendingSenderRelation.remove(mCurrentUser);

        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

//        mSender.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, e.getMessage());
//                }
//            }
//        });
    }
}