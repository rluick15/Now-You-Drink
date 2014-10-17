package com.richluick.nowyoudrink.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.richluick.nowyoudrink.utils.MD5Util;
import com.richluick.nowyoudrink.utils.ParseConstants;
import com.richluick.nowyoudrink.R;
import com.squareup.picasso.Picasso;


public class PersonalProfileActivity extends Activity {

    protected String mUsername;
    protected String mFullName;
    protected String mEmail;
    protected String mHometown;
    protected String mWebsite;
    protected TextView mUsernameField;
    protected TextView mFullNameField;
    protected TextView mEmailField;
    protected TextView mHometownField;
    protected TextView mWebsiteField;
    protected ImageView mUserImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile);

        mUsernameField = (TextView) findViewById(R.id.usernameSpace);
        mFullNameField = (TextView) findViewById(R.id.fullNameSpace);
        mEmailField = (TextView) findViewById(R.id.emailSpace);
        mHometownField = (TextView) findViewById(R.id.hometownSpace);
        mWebsiteField = (TextView) findViewById(R.id.websiteSpace);
        mUserImageView = (ImageView) findViewById(R.id.userImageView);

        ParseUser currentUser = ParseUser.getCurrentUser();

        mUsername = currentUser.getUsername();
        mFullName = currentUser.get(ParseConstants.KEY_FULL_NAME).toString();
        mEmail = currentUser.getEmail();
        mHometown = currentUser.get(ParseConstants.KEY_HOMETOWN).toString();
        mWebsite = currentUser.get(ParseConstants.KEY_WEBSITE).toString();

        setProfilePicture();

        mUsernameField.setText(mUsername);
        mFullNameField.setText("Name: " + mFullName);
        mEmailField.setText("Email: " + mEmail);
        mHometownField.setText("Hometown: " + mHometown);
        mWebsiteField.setText("Website: " + mWebsite);
        Linkify.addLinks(mWebsiteField, Linkify.ALL);
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
}
