package tech.xiaosuo.com.phontomanager.interfaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import tech.xiaosuo.com.phontomanager.R;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_base);
/*        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
    }

    /**
     * show the error code from Bmob web server.
     * @param err_code
     */
    protected void showErrorCode(int err_code){
        BmobInterface.showError(this,err_code);
    }

    /**
     * show the replacePhoneNumberDialog result.
     * @param result
     * @param err_code  :from Bmob web server
     */
    protected void replacePhoneNumberDialog(boolean result,int err_code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(result){
            builder.setTitle(R.string.replace_phonenumber).setMessage(R.string.replace_phone_number_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }else{
            BmobInterface.showError(this,err_code);
           /* builder.setTitle(R.string.replace_phonenumber).setMessage(R.string.replace_phone_number_fail).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });*/
        }

    }

    /**
     * show the result dialog for reset password
     * @param result
     * @param err_code from Bmob web server
     */
    public void resetPasswordDialog(boolean result,int err_code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(result == true){
            builder.setTitle(R.string.reset_password).setMessage(R.string.reset_pwd_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }else{
            BmobInterface.showError(this,err_code);
/*            builder.setTitle(R.string.reset_password).setMessage(R.string.reset_pwd_fail).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });*/
        }
    }




}
