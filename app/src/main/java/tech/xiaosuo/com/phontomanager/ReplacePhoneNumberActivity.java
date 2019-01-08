package tech.xiaosuo.com.phontomanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import tech.xiaosuo.com.phontomanager.bean.UserInfo;
import tech.xiaosuo.com.phontomanager.interfaces.BmobInterface;
import tech.xiaosuo.com.phontomanager.tools.Utils;

public class ReplacePhoneNumberActivity extends AppCompatActivity implements View.OnClickListener ,BmobInterface.CallBackPresenter{

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
        BmobInterface.setCallBackPresenter(this);
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
                    BmobInterface.sendSmsCodeRequest(mContext,newPhoneNumber);
                }
                break;
            case R.id.replace_pnum_submit_button:
                Log.d(TAG,"bind/unbind phone number clic ");
                if(smsCodeView != null && !TextUtils.isEmpty(smsCodeView.getText().toString())){
                    Log.d(TAG,"bind/unbind phone number start ");
                    BmobInterface.replacePhoneNumber(mContext,newPhoneNumber,smsCodeView.getText().toString());
                }
                break;
                default:
                    break;
        }
    }

    @Override
    public void resetPasswordDialog(boolean result) {

    }

    @Override
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
    }


}
