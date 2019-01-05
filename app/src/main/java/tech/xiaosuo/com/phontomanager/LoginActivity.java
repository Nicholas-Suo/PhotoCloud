package tech.xiaosuo.com.phontomanager;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import cn.bmob.v3.listener.SaveListener;
import tech.xiaosuo.com.phontomanager.bean.UserInfo;
import tech.xiaosuo.com.phontomanager.tools.Utils;

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
    Button mUserLoginButton;
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

        mUserLoginButton = (Button) findViewById(R.id.user_login_button);
        mUserLoginButton.setOnClickListener(this);
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
                    LoginOrSignByPhoneNumber(username,password);
                  //  LoginBySmsCode(username,password);
                    break;
                default:
                    break;
            }
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
           if(phoneNumber == null || phoneNumber.length() != 11){
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
                    Toast.makeText(mContext,R.string.error_invalid_telnumber,Toast.LENGTH_SHORT).show();
                    return;
                }
                String phoneNumber = mUserNameView.getText().toString();
                boolean isValidPhoneNumber = isValidPhoneNumber(phoneNumber);
                if(isValidPhoneNumber){
                    timerRequestSmsCodeAgain(Utils.ONE_MINUTE);
                  //  sendSmsCodeRequest(phoneNumber);
                }else{
                    Toast.makeText(mContext,R.string.error_invalid_telnumber,Toast.LENGTH_SHORT).show();
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
                   mUserLoginButton.setText(R.string.action_sign_in_short);
                   break;
               case SMS_CODE_LOGIN:
                   getSmsCodeButton.setVisibility(View.VISIBLE);
                   userNameInputLayout.setHint(getResources().getString(R.string.phone_number));
                   //passWordInputLayout.setHint(getResources().getString(R.string.sms_code));
                   mUserNameView.setInputType(InputType.TYPE_CLASS_PHONE);
                   mPasswordView.setHint(getResources().getString(R.string.sms_code));
                   smsCodeButton.setText(R.string.username_password_login);
                   mUserLoginButton.setText(R.string.action_sign_in);
                   break;
               default:
                   getSmsCodeButton.setVisibility(View.GONE);
                   userNameInputLayout.setHint(getResources().getString(R.string.prompt_username));
                   mPasswordView.setHint(getResources().getString(R.string.prompt_password));
                   smsCodeButton.setText(R.string.sms_code);
                   mUserLoginButton.setText(R.string.action_sign_in_short);
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
        BmobSMS.requestSMSCode(phone, "相册云短信模板", new QueryListener<Integer>() {
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

    /**
     * 一键注册或登录的同时保存其他字段的数据
     * @param phone
     * @param code
     */
    private void signOrLogin(String phone,String code) {
        UserInfo user = new UserInfo();
        user.setUsername(phone);
        user.setEmail("");
        //设置手机号码（必填）
        user.setMobilePhoneNumber(phone);
        user.setPassword("");
/*        User user = new User();
        //设置手机号码（必填）
        user.setMobilePhoneNumber(phone);
        //设置用户名，如果没有传用户名，则默认为手机号码
        user.setUsername(phone);
        //设置用户密码
        user.setPassword("");
        //设置额外信息：此处为年龄*/

        user.signOrLogin(code, new SaveListener<UserInfo>() {

            @Override
            public void done(UserInfo user,BmobException e) {
                if (e == null) {
               //     mTvInfo.append("短信注册或登录成功：" + user.getUsername());
                  //  startActivity(new Intent(UserSignUpOrLoginSmsActivity.this, UserMainActivity.class));
                } else {
                  //  mTvInfo.append("短信注册或登录失败：" + e.getErrorCode() + "-" + e.getMessage() + "\n");
                }
            }
        });
    }

    /**
     * use phone number login or sign.
     * @param phone
     * @param sms_code
     */
    private void LoginOrSignByPhoneNumber(String phone,String sms_code){
        BmobUser.signOrLoginByMobilePhone(phone, sms_code, new LogInListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                showProgress(false);
                if (e == null) {
                    Log.d(TAG,"login/register success by sms code" + bmobUser.getObjectId() + "-" + bmobUser.getUsername());
                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                  //  mTvInfo.append("短信注册或登录成功：" + bmobUser.getUsername());
                  //  startActivity(new Intent(UserSignUpOrLoginSmsActivity.this, UserMainActivity.class));
                } else {
                   // mTvInfo.append("短信注册或登录失败：" + e.getErrorCode() + "-" + e.getMessage() + "\n");
                    Log.d(TAG,"login/register fail by sms code" + e.getErrorCode() + "-" + e.getMessage() + "\n");
                    Toast.makeText(mContext,"login fail by sms code" + e.getErrorCode() + "-" + e.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"login/register by sms code" + bmobUser.getObjectId() + "-" + bmobUser.getUsername());
                }
            }
        });
    }

    public int getLoginType() {
        return loginType;
    }

    Handler loginHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what){
                case Utils.REFRESH_SEND_SMS_CODE_TIMER:
                    int second = msg.arg1;
                    if(second > 0){
                        second--;
                        timerRequestSmsCodeAgain(second);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * begin send msg to refresh the Button : "Request sms code button" per 1s.
     * @param second
     */
    private void timerRequestSmsCodeAgain(int second){
        Log.d(TAG," the left time is " + second + " second");
        updateRequestSmsCodeButtonText(second);
        if(second < 0 || second > Utils.ONE_MINUTE){
            return;
        }
        Message msg = new Message();
        msg.what = Utils.REFRESH_SEND_SMS_CODE_TIMER;
        msg.arg1 = second;
        loginHandler.sendMessageDelayed(msg,1000);
    }

    /**
     * update the reqest sms code button's text
     * @param second
     */
    @SuppressLint("StringFormatInvalid")
    private void updateRequestSmsCodeButtonText(int second){
        if(second <= 0){
            getSmsCodeButton.setText(R.string.request_sms_code);
            getSmsCodeButton.setEnabled(true);
            return;
        }
        getSmsCodeButton.setEnabled(false);
        String timerStr = getString(R.string.request_sms_code_agian,String.valueOf(second));
        getSmsCodeButton.setText(timerStr);
    }


}

