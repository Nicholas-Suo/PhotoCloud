package tech.xiaosuo.com.phontomanager.tools;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import tech.xiaosuo.com.phontomanager.PhotoRecylerAdapter;
import tech.xiaosuo.com.phontomanager.PreviewPhotoActivity;
import tech.xiaosuo.com.phontomanager.bean.ImageInfo;
import tech.xiaosuo.com.phontomanager.bean.UserInfo;


/**
 * Created by wangshumin on 2017/11/9.
 */

public class Utils {

    private static final String TAG = "Utils";
    public static final String LINE_BEGIN = "--";
    public static final String LINE_END = "\r\n";
    public static final String SINGLE_QUOT = "\"";

/*    public static final int MSG_IMG_LIST_IS_EMPERTY = 0;
    public static final int MSG_UPDATE_NOTIFICATION = 1;
    public static final int MSG_NETWORK_IS_NOT_CONNECT = 2;
    public static final int MSG_BACKUP_IMG_FAIL = 3;
    public static final int MSG_PLS_CHECK_PERMISSION = 4;
    public static final int MSG_NO_NEED_BACKUP_OR_RESTORE = 5;
    public static final int MSG_SHOW_SUCCESS = 6;
    public static final int MSG_UPLOAD_FINISH = 7;*/
    //network exception status;
    public static final int CONNECT_EXCEPTION = 0;
    public static final  int MALFORMS_URL_EXCEPTION = 1;//MalformedURLException
    public static final int PARAMS_IS_NULL= 2;
    public static final int SUCEESS = 3;
    public static final int FAIL = 4;
    public static final int USER_INVALID = 5;
    public static final int FILE_NOT_FOUND = 6;

    public static final int IS_RESTORE = 0;
    public static final int IS_BACKPU = 1;

    public static final String USER_COOKIE = "user_cookie";
    public static final String COOKIE = "cookie";

    public static final String ACTION_LOGIN = "com.backup.login";
    public static  final String IMG_EXIST = "img_exist";
    public static final String ACTION_START_ANIMATOR = "action.start.animator";
    public static final String ACTION_END_ANIMATOR = "action.end.animator";
    public static final String ACTION_UPLOAD_ONE_PHOTO_SUCCESS = "action.upload.one.photo.success";
    public static final String ACTION_UPLOAD_MULTI_PHOTOS_SUCCESS = "action.upload.multi.photos.success";
    public static final String ACTION_UNSELECT_ALL = "action.unselect_all";
    public static final int INSERT_ONE_PHOTO = -1;
    public static final String CURRENT_POSITION = "curr_position";

    public static final String PHOTO_URL_KEY = "photo_url_key";
    public static final String PHOTO_IMAGEINFO_KEY = "photo_imageinfo_key";
    public static final String PHOTO_IMAGEINFO_POSITION_KEY ="photo_imageinfo_position";
    public static final int PREVIEW_CLOUD_PHOTO_REQUEST_CODE = 1;
//check sync data whether is form webserver.
    public static final int SYNC_FROM_SERVER = -1;
    public static boolean isNetworkConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
    public static boolean isMobileDataConnected(Context context){
           boolean result = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
         if(networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
             result = true;
         }
          return result;
    }

    public static boolean isWifiConnected(Context context){
        boolean result = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            result = true;
        }
        return result;
    }
    /* save the session id*/
    public static void saveCookiePreferences(Context context,String cookie){
        if(context == null){
            Log.d(TAG," wangsm the contxt is null retrun");
              return;
        }
        SharedPreferences preferences = context.getSharedPreferences(USER_COOKIE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(COOKIE,cookie);
        editor.commit();
    }
    /*get the session id*/
    public static String getCookiePreferences(Context context){
        if(context == null){
            Log.d(TAG," wangsm the contxt is null retrun");
            return null;
        }

        SharedPreferences preferences = context.getSharedPreferences(USER_COOKIE, Context.MODE_PRIVATE);
        String sessionid = preferences.getString(COOKIE,null);

        return  sessionid;
    }
    /*
    * get file md5 str
    * */
    public static String getFileMD5(File file){
        String md5Str = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            md5Str = new String(Hex.encodeHex(DigestUtils.md5(fileInputStream)));
           // md5Str = DigestUtils.md5Hex(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return md5Str;
    }

    /**
     * get str md5
     */
    public static String getStringMd5(String path){
          // MessageDigest messageDigest = MessageDigest.getInstance("MD5");
         String md5 = null;
         byte[] byteArray = DigestUtils.md5(path);
         md5 =new String(Hex.encodeHex(byteArray));
        Log.d(TAG," the  md5 is: " + md5);
         return md5;
    }


    /*
    * get the file md5 ,for compare whether two files is different.
    * */
    public static String getFileMD5Str(File file){
         FileInputStream fileInputStream = null;
         String md5Str =null;
        try {
            MessageDigest mdigest = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = fileInputStream.read(buffer)) > 0){
                mdigest.update(buffer,0,len);
            }
            byte[] data = mdigest.digest();
            md5Str = new BigInteger(1,data).toString(16);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return md5Str;
    }

/*
*  perhaps the function has some problem,do not use it pls.
*  suggest use getFileMD5Str(File file)
 */
    public static String getMD5Str(File file){
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        MessageDigest messageDigest = null;
        String md5Str = null;
        try{
             messageDigest = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            digestInputStream = new DigestInputStream(fileInputStream,messageDigest);
            byte[] buffer = new byte[521*1024];
            while (digestInputStream.read(buffer) > 0);
            MessageDigest msgDigest = digestInputStream.getMessageDigest();
            byte[] digestBuffer =  msgDigest.digest();
            md5Str = digestBufferTo32BitStr(digestBuffer);

        }catch (NoSuchAlgorithmException e){
            Log.d(TAG," wangsm getMD5Str NoSuchAlgorithmException ");
            e.printStackTrace();
        }catch(FileNotFoundException e){
            Log.d(TAG," wangsm getMD5Str FileNotException ");
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG," wangsm getMD5Str IOException ");
        }

        return md5Str;
    }

    public static String getMD5Str(InputStream inputStream){
        DigestInputStream digestInputStream = null;
        MessageDigest messageDigest = null;
        String md5Str = null;
        try{
            messageDigest = MessageDigest.getInstance("MD5");
            digestInputStream = new DigestInputStream(inputStream,messageDigest);
            byte[] buffer = new byte[521*1024];
            while (digestInputStream.read(buffer) > 0);
            MessageDigest msgDigest = digestInputStream.getMessageDigest();
            byte[] digestBuffer =  msgDigest.digest();
            md5Str = digestBufferToStr(digestBuffer);

        }catch (NoSuchAlgorithmException e){
            Log.d(TAG," wangsm getMD5Str NoSuchAlgorithmException ");
            e.printStackTrace();
        }catch(FileNotFoundException e){
            Log.d(TAG," wangsm getMD5Str FileNotException ");
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG," wangsm getMD5Str IOException ");
        }

        return md5Str;
    }

    private static  String digestBufferToStr(byte[] buffer){

           if(buffer == null){
               Log.d(TAG," wangsm digestBufferToStr buffer is null ");
              return null;
           }

           StringBuilder sb = new StringBuilder();
           for(int i=0; i<buffer.length; i++){
               sb.append(Integer.toHexString(buffer[i]));
           }
           return sb.toString();
    }

    private static  String digestBufferTo32BitStr(byte[] buffer){

        if(buffer == null){
            Log.d(TAG," wangsm digestBufferTo32BitStr buffer is null ");
            return null;
        }
        char hex[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        StringBuilder sb = new StringBuilder();
        Log.d(TAG," wangsm digestBufferTo32BitStr buffer.length " + buffer.length);
        for(int i=0; i<buffer.length; i++){
            byte value = buffer[i];
            int temp = value>>> 4 & 0XF;
          //  Log.d(TAG," wangsm digestBufferTo16Str temp = " + temp);
            sb.append(hex[temp]);
            int temp1 = value&0XF;
            sb.append(hex[temp1]);
        }
        return sb.toString();
    }

    public static File getPhoneImageFile(ImageInfo imageInfo){
        File sdCard  = Environment.getExternalStorageDirectory();

        //Log.d(TAG," sdCard path " + sdCard.getAbsolutePath());
        // /storage/emulated/0/0/test/test.jpg
        // /storage/emulated/0/Pictures/chihuahua2-1.JPG
        // /storage/emulated/0/0/test.jpg
        String fileName = imageInfo.getDisplayName();
        String data = imageInfo.getData();

        int endIndex = data.lastIndexOf("/");
        String dir = data.substring(0,endIndex);
        boolean isSdcardRoot = false;
        isSdcardRoot = sdCard.getAbsolutePath().equals(dir);

        Log.d(TAG," wangsm getPhoneImageFile -> the  dir is: " + dir);
        // File file = new File(sdCard + "/tupian/test1.jpg");
        File file = null;
        if(isSdcardRoot){
            file = new File(sdCard + File.separator + fileName);
            Log.d(TAG," wangsm getPhoneImageFile -> is sdcard root: " );
        }else{
            int beginIndex = sdCard.getAbsolutePath().length();
            dir = data.substring(beginIndex,endIndex);
            Log.d(TAG," getPhoneImageFile -> the  dir is: " + dir);
            file = new File(sdCard + dir + File.separator + fileName);
        }
        if(file.exists()){
            Log.d(TAG," getPhoneImageFile -> file exist: " + fileName );
            //  return file;
        }else{
            Log.d(TAG,"  getPhoneImageFile -> file not exist: " + fileName );
            //  return null;
        }
        return  file;
    }

    /*
*    rotate animator view
*
* */
    public static ObjectAnimator  rotatePhotoImageViewAnimator(ImageView photoView, float beginDegree, float endDegree){
        ObjectAnimator backUpAnimator = ObjectAnimator.ofFloat(photoView,"rotation",beginDegree,endDegree);
        backUpAnimator.setDuration(1000);
        backUpAnimator.setInterpolator(new LinearInterpolator());
        backUpAnimator.setRepeatCount(ValueAnimator.INFINITE);
        return backUpAnimator;
    }


/*    *//**
     *  if upload success, we weill update the selected map value,detail to see PhotoRecylerAdapter
     *  because, if the holderView is recycler use,the animator is null,can not set checkbox enable true or false, the UI is not refresh when pull back to check UI.
     *  so,wei need update the selected map--->checkBoxStatusMap
     * @param position, the key in map.
     *//*
    public static  void removeKeyFromSelectedMap(int position,PhotoRecylerAdapter adapter){
        Log.d(TAG," removeKeyFromSelectedMap ,the position is: " + position);
        if(position != -1 && adapter!= null && adapter.getCheckBoxStatusMap().containsKey(position)){//need remove the key ,value if success upload.
            adapter.getCheckBoxStatusMap().remove(position);
            Log.d(TAG," removeKeyFromSelectedMap ,remove ok");
        }
    }*/


}
