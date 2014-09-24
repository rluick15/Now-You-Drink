package com.richluick.nowyoudrink;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;


public class ViewFriendRequestActivity extends Activity {

    protected TextView mRequestText;
    protected Button mAcceptButton;
    protected Button mRejectButton;
    protected String mSenderId;
    protected String mUsername;
    protected ParseRelation<ParseUser> mPendingRelation;
    protected ParseUser mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_view_friend_request);

        mCurrentUser = ParseUser.getCurrentUser();
        mPendingRelation = mCurrentUser.getRelation(ParseConstants.KEY_PENDING_RELATION);

        mRequestText = (TextView) findViewById(R.id.text);
        mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mRejectButton = (Button) findViewById(R.id.rejectButton);

//        mSenderId = getIntent().getStringExtra(ParseConstants.KEY_SENDER_ID);
//        mPendingRelation.add(mSender.getParseUser(mSenderId));

        //checks the message object for user id and displays it
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Messages");
        query.getInBackground(mSenderId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, com.parse.ParseException e) {
                if (e == null) {
                    mUsername = parseObject.get(ParseConstants.KEY_SENDER_NAME).toString();
                    mRequestText.setText(mUsername + " has sent you a friend request!");
                }
            }
        });

        //User Clicks on the accept button
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProgressBarIndeterminateVisibility(true);


            }
        });

    }
}