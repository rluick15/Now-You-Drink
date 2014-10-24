package com.richluick.nowyoudrink.ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.richluick.nowyoudrink.NowYouDrinkApplication;
import com.richluick.nowyoudrink.R;
import com.richluick.nowyoudrink.utils.ParseConstants;
import com.richluick.nowyoudrink.utils.Utilities;


public class SignUpActivity extends Activity {

    protected EditText mUsername;
    protected EditText mPassword;
    protected EditText mEmail;
    protected EditText mFirstName;
    protected EditText mLastName;
    protected EditText mHometown;
    protected EditText mWebsite;
    protected Button mSignUpButton;
    protected Button mCancelButton;
    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //set the title to custom font
        TextView txt = (TextView) findViewById(R.id.titleText);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Stainy.ttf");
        txt.setTypeface(font);

        Utilities.setContext(this); //set the utilities context to this

        //Get progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        //Hide the action bar
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.hide();

        mUsername = (EditText) findViewById(R.id.usernameField);
        mPassword = (EditText) findViewById(R.id.passwordField);
        mEmail = (EditText) findViewById(R.id.emailField);
        mFirstName = (EditText) findViewById(R.id.firstNameField);
        mLastName = (EditText) findViewById(R.id.lastNameField);
        mHometown = (EditText) findViewById(R.id.hometownField);
        mWebsite = (EditText) findViewById(R.id.websiteField);

        //Returns to Login Screen
        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Signs up User with given info
        mSignUpButton = (Button) findViewById(R.id.signupButton);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();
                String firstName = mFirstName.getText().toString();
                String lastName = mLastName.getText().toString();
                String hometown = mHometown.getText().toString();
                String website = mWebsite.getText().toString();

                username = username.trim();
                password = password.trim();
                email = email.trim();
                firstName = firstName.trim();
                lastName = lastName.trim();
                String fullName = firstName + " " + lastName;
                hometown = hometown.trim();
                website = website.trim();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    mProgressBar.setVisibility(View.INVISIBLE);

                    //Checks if the user left a field blank and displays an alert message
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setTitle(getString(R.string.signup_error_title))
                            .setMessage(getString(R.string.signup_error_message))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    Utilities.customDialog(dialog);
                }
                else {
                    mProgressBar.setVisibility(View.INVISIBLE);

                    //new user is signed up on parse.com
                    setProgressBarIndeterminateVisibility(true);

                    ParseUser newUser = new ParseUser();
                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.setEmail(email);
                    newUser.put(ParseConstants.KEY_FULL_NAME, fullName);
                    newUser.put(ParseConstants.KEY_FULL_NAME_LOWERCASE, fullName.toLowerCase());
                    newUser.put(ParseConstants.KEY_HOMETOWN, hometown);
                    newUser.put(ParseConstants.KEY_WEBSITE, website);

                    newUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            setProgressBarIndeterminateVisibility(false);

                            if (e == null) {
                                //If sign up is a success, user is taken to the inbox
                                NowYouDrinkApplication.updateParseInstallation(ParseUser.getCurrentUser());

                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else {
                                //if sign up fails, error message is displayed
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setTitle(R.string.signup_error_title)
                                        .setMessage(e.getMessage())
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                Utilities.customDialog(dialog);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utilities.setContext(null); //set context to null to prevent leak
    }
}
