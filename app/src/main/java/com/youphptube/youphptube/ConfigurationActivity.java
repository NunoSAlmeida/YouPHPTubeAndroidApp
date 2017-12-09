package com.youphptube.youphptube;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class ConfigurationActivity extends AppCompatActivity{

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mServerUrlView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    FrameLayout progressBarHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mServerUrlView = (AutoCompleteTextView) findViewById(R.id.server);
        mPasswordView = (EditText) findViewById(R.id.password);
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);


        SharedPreferences Defs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            mServerUrlView.setText(Defs.getString("ServerUrl", ""));
            mEmailView.setText(Defs.getString("UserName", ""));
            mPasswordView.setText(Defs.getString("Password", ""));



        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);

        if (Defs.getBoolean("AutoLogin", false)){
            if (mServerUrlView.getText().toString().length()>0){
                attemptLogin();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        HttpHandler.cookie = null;
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mServerUrlView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String server = mServerUrlView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(server, email, password);
            mAuthTask.execute((Void) null);
        }
    }


    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 3;
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private String mServerUrl;
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String ServerUrl, String email, String password) {
            mEmail = email;
            mPassword = password;
            mServerUrl = ServerUrl;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            HttpHandler sh = new HttpHandler();
            if (mServerUrl.endsWith("/")) {
                mServerUrl = mServerUrl.substring(0, mServerUrl.length()-1);
            }

            String LoginUrl = mServerUrl + "/login";
            HttpHandler.LoginInfo serverresponse =sh.Login(LoginUrl, mEmail, mPassword);

            if (serverresponse.response.equals("")){
                return 0;
            }

            if (serverresponse.cookie == null){
                return 0;
            }
            if (serverresponse.cookie.contains("PHPSESSID=")){
                if (mEmail.length() == 0 && mPassword.length() == 0){
                    return 1;
                }
            }

            if (serverresponse.response.contains("isLogged\":true,")){
                HttpHandler.cookie = serverresponse.cookie;
                return 1;
            }else return 2;


        }

        @Override
        protected void onPostExecute(final Integer result) {
            mAuthTask = null;
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);

            if (result == 1) {
                SharedPreferences Defs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = Defs.edit();
                editor.putString("ServerUrl", mServerUrlView.getText().toString());
                editor.putString("UserName", mEmailView.getText().toString());
                editor.putString("Password", mPasswordView.getText().toString());
                editor.putBoolean("AutoLogin", true);
                MasterClass.ServerUrl = mServerUrlView.getText().toString();
                MasterClass.ServerUsername = mEmailView.getText().toString();
                MasterClass.ServerPassword = mPasswordView.getText().toString();
                editor.apply();
                finish();
                Intent objIndent = new Intent(ConfigurationActivity.this,MasterActivity.class);
                startActivity(objIndent);
            } else {
                if (result == 0){
                    mServerUrlView.setError(getString(R.string.prompt_servererror));
                    mServerUrlView.requestFocus();
                }
                if (result == 2){
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
        }
    }
}

