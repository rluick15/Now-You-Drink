package com.richluick.nowyoudrink;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;


public class LoginActivity extends Activity {

    protected EditText mUsername;
    protected EditText mPassword;
    protected Button mLoginButton;
    protected ProgressBar mProgressBar;

    protected TextView mSignUpTextView;
    protected TextView mForgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        //Hide the action bar
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        //Navigate to SignUp when link is clicked
        mSignUpTextView = (TextView) findViewById(R.id.signUpText);
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        //Navigate to Forgot Password when link is clicked
        mForgotPasswordTextView = (TextView) findViewById(R.id.forgotPasswordText);
        mForgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        mUsername = (EditText) findViewById(R.id.usernameField);
        mPassword = (EditText) findViewById(R.id.passwordField);
        mLoginButton = (Button) findViewById(R.id.loginButton);

        //Log In on Button click if all info is correct
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);

                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                username = username.trim();
                password = password.trim();

                if (username.isEmpty() || password.isEmpty()) {
                    mProgressBar.setVisibility(View.INVISIBLE);

                    //alert user if username or password are empty
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(getString(R.string.login_error_title))
                            .setMessage(getString(R.string.login_error_message))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else { //Login
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            mProgressBar.setVisibility(View.INVISIBLE);

                            if (e == null) { //Login is a success, send user to inbox activity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else { //error logging in, error message is displayed
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle(R.string.login_error_title)
                                        .setMessage(e.getMessage())
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });
    }
}
