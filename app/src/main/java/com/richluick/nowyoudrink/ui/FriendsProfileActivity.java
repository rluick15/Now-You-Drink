package com.richluick.nowyoudrink.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.richluick.nowyoudrink.utils.MD5Util;
import com.richluick.nowyoudrink.utils.ParseConstants;
import com.richluick.nowyoudrink.R;
import com.squareup.picasso.Picasso;


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
    protected ImageView mUserImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);

        mUsernameField = (TextView) findViewById(R.id.usernameSpace);
        mFullNameField = (TextView) findViewById(R.id.fullNameSpace);
        mEmailField = (TextView) findViewById(R.id.emailSpace);
        mHometownField = (TextView) findViewById(R.id.hometownSpace);
        mWebsiteField = (TextView) findViewById(R.id.websiteSpace);
        mUserImageView = (ImageView) findViewById(R.id.userImageView);

        mId = getIntent().getStringExtra(ParseConstants.KEY_ID);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(mId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, com.parse.ParseException e) {
                if (e == null) {
                    mUsername = parseUser.getUsername();
                    setTitle(mUsername + "'s Profile"); //set the page title to the friends username
                    mFullName = parseUser.get(ParseConstants.KEY_FULL_NAME).toString();
                    mEmail = parseUser.getEmail();
                    mHometown = parseUser.get(ParseConstants.KEY_HOMETOWN).toString();
                    mWebsite = parseUser.get(ParseConstants.KEY_WEBSITE).toString();

                    setProfilePicture();

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
                    customDialog(dialog);
                }
            }
        });
    }

    private void setProfilePicture() {
        String email = mEmail.toLowerCase();
        if(email.equals("")) {//Use default image if no email is given
            mUserImageView.setImageResource(R.drawable.avatar_empty);
        }
        else { //use gravatar image if email is given and if user has account
            String hash = MD5Util.md5Hex(email);
            String gravatarUrl = "http://www.gravatar.com/avatar/" + hash + "?s=408&d=404";
            Picasso.with(this).load(gravatarUrl)
                    .placeholder(R.drawable.avatar_empty) //default image if no account avail
                    .into(mUserImageView);
        }
    }

    //set the colors for the custom dialogs
    protected void customDialog(AlertDialog dialog) {
        //custom divider color
        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = dialog.findViewById(dividerId);
        divider.setBackgroundColor(getResources().getColor(R.color.main_color));

        //custom title color
        int textViewId = dialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        TextView tv = (TextView) dialog.findViewById(textViewId);
        tv.setTextColor(getResources().getColor(R.color.main_color));
    }

}
