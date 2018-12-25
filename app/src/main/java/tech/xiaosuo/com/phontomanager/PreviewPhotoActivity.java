package tech.xiaosuo.com.phontomanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.UpdateListener;
import tech.xiaosuo.com.phontomanager.bean.ImageInfo;
import tech.xiaosuo.com.phontomanager.database.DataBaseHelper;
import tech.xiaosuo.com.phontomanager.tools.DBUtils;
import tech.xiaosuo.com.phontomanager.tools.Utils;

public class PreviewPhotoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PreviewPhotoActivity";
    ImageView preImageView;
    ImageView preDownloadView;
    ImageView preDeleteView;
    ImageInfo preImageInfo = null;
    Context mContext;
    DataBaseHelper mDatabaseHelper;
    int prePosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_photo);
        mContext = getBaseContext();
        mDatabaseHelper = DataBaseHelper.getDbHelperInstance(mContext);//new DataBaseHelper(mContext,null,null,DataBaseHelper.VERSION);
        preImageView = (ImageView)findViewById(R.id.preview_photo_view);
        preDownloadView = (ImageView)findViewById(R.id.preview_download_cloud_photo);
        preDownloadView.setOnClickListener(this);
        preDeleteView =  (ImageView)findViewById(R.id.preview_delete_cloud_photo);
        preDeleteView.setOnClickListener(this);
        Intent intent = getIntent();
        if(intent != null){
           Bundle bundle = intent.getExtras();
           preImageInfo = (ImageInfo)bundle.getSerializable(Utils.PHOTO_IMAGEINFO_KEY);
           if(preImageInfo == null){
               Log.d(TAG," the preImageInfo is null from inten.");
              return;
           }
            prePosition = bundle.getInt(Utils.PHOTO_IMAGEINFO_POSITION_KEY,-1);
            String url = preImageInfo.getFile().getFileUrl(); //(String)bundle.get(Utils.PHOTO_URL_KEY);
            Glide.with(getApplicationContext()).load(url).error(R.mipmap.ic_launcher).into(preImageView);//.placeholder(R.mipmap.ic_launcher)
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.preview_download_cloud_photo:
                Log.d(TAG," preview phonto begin download");
                downloadFile(preImageInfo);
                break;
            case R.id.preview_delete_cloud_photo:
                Log.d(TAG," preview phonto begin delete");
                deleteOnePhotoInCloud(preImageInfo);
                break;
                default:
                    break;
        }
    }

    /**
     * delete the item data from table imageinfo.
     * @param imageInfo
     */
    private void deleteDataInCloud(ImageInfo imageInfo){
        if(imageInfo == null){
            return;
        }

        imageInfo.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                    Log.d(TAG," the item data in imageinfo table  delete success");
                   // Toast.makeText(mContext,R.string.delet_success,Toast.LENGTH_SHORT).show();
                    Intent backIntent = new Intent();
                    backIntent.putExtra(Utils.PHOTO_IMAGEINFO_POSITION_KEY,prePosition);
                    setResult(Utils.PREVIEW_DELETE_SUCCESS,backIntent);
                    finish();
                }else{
                    Log.d(TAG," the item data in imageinfo table  delete fail "+e.getMessage()+","+e.getErrorCode());
                    String msg = "the item data in imageinfo table  delete fail "+e.getMessage()+","+e.getErrorCode();
                    Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    /**
     * down load  the photo from server.
     * @param imageInfo
     */
    private void downloadFile(final ImageInfo imageInfo){
        //允许设置下载文件的存储路径，默认下载文件的目录为：context.getApplicationContext().getCacheDir()+"/bmob/"
        if(imageInfo == null || mContext == null){
            return;
        }
        if(DBUtils.isExistInSysImageDb(mContext,imageInfo)){
            Toast.makeText(mContext,R.string.photo_exist_in_phone,Toast.LENGTH_SHORT).show();
            return;
        }
        File saveFile = Utils.getPhoneImageFile(imageInfo);//new File(imageInfo.getData());
        BmobFile file = imageInfo.getFile();
        file.download(saveFile, new DownloadFileListener() {

            @Override
            public void onStart() {
                Log.d(TAG," downloadFile -> begin download... ");
            }

            @Override
            public void done(String savePath,BmobException e) {
                if(e==null){
                    Log.d(TAG,"  downloadFile -> download success...savePath: " + savePath);
                    DBUtils.insertRestoreImageInfoToDb(mContext,imageInfo);
                    Toast.makeText(mContext,R.string.download_finish,Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Log.d(TAG,"  downloadFile -> download fail...savePath: " +e.getErrorCode()+","+e.getMessage());
                    String msg = "downloadFile -> download fail...savePath: " +e.getErrorCode()+","+e.getMessage();
                    Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onProgress(Integer value, long newworkSpeed) {
                Log.d(TAG," downloadFile -> 下载进度："+value+","+newworkSpeed);
            }
        });
    }

    /**
     * delete the photo file from cloud.
     * @param bmobFile
     */
    private void deletPhotoInCloud(BmobFile bmobFile){
        //BmobFile file = new BmobFile();
        //   file.setUrl(url);//此url是上传文件成功之后通过bmobFile.getUrl()方法获取的。
        if(bmobFile == null){
            Log.d(TAG," the bombFile is null");
            return;
        }
        bmobFile.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                    Log.d(TAG," the bombFile delete success");
                }else{
                    Log.d(TAG," the bombFile delete fail " +e.getErrorCode()+","+e.getMessage());
                }
            }
        });
    }

    /**
     * delete one photo in cloud.
     * @param imageInfo
     */
    private void deleteOnePhotoInCloud(ImageInfo imageInfo){
        if(imageInfo == null || mDatabaseHelper == null || imageInfo.getFile() == null){
            Log.d(TAG," preview photo,delete photo fail,null,return");
            return;
        }
        deletPhotoInCloud(imageInfo.getFile());
        deleteDataInCloud(imageInfo);
        DBUtils.deleteDataFromDatabase(mDatabaseHelper.getWritableDatabase(),imageInfo);
    }
}
