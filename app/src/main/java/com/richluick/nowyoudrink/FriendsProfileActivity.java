package com.richluick.nowyoudrink;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class FriendsProfileActivity extends Activity {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    protected String mUsername;
    protected String mFirstName;
    protected String mLastName;
    protected String mFullName;
    protected String mEmail;
    protected String mHometown;
    protected String mWebsite;
    protected TextView mUsernameField;
    protected TextView mFullNameField;
    protected TextView mEmailField;
    protected TextView mHometownField;
    protected TextView mWebsiteField;
    protected String mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);

        mUsernameField = (TextView) findViewById(R.id.usernameSpace);
        mFullNameField = (TextView) findViewById(R.id.fullNameSpace);
        mEmailField = (TextView) findViewById(R.id.emailSpace);
        mHometownField = (TextView) findViewById(R.id.hometownSpace);
        mWebsiteField = (TextView) findViewById(R.id.websiteSpace);

        mId = getIntent().getStringExtra(ParseConstants.KEY_SENDER_ID);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(mId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, com.parse.ParseException e) {
                if (e == null) {
                    mUsername = parseUser.getUsername();
                    mFirstName = parseUser.get(ParseConstants.KEY_FIRST_NAME).toString();
                    mLastName = parseUser.get(ParseConstants.KEY_LAST_NAME).toString();
                    mFullName = mFirstName + " " + mLastName;
                    mEmail = parseUser.getEmail();
                    mHometown = parseUser.get(ParseConstants.KEY_HOMETOWN).toString();
                    mWebsite = parseUser.get(ParseConstants.KEY_WEBSITE).toString();

                    mUsernameField.setText(mUsername);
                    mFullNameField.setText("Name: " + mFullName);
                    mEmailField.setText("Email: " + mEmail);
                    mHometownField.setText("Hometown: " + mHometown);
                    mWebsiteField.setText("Website: " + mWebsite);
                    Linkify.addLinks(mWebsiteField, Linkify.ALL);
                }
                else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsProfileActivity.this);
                    builder.setTitle(R.string.error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }

}
