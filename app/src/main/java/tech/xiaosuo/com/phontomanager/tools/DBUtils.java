package tech.xiaosuo.com.phontomanager.tools;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import tech.xiaosuo.com.phontomanager.bean.ImageInfo;
import tech.xiaosuo.com.phontomanager.bean.PhotoInfoTable;
import tech.xiaosuo.com.phontomanager.bean.UserInfo;
import tech.xiaosuo.com.phontomanager.interfaces.UIPresenter;


/**
 * Created by wangshumin on 2017/11/13.
 */

public class DBUtils {
    private static final String TAG = "DBUtils";
    public static Uri IMG_CONTENT_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    static String[] IMG_COLUMNS = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.SIZE
            , MediaStore.Images.ImageColumns.MIME_TYPE, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATA};
/* scan photos in phone,and make imageinfo list*/
    public static List<ImageInfo> scanImageFromPhone(Context context, Handler mHandler){
        if(context == null){
            Log.d(TAG," scanImageFromPhone,the context is null");
           return  null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        String where = MediaStore.Images.ImageColumns.MIME_TYPE + " =? or " + MediaStore.Images.ImageColumns.MIME_TYPE + "=?";
        String[] selectionArgs = new String[]{"image/jpeg","image/png"};
        try{
             cursor = MediaStore.Images.Media.query(contentResolver,IMG_CONTENT_URI,IMG_COLUMNS,where,selectionArgs,MediaStore.Images.Media.DATE_MODIFIED);
            //cursor = MediaStore.Images.Media.query(contentResolver,IMG_CONTENT_URI,IMG_COLUMNS,null,null,MediaStore.Images.Media.DATE_MODIFIED);
        }catch (SecurityException se){
/*            try {
                if(mCallback != null){
                    mCallback.confirmPermissionsDialog(Utils.MSG_PLS_CHECK_PERMISSION);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
           if(mHandler != null){
               Message msg = new Message();
               msg.what = UIPresenter.MSG_PLS_CHECK_PERMISSION;
               mHandler.sendMessage(msg);
           }
            Log.d(TAG," scanImageFromPhone,has no read/write extarnal storage permission");
            se.printStackTrace();
            return null;
        }
        if(cursor == null){
/*            Log.d(TAG," the img is null");
            try {
                mCallback.imagesIsEmpty();
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
            return null;
        }

        List<ImageInfo> imgData = new ArrayList<ImageInfo>();
      //  imgData.clear();
        Log.d(TAG," scanImageFromPhone,begin: " +  cursor.getCount());
        while(cursor.moveToNext()){
            ImageInfo imgInfo = new ImageInfo();
            Log.d(TAG," scanImageFromPhone,the mediastore image media has data.");
/*            int idIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID);
            long id = cursor.getLong(idIndex);
            imgInfo.setImageId(id);*/
            int displayNameIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME);
            String displayName = cursor.getString(displayNameIndex);
            imgInfo.setDisplayName(displayName);
            int sizeIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE);
            Long size = cursor.getLong(sizeIndex);
            imgInfo.setImgSize(size);
            int mimeTypeIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE);
            String mimeType = cursor.getString(mimeTypeIndex);
            imgInfo.setMimeType(mimeType);
            int bucketNameIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
            String bucketName = cursor.getString(bucketNameIndex);
            imgInfo.setBucketName(bucketName);
            int dataIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String data = cursor.getString(dataIndex);
            imgInfo.setData(data);

            File  picFile = Utils.getPhoneImageFile(imgInfo);
            if(!picFile.exists()){
                Log.d(TAG," picFile file not exist.contine..." + picFile.getAbsolutePath());
                continue;
            }else{
                String fileMd5 = Utils.getFileMD5(picFile);
                Log.d(TAG," picFile fileMd5: " + fileMd5);
                imgInfo.setMd5(fileMd5);
            }
            BmobFile file = new BmobFile(picFile);//the prcFile must exist.
            file.obtain(displayName,"","");
            imgInfo.setFile(file);
            UserInfo bmobUser = UserInfo.getCurrentUser(UserInfo.class);
            imgInfo.setUserInfo(bmobUser);
            imgData.add(imgInfo);
            File sdCard = Environment.getExternalStorageDirectory();
            int beginIndex = sdCard.getAbsolutePath().length();
            int endIndex = data.lastIndexOf("/");
            String dir = data.substring(beginIndex,endIndex);
            Log.d(TAG," the img name: " + displayName + " , size: " + size + "  ,mimeType: " + mimeType + " ,bucketName: " + bucketName + " data: " + data + " dir: " + dir);
        }
        Log.d(TAG," scanImageFromPhone,end");
        Collections.reverse(imgData);
        return imgData;
    }
/**
 *  after restore image from web server,
 *  we will update the device's image db info.
 */

   public static Uri insertRestoreImageInfoToDb(Context context, ImageInfo imageInfo){
       ContentResolver resolver = context.getContentResolver();
       ContentValues values = new ContentValues();
       values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME,imageInfo.getDisplayName());
       values.put(MediaStore.Images.ImageColumns.SIZE,imageInfo.getImgSize());
       values.put(MediaStore.Images.ImageColumns.MIME_TYPE,imageInfo.getMimeType());
       values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,imageInfo.getBucketName());
       values.put(MediaStore.Images.ImageColumns.DATA,imageInfo.getData());
       Uri uri = resolver.insert(IMG_CONTENT_URI,values);
       Log.d(TAG," the insert uri: " + uri.toString());
       return uri;
   }

    /**
     * query the system image db, whether the image has existed in phone
     * @param context
     * @param imageInfo
     * @return
     */
    public static boolean isExistInSysImageDb(Context context, ImageInfo imageInfo){
        boolean result = false;
        ContentResolver resolver = context.getContentResolver();

        String[] args = new String[]{imageInfo.getData(),String.valueOf(imageInfo.getImgSize())};
        String selection = MediaStore.Images.ImageColumns.DATA + " =? " + " and " + MediaStore.Images.ImageColumns.SIZE + " =? ";
        Cursor cursor = resolver.query(IMG_CONTENT_URI,null,selection,args,null);
        if(cursor != null && cursor.getCount() > 0){
            result = true;
        }else{
            result = false;
        }
        Log.d(TAG," isExistInSysImageDb result: " + result);
        return result;
    }


    /**
     * check whether the photo info has inserted into the db table.photoinfo
     * @param sqLiteDatabase
     * @param md5
     * @return
     */
   public static boolean isExistInDb(SQLiteDatabase sqLiteDatabase,String md5,String data){

       String selection = PhotoInfoTable.COLUMN_IMAGE_MD5 + " =? and " + PhotoInfoTable.COLUMN_IMAGE_DATA + " =? ";
       Cursor cursor = sqLiteDatabase.query(PhotoInfoTable.PHOTO_INFO_TABLE,new String[]{PhotoInfoTable.COLUMN_IMAGE_MD5},selection,new String[]{md5,data},null,null,null);
       if(cursor != null && cursor.getCount() > 0){

           return true;
       }
       return false;
   }


    /**
     *
     * @param sqLiteDatabase
     * @param imageInfo
     */
    public static void syncDataToDatabase(SQLiteDatabase sqLiteDatabase,ImageInfo imageInfo){
        if(imageInfo == null || sqLiteDatabase == null){
            return;
        }

        if(isExistInDb(sqLiteDatabase,imageInfo.getMd5(),imageInfo.getData())){
            Log.d(TAG," the data has exist in database ,ignore it. return");
            return;
        }
        ContentValues values = new ContentValues();
        values.put(PhotoInfoTable.COLUMN_IMAGE_NAME,imageInfo.getDisplayName());
        values.put(PhotoInfoTable.COLUMN_IMAGE_DATA,imageInfo.getData());
        values.put(PhotoInfoTable.COLUMN_IMAGE_MD5,imageInfo.getMd5());
        values.put(PhotoInfoTable.COLUMN_IMAGE_IS_IN_CLOUD,1);
        long id = sqLiteDatabase.insert(PhotoInfoTable.PHOTO_INFO_TABLE,null,values);
        Log.d(TAG," syncDataToDatabase to phone database,the id is: " + id);
    }

    /**
     * remove data from database
     * @param sqLiteDatabase
     * @param imageInfo
     */
    public static void deleteDataFromDatabase(SQLiteDatabase sqLiteDatabase,ImageInfo imageInfo){
        if(sqLiteDatabase == null || imageInfo == null){
              return;
        }
        String where = PhotoInfoTable.COLUMN_IMAGE_MD5 + " =? and " + PhotoInfoTable.COLUMN_IMAGE_DATA + " =? ";
        int id = sqLiteDatabase.delete(PhotoInfoTable.PHOTO_INFO_TABLE,where,new String[]{imageInfo.getMd5(),imageInfo.getData()});
        Log.d(TAG," deleteDataFromDatabase to phone database,the id is: " + id);
    }
}
