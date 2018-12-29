package tech.xiaosuo.com.phontomanager;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import tech.xiaosuo.com.phontomanager.bean.UserInfo;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private static final String TAG = "LoginActivity";
    // UI references.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ProgressDialog progressDialog;
    private Context mContext;
    private Context mActivity;
    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL=-1;
    private static final int SERVER_ERROR = 1;
    private static final int IO_EXCEPTION = 2;
    private final int PASSWORD_LOGIN = 0;
    private final int SMS_CODE_LOGIN = 1;
    private int loginType = PASSWORD_LOGIN;
    TextInputLayout userNameInputLayout;
    TextInputLayout passWordInputLayout;
    Button smsCodeButton;
    Button getSmsCodeButton;
/*    LinearLayout passwordLayout;
    LinearLayout smsCodeLayout;*/

   // BackUpApplication mApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG," wangsm LoginActivity onCreate");
        mContext = getApplicationContext();
        mActivity = this;
     //   mApp = (BackUpApplication)getApplication();
        // Set up the login form.
        userNameInputLayout = (TextInputLayout)findViewById(R.id.username_input_layout);
        passWordInputLayout = (TextInputLayout)findViewById(R.id.password_input_layout);
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.user_name);

        mPasswordView = (EditText) findViewById(R.id.password);
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

        Button mUserSignInButton = (Button) findViewById(R.id.user_login_button);
        mUserSignInButton.setOnClickListener(this);
        Button registerButton = (Button)findViewById(R.id.user_register_button);
        registerButton.setOnClickListener(this);
        smsCodeButton = (Button)findViewById(R.id.sms_code_login);
        smsCodeButton.setOnClickListener(this);
        getSmsCodeButton = (Button)findViewById(R.id.get_sms_code);
        getSmsCodeButton.setOnClickListener(this);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

  /*      passwordLayout = (LinearLayout) findViewById(R.id.password_linearlayout);
        smsCodeLayout = (LinearLayout) findViewById(R.id.sms_code_linearlayout);*/
    }




    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUserNameView.setError(getString(R.string.error_invalid_username));
            focusView = mUserNameView;
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
            int loginType = getLoginType();
            Log.d(TAG," the login type is: " + loginType);
            switch (loginType){
                case PASSWORD_LOGIN:
                    LoginByPassword(username,password);
                    break;
                case SMS_CODE_LOGIN:
                    LoginBySmsCode(username,password);
                    break;
                default:
                    break;
            }
/*            if(getLoginType() == PASSWORD_LOGIN){
                LoginByPassword(username,password);
            }else if(loginType == SMS_CODE_LOGIN){
                LoginBySmsCode(username,password);
            }*/
/*            BmobUser.loginByAccount(username, password, new LogInListener<UserInfo>() {

                @Override
                public void done(UserInfo user, BmobException e) {
                    showProgress(false);
                    if(user!=null){
                        Log.d(TAG,"login success");
                        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    }else{
                        Log.d(TAG,"login fail " + e.toString());
                        new AlertDialog.Builder(mActivity).setTitle(R.string.dialog_title).setMessage(e.toString()).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                    }
                }
            });*/

        }
    }

    private boolean isUsernameValid(String email) {
        //TODO: Replace this with your own logic
        return true;//email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * check the phone number whether is valid.
     * @param phoneNumber
     * @return
     */
    private boolean isValidPhoneNumber(String phoneNumber){
           if(phoneNumber == null || phoneNumber.length()<11){
              return false;
           }
           return true;
    }
    /**
     * Shows the progress UI and hides the login form.
     */

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
         if(progressDialog == null){
             progressDialog = new ProgressDialog(this);
         }
        if(show){
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.login_progress_message));
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }else{
            progressDialog.cancel();
            progressDialog = null;
        }

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.user_login_button:
                attemptLogin();
                break;
            case R.id.user_register_button:
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.sms_code_login:
                toggleLoginType();
                break;
            case R.id.get_sms_code:
                if(mUserNameView==null || mUserNameView.getText() == null){
                    Log.d(TAG," the phone number is null,return");
                    return;
                }
                String phoneNumber = mUserNameView.getText().toString();
                boolean isValidPhoneNumber = isValidPhoneNumber(phoneNumber);
                if(isValidPhoneNumber){
                    sendSmsCodeRequest(phoneNumber);
                }

                break;
           default:
               break;
        }
    }

    /**
     *
     * switch the login type
     * type:PASSWORD_LOGIN
     *      SMS_CODE_LOGIN
     */
    private void toggleLoginType(){
        if(loginType == PASSWORD_LOGIN){
            loginType = SMS_CODE_LOGIN;
        }else{
            loginType = PASSWORD_LOGIN;
        }
        updateStringByLoginType(loginType);
    }

    /**
     * if the login type is PASSWORD_LOGIN,need show username/telphone + password
     * if the login type is SMS_CODE_LGOIN, need show telphone + sms code.
     * @param loginType
     */
    private void updateStringByLoginType(int loginType){

           mUserNameView.setText(null);
           mPasswordView.setText(null);//we need clear the password text.
           switch (loginType){
               case PASSWORD_LOGIN:
                   getSmsCodeButton.setVisibility(View.GONE);
                   userNameInputLayout.setHint(getResources().getString(R.string.prompt_username));
                   //passWordInputLayout.setHint(getResources().getString(R.string.prompt_password));
                   mUserNameView.setInputType(InputType.TYPE_CLASS_TEXT);
                   mPasswordView.setHint(getResources().getString(R.string.prompt_password));
                   smsCodeButton.setText(R.string.sms_code);
                   break;
               case SMS_CODE_LOGIN:
                   getSmsCodeButton.setVisibility(View.VISIBLE);
                   userNameInputLayout.setHint(getResources().getString(R.string.phone_number));
                   //passWordInputLayout.setHint(getResources().getString(R.string.sms_code));
                   mUserNameView.setInputType(InputType.TYPE_CLASS_PHONE);
                   mPasswordView.setHint(getResources().getString(R.string.sms_code));
                  smsCodeButton.setText(R.string.username_password_login);
                   break;
               default:
                   getSmsCodeButton.setVisibility(View.GONE);
                   userNameInputLayout.setHint(getResources().getString(R.string.prompt_username));
                   mPasswordView.setHint(getResources().getString(R.string.prompt_password));
                   smsCodeButton.setText(R.string.sms_code);
                   break;
           }
    }

    /**
     * login using username + password
     * @param username
     * @param password
     */
    private void LoginByPassword( String username, String password){
        BmobUser.loginByAccount(username, password, new LogInListener<UserInfo>() {

            @Override
            public void done(UserInfo user, BmobException e) {
                showProgress(false);
                if(user!=null){
                    Log.d(TAG,"login success");
                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }else{
                    Log.d(TAG,"login fail " + e.toString());
                    new AlertDialog.Builder(mActivity).setTitle(R.string.dialog_title).setMessage(e.toString()).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();
                }
            }
        });
    }

    /**
     * login by phone number + sms code.
     * @param phone
     * @param smsCode
     */
    private void LoginBySmsCode(String phone,String smsCode){
        /**
         * TODO 此API需要在用户已经注册并验证的前提下才能使用
         */
        BmobUser.loginBySMSCode(phone, smsCode, new LogInListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                showProgress(false);
                if (e == null) {
                    Log.d(TAG,"login success by sms code" + bmobUser.getObjectId() + "-" + bmobUser.getUsername());
                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    Log.d(TAG,"login fail by sms code" + e.getErrorCode() + "-" + e.getMessage() + "\n");
                    Toast.makeText(mContext,"login fail by sms code" + e.getErrorCode() + "-" + e.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"login success by sms code" + bmobUser.getObjectId() + "-" + bmobUser.getUsername());
                }
            }
        });
    }

    /**
     * send sms code request to Bmob server
     * @param phone
     */
    private void sendSmsCodeRequest(String phone){
        /**
         * TODO template 如果是自定义短信模板，此处替换为你在控制台设置的自定义短信模板名称；如果没有对应的自定义短信模板，则使用默认短信模板，默认模板名称为空字符串""。
         *
         * TODO 应用名称以及自定义短信内容，请使用不会被和谐的文字，防止发送短信验证码失败。
         *
         */
        BmobSMS.requestSMSCode(phone, "", new QueryListener<Integer>() {
            @Override
            public void done(Integer smsId, BmobException e) {
                if (e == null) {
                    Log.d(TAG,"send sms code request success " + smsId + "\n");
                    Toast.makeText(mContext,"send sms code request success",Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG,"send sms code request fail " + e.getErrorCode() + "-" + e.getMessage());
                    Toast.makeText(mContext,"send sms code request fail" + e.getErrorCode() + "-" + e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public int getLoginType() {
        return loginType;
    }
}

