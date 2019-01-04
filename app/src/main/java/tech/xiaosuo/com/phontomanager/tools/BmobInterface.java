package tech.xiaosuo.com.phontomanager.tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import tech.xiaosuo.com.phontomanager.R;

public class BmobInterface {

    private static final String TAG = "BmobInterface";
    static CallBackPresenter mCallBackPresenter;


    public static void setCallBackPresenter(CallBackPresenter callBackPresenter) {
        mCallBackPresenter = callBackPresenter;
    }

    /**
     * send sms code request to Bmob server
     * @param phone
     */
    public static void sendSmsCodeRequest(final Context context,final String phone){
        /**
         * TODO template 如果是自定义短信模板，此处替换为你在控制台设置的自定义短信模板名称；如果没有对应的自定义短信模板，则使用默认短信模板，默认模板名称为空字符串""。
         *
         * TODO 应用名称以及自定义短信内容，请使用不会被和谐的文字，防止发送短信验证码失败。
         *
         */

        if(phone == null){
            Log.d(TAG," sendSmsCodeRequest fail,params is null,return ");
            return ;
        }
        BmobSMS.requestSMSCode(phone, "相册云短信模板", new QueryListener<Integer>() {
            @Override
            public void done(Integer smsId, BmobException e) {
                if (e == null) {
                    Log.d(TAG,"send sms code request success " + smsId + "\n");
                    Toast.makeText(context,context.getResources().getString(R.string.sms_code_has_send_to) + phone,Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG,"send sms code request fail " + e.getErrorCode() + "-" + e.getMessage());
                    Toast.makeText(context,context.getResources().getString(R.string.sms_code_request_fail) + e.getErrorCode() + "-" + e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * send sms code request to bmob server
     *
     * @param smsCode
     * @param newPassword
     */
    public static void resetPassword(String smsCode,String newPassword){

        if(smsCode == null || newPassword == null){
            Log.d(TAG,"reset password fail,params is null,return ");
            return ;
        }

        BmobUser.resetPasswordBySMSCode(smsCode, newPassword, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                   // mTvInfo.append("重置成功");
                    Log.d(TAG,"reset password success ");
                  //  Toast.makeText(context,context.getResources().getString(R.string.reset_pwd_success),Toast.LENGTH_SHORT).show();
                    if(mCallBackPresenter != null){
                        mCallBackPresenter.resetPassword(true);
                    }
                } else {
                    Log.d(TAG,"reset password fail " + e.getErrorCode() + "-" + e.getMessage() );
                  //  Toast.makeText(context,context.getResources().getString(R.string.reset_pwd_fail) + e.getErrorCode() + "-" + e.getMessage() ,Toast.LENGTH_SHORT).show();
                    if(mCallBackPresenter != null){
                        mCallBackPresenter.resetPassword(false);
                    }
                }
            }
        });
    }


    public interface CallBackPresenter{
         public void resetPassword(boolean result);
    }
}
