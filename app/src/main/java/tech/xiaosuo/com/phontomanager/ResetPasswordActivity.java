package tech.xiaosuo.com.phontomanager;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import tech.xiaosuo.com.phontomanager.bean.UserInfo;
import tech.xiaosuo.com.phontomanager.tools.BmobInterface;
import tech.xiaosuo.com.phontomanager.tools.Utils;


public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener , BmobInterface.CallBackPresenter{

    // UI references.
    private static final String TAG = "ResetPasswordActivity";
    private EditText mNewPasswordView;
    private EditText mConfirmPasswordView;
    private TextView mPhoneNumberView;
    private EditText mSmsCodeView;
    private Button mGetSmsCodeButton;
    private String mUserPhoneNumber;
    UserInfo mBmobUser;
    Context mContext;
/*    private static final int REFRESH_SEND_SMS_CODE_TIMER = 1;
    private static final int ONE_MINUTE = 60;*/
    private ProgressDialog progressDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        BmobInterface.setCallBackPresenter(this);
        mContext = getApplicationContext();
        mBmobUser  = UserInfo.getCurrentUser(UserInfo.class);
        mUserPhoneNumber = mBmobUser.getMobilePhoneNumber();
        // Set up the login form.
        mPhoneNumberView = (TextView)findViewById(R.id.reset_pwd_phone_number);
        mPhoneNumberView.setText(mUserPhoneNumber);

        mNewPasswordView = (EditText) findViewById(R.id.new_password);
   /*     mNewPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                   // submitNewPassword();
                    return true;
                }
                return false;
            }
        });*/

        mConfirmPasswordView = (EditText) findViewById(R.id.comfirm_password);
        mSmsCodeView = (EditText)findViewById(R.id.reset_pwd_sms_code);
        mGetSmsCodeButton = (Button)findViewById(R.id.get_reset_pwd_sms_code);
        mGetSmsCodeButton.setOnClickListener(this);
        Button submitButton = (Button) findViewById(R.id.reset_pwd_submit_button);
        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                submitNewPassword();
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void submitNewPassword() {
        // Reset errors.

        mNewPasswordView.setError(null);
        mConfirmPasswordView.setError(null);
        mSmsCodeView.setError(null);

        // Store values at the time of the login attempt.

        String newPassword = mNewPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();
        String smsCode = mSmsCodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(newPassword) && !isPasswordValid(newPassword)) {
            mNewPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mNewPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(confirmPassword) && !isPasswordValid(confirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        }
        if(!newPassword.equals(confirmPassword)){
            mConfirmPasswordView.setError(getString(R.string.password_not_equal));
            focusView = mConfirmPasswordView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(smsCode)) {
            mSmsCodeView.setError(getString(R.string.error_field_required));
            focusView = mSmsCodeView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
           // showProgress(true);
            BmobInterface.resetPassword(smsCode,newPassword);
        }
    }



    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 6;
    }


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
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.get_reset_pwd_sms_code:
                BmobInterface.sendSmsCodeRequest(mContext,mUserPhoneNumber);
                timerRequestSmsCodeAgain(Utils.ONE_MINUTE);
                break;
             default:
                break;
        }
    }

    @Override
    public void resetPassword(boolean result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
         if(result == true){
             builder.setTitle(R.string.reset_password).setMessage(R.string.reset_pwd_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                     finish();
                 }
             });
         }else{
             builder.setTitle(R.string.reset_password).setMessage(R.string.reset_pwd_fail).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                 }
             });
         }
         AlertDialog dialog = builder.create();
        dialog.show();
    }


   Handler resetHandler = new Handler(){
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
        resetHandler.sendMessageDelayed(msg,1000);
    }

    /**
     * update the reqest sms code button's text
     * @param second
     */
    @SuppressLint("StringFormatInvalid")
    private void updateRequestSmsCodeButtonText(int second){
        if(second <= 0){
            mGetSmsCodeButton.setText(R.string.request_sms_code);
            mGetSmsCodeButton.setEnabled(true);
            return;
        }
        mGetSmsCodeButton.setEnabled(false);
        String timerStr = getString(R.string.request_sms_code_agian,String.valueOf(second));
        mGetSmsCodeButton.setText(timerStr);
    }

}

