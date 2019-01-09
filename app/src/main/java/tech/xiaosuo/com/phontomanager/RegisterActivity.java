package tech.xiaosuo.com.phontomanager;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import tech.xiaosuo.com.phontomanager.tools.Utils;
import tech.xiaosuo.com.phontomanager.bean.UserInfo;
//import tech.xiaosuo.com.bmob.http.HttpClientUtils;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "RegisterActivity";
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */


    // UI references.
    private AutoCompleteTextView mNickName;
    private AutoCompleteTextView mEmailView;
    private EditText mTelNumber;
    private EditText mPasswordView;
    private EditText mRepeatPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ProgressDialog progressDialog;
    private Context mActivity;
    private static final int USERNAME_EXIST = 202;
    private static final int EMAIL_EXIST = 203;
    private static final int PHONE_NUMBER_EXIST = 209;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mActivity = this;
        // Set up the login form.
        mNickName = (AutoCompleteTextView) findViewById(R.id.nick_name);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();
        mTelNumber = (EditText) findViewById(R.id.telnumber);

        mPasswordView = (EditText) findViewById(R.id.password);
        mRepeatPasswordView = (EditText) findViewById(R.id.repeat_password);
        mRepeatPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                Log.d(TAG," wangsm register action option");
                if (id == R.id.login || id == EditorInfo.IME_NULL || id == EditorInfo.IME_ACTION_DONE){
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        if(!Utils.isNetworkConnected(this)){
            Toast.makeText(this,R.string.pls_connect_network,Toast.LENGTH_SHORT).show();
            return;
        }
        // Reset errors.
        mEmailView.setError(null);
        mTelNumber.setError(null);
        mPasswordView.setError(null);
        mRepeatPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String nickname = mNickName.getText().toString();
        String email = mEmailView.getText().toString();
        String telnumber = mTelNumber.getText().toString();
        String password = mPasswordView.getText().toString();
        String rePassword = mRepeatPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }else if (!TextUtils.isEmpty(rePassword) && !isPasswordValid(rePassword)) {
            mRepeatPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mRepeatPasswordView;
            cancel = true;
        }else if (!rePassword.equals(password)) {
            mRepeatPasswordView.setError(getString(R.string.password_not_equal));
            focusView = mRepeatPasswordView;
            cancel = true;
        }


        // Check for a valid email address.
        if(mEmailView.getVisibility() == View.VISIBLE)
        {
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                focusView = mEmailView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailView;
                cancel = true;
            }
        }

        // Check for a valid tel address.
        if (TextUtils.isEmpty(telnumber) || (telnumber.length() < 11 && telnumber.length() > 0)) {
            mTelNumber.setError(getString(R.string.error_invalid_telnumber));
            focusView = mTelNumber;
            cancel = true;
        }
        // Check for a valid tel address.
        if (TextUtils.isEmpty(nickname)) {
            mNickName.setError(getString(R.string.error_invalid_telnumber));
            focusView = mNickName;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            UserInfo user = new UserInfo();
            user.setUsername(nickname);
            if(mEmailView.getVisibility() == View.VISIBLE){
                user.setEmail(email);
            }

            user.setMobilePhoneNumber(telnumber);
            user.setPassword(password);
            user.signUp(new SaveListener<UserInfo>() {
                @Override
                public void done(UserInfo s, BmobException e) {
                    showProgress(false);
                    if (e==null) {
                        Log.d(TAG," register success " + s.toString());
                       new AlertDialog.Builder(mActivity).setTitle(R.string.dialog_title).setMessage(R.string.register_sucess).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
                    }else{

                        Log.d(TAG," register fail " + e.toString());
                        int errorCode = e.getErrorCode();
                        switch (errorCode){
                            case USERNAME_EXIST:
                                mNickName.setError(getString(R.string.nickname_exist));
                                mNickName.requestFocus();
                                break;
                            case EMAIL_EXIST:
                                mEmailView.setError(getString(R.string.email_exist));
                                mEmailView.requestFocus();
                                break;
                            case PHONE_NUMBER_EXIST:
                                mTelNumber.setError(getString(R.string.telnumber_exist));
                                mTelNumber.requestFocus();
                                break;
                            default:
                                Toast.makeText(mActivity,e.toString(),Toast.LENGTH_SHORT).show();
                        }

                    }

                    /*else if(status.equals(HttpClientUtils.USERNAME_EXIST)){
                        mNickName.setError(getString(R.string.nickname_exist));
                        mNickName.requestFocus();
                    } else if(status.equals(HttpClientUtils.EMAIL_EXIST)){
                        mEmailView.setError(getString(R.string.email_exist));
                        mEmailView.requestFocus();
                    } else if(status.equals(HttpClientUtils.TELNUMBER_EXIST)){
                        mTelNumber.setError(getString(R.string.telnumber_exist));
                        mTelNumber.requestFocus();
                    }else {
                        Toast.makeText(mActivity,R.string.network_conn_exception,Toast.LENGTH_SHORT).show();
                    }*/
/*                    if(e==null){
                        toast("注册成功:" +s.toString());
                    }else{
                        loge(e);
                    }*/
                }
            });

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
        }
        if(show){
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.register_progress_message));
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);

            progressDialog.show();
        }else{
            progressDialog.cancel();
            progressDialog = null;
        }

       /* // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }*/
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


}

