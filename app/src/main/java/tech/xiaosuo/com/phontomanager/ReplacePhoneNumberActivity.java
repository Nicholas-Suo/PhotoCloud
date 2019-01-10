package tech.xiaosuo.com.phontomanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import tech.xiaosuo.com.phontomanager.bean.UserInfo;
import tech.xiaosuo.com.phontomanager.interfaces.BaseActivity;
import tech.xiaosuo.com.phontomanager.interfaces.BmobInterface;
import tech.xiaosuo.com.phontomanager.tools.Utils;

public class ReplacePhoneNumberActivity extends BaseActivity implements View.OnClickListener {// ,BmobInterface.CallBackPresenter

    private static final String TAG="ReplacePhoneNumber";
    TextView currNumberView;
    String currPhoneNumber;
    EditText newNumberView;
    Button reqSmsCodeButton;
    EditText smsCodeView;
    Button submitButton;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replace_phone_number);
        mContext = getApplicationContext();
     //   BmobInterface.setCallBackPresenter(this);
        initView();
    }

    /**
     *
     */
    private void initView(){
        //curr login user'phone
        currNumberView = (TextView)findViewById(R.id.replace_pnum_curr_num_view);
        currPhoneNumber = Utils.getCurrUserPhoneNumber();
        String starPhoneNumber = Utils.modifyPhoneMiddleNumberUsingStar(currPhoneNumber);
        currNumberView.setText(starPhoneNumber);


        newNumberView = (EditText)findViewById(R.id.replace_pnum_new_phonenumber_view);
        reqSmsCodeButton = (Button)findViewById(R.id.replace_pnum_request_smscode_button);
        reqSmsCodeButton.setOnClickListener(this);

        smsCodeView = (EditText)findViewById(R.id.replace_pnum_sms_code_view);


        submitButton = (Button)findViewById(R.id.replace_pnum_submit_button);
        submitButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        String newPhoneNumber = newNumberView.getText().toString();
        Log.d(TAG," newPhoneNumber: " + newPhoneNumber);
        switch (id){
            case R.id.replace_pnum_request_smscode_button:
                if(Utils.isValidPhoneNumber(newPhoneNumber)){
                    smsCodeView.requestFocus();
                    BmobInterface.sendSmsCodeRequest(this,newPhoneNumber);
                    timerRequestSmsCodeAgain(Utils.ONE_MINUTE);
                }
                break;
            case R.id.replace_pnum_submit_button:
                Log.d(TAG,"bind/unbind phone number clic ");
                if(smsCodeView != null && !TextUtils.isEmpty(smsCodeView.getText().toString())){
                    Log.d(TAG,"bind/unbind phone number start ");
                    BmobInterface.replacePhoneNumber(this,newPhoneNumber,smsCodeView.getText().toString());
                }
                break;
                default:
                    break;
        }
    }

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
        replacePnumHandler.sendMessageDelayed(msg,1000);
    }

    /**
     * update the reqest sms code button's text
     * @param second
     */
    @SuppressLint("StringFormatInvalid")
    private void updateRequestSmsCodeButtonText(int second){
        if(second <= 0){
            reqSmsCodeButton.setText(R.string.request_sms_code);
            reqSmsCodeButton.setEnabled(true);
            return;
        }
        reqSmsCodeButton.setEnabled(false);
        String timerStr = getString(R.string.request_sms_code_agian,String.valueOf(second));
        reqSmsCodeButton.setText(timerStr);
    }

    Handler replacePnumHandler = new Handler(){
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
/*    @Override
    public void replacePhoneNumberDialog(boolean result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(result){
            builder.setTitle(R.string.replace_phonenumber).setMessage(R.string.replace_phone_number_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
        }else{
            builder.setTitle(R.string.replace_phonenumber).setMessage(R.string.replace_phone_number_fail).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }*/


}
