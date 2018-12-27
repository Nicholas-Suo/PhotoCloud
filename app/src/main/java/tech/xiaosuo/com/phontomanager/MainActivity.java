package tech.xiaosuo.com.phontomanager;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TimeUtils;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;
import cn.bmob.v3.listener.UploadFileListener;
import tech.xiaosuo.com.phontomanager.bean.PhotoInfoTable;
import tech.xiaosuo.com.phontomanager.database.DataBaseHelper;
import tech.xiaosuo.com.phontomanager.interfaces.UIPresenter;
import tech.xiaosuo.com.phontomanager.service.PhotoManagerService;
import tech.xiaosuo.com.phontomanager.sync.SyncWorker;
import tech.xiaosuo.com.phontomanager.tools.DBUtils;
import tech.xiaosuo.com.phontomanager.tools.Utils;
import tech.xiaosuo.com.phontomanager.bean.ImageInfo;
import tech.xiaosuo.com.phontomanager.bean.UserInfo;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , PhotoRecylerAdapter.OnRecyclerItemClickListener,View.OnClickListener ,UIPresenter {

    private static final String TAG = "MainActivity";
    private static final int MAX_LIMIT = 30;
    RecyclerView mRecyclerView;
    IntentService aa;
    PhotoRecylerAdapter mRecylerAdapter;
    Context mContext;
    Toolbar toolbar;
    TextView selectedCountView;
    boolean selectAllStatus = false;
    DataBaseHelper mDatabaseHelper;
    UserInfo bmobUser;
    ContentLoadingProgressBar mProgressBar;
    LinearLayout progressLayout;
    PhotoObserver photoObserver;
    PhotoManagerService photoManagerService;
    boolean isBoundService = false;
    MenuItem selectButton;
    FloatingActionButton fab;
    ObjectAnimator uploadAnimator;
    TextView userNameView;
    TextView registerPhoneNumberView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Bmob.initialize(this, "a9155fe2bc2f2ede3b0e28261a93004a");
        bmobUser = UserInfo.getCurrentUser(UserInfo.class);
        if(bmobUser != null){
            // 允许用户使用应用
        }else{
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
        //bind service
        Intent intent = new Intent(this,PhotoManagerService.class);
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mContext = getApplicationContext();
        progressLayout = (LinearLayout) findViewById(R.id.progress_layout);
        mProgressBar = (ContentLoadingProgressBar)findViewById(R.id.loading_progressbar);
        mDatabaseHelper = DataBaseHelper.getDbHelperInstance(mContext);// new DataBaseHelper(mContext,null,null,DataBaseHelper.VERSION);

        mRecyclerView = (RecyclerView)findViewById(R.id.photo_recycler_view);

        PhotoItemViewDecoration decoration = new PhotoItemViewDecoration();
        mRecyclerView.addItemDecoration(decoration);
        mRecylerAdapter = new PhotoRecylerAdapter(mContext,mDatabaseHelper,mRecyclerView);
        mRecylerAdapter.setOnRecyclerItemClickListener(this);
        mRecyclerView.setAdapter(mRecylerAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext,3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    mRecylerAdapter.setScrollFlag(false);
                    mRecylerAdapter.notifyDataSetChanged();
                }else{
                    mRecylerAdapter.setScrollFlag(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });

        selectedCountView = (TextView) findViewById(R.id.select_number);

/*        ScanPhotosAsyncTask scanPhotosAsyncTask = new ScanPhotosAsyncTask(mContext,mRecylerAdapter,mainHandler,mProgressBar);
        scanPhotosAsyncTask.execute();*/

       photoObserver = new PhotoObserver(mContext,mainHandler,mRecylerAdapter);
        getContentResolver().registerContentObserver(DBUtils.IMG_CONTENT_URI,false,photoObserver);
        //register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.ACTION_END_ANIMATOR);
        filter.addAction(Utils.ACTION_UPLOAD_ONE_PHOTO_SUCCESS);
        filter.addAction(Utils.ACTION_START_ANIMATOR);
        filter.addAction(Utils.ACTION_UPLOAD_MULTI_PHOTOS_SUCCESS);
        filter.addAction(Utils.ACTION_UNSELECT_ALL);
        registerReceiver(mBrodcastReceiver,filter);
/*        if(photoManagerService != null){
            Log.d(TAG," service setUiPresenter");
            photoManagerService.setUiPresenter(this);
        }*/

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
/*        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG," begin upload photo ");
                if(mRecylerAdapter!=null && photoManagerService != null && photoManagerService.getUploadStatus() != PhotoManagerService.STATUS_UPLOADING){
                    uploadPhotos();
                }

*//*                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*//*
            }
        });*/
        //initSyncWorkManager();
       // loadDataFromServer();
    }

/*    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toolbar.setTitle(null);
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        loadDataFromServer();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initNavHeaderUserInfo();//for user infor display.
        if(mRecylerAdapter != null){
            mRecylerAdapter.updateSyncStatusMap();
        }
       // initSyncWorkManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDatabaseHelper != null){
            mDatabaseHelper.close();
        }
        if(photoObserver != null){
            getContentResolver().unregisterContentObserver(photoObserver);
        }
        unregisterReceiver(mBrodcastReceiver);

        if(isBoundService){
            unbindService(mConnection);
           isBoundService = false;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        selectButton = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.select_all) {
            if(mRecylerAdapter != null && selectAllStatus == false){
               boolean result = mRecylerAdapter.selectedAll();
                selectAllStatus = result;
                if(result){
                    item.setTitle(R.string.un_select_all);
                }
            }else{
                mRecylerAdapter.unselectedAll();
                selectAllStatus = false;
                item.setTitle(R.string.select_all);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
          Intent intent = new Intent(this,CloudPhotosActivity.class);
          startActivity(intent);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if(id == R.id.logout){
            logOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onRecyclerItemClick(int count,int total) {
        Log.d(TAG," the selected photo counts is: " + count + " ,total is: " +total);
        selectedCountView.setText(String.valueOf(count));
        if(count == total){
            selectAllStatus = true;
            selectButton.setTitle(R.string.un_select_all);
        }else{
            selectAllStatus = false;
            selectButton.setTitle(R.string.select_all);
        }

    }

    /**
     * upload image tu clound
     */
    public void uploadPhotos(){

        HashMap<Integer, ImageInfo>  indexMap = null;//save the select photo and image info.
        HashMap<Integer, Boolean>  checkBoxStatusMap = mRecylerAdapter.getCheckBoxStatusMap();
        List<ImageInfo>  listData = mRecylerAdapter.getListData();

        if(checkBoxStatusMap == null || checkBoxStatusMap.size() == 0 || listData == null || listData.size() == 0){
            Toast.makeText(mContext,R.string.no_select_photo,Toast.LENGTH_SHORT).show();
            fab.setVisibility(View.VISIBLE);
            return;
        }

        Log.d(TAG," begin upload it, uploadPhoto entry checkBoxStatusMap.size() "+ checkBoxStatusMap.size());
        for(Integer position :checkBoxStatusMap.keySet()){
            Log.d(TAG," begin upload it, uploadPhoto entry position: "+ position);
            if(indexMap == null){
                indexMap = new  HashMap<Integer, ImageInfo>();
            }
            ImageInfo imageInfo = listData.get(position);
            indexMap.put(position,imageInfo);
/*                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if(holder instanceof PhotoRecylerAdapter.PhotoHolder){
                    ImageView imageSyncView = ((PhotoRecylerAdapter.PhotoHolder) holder).getPhotoSyncView();
                    ObjectAnimator animator = Utils.rotatePhotoImageViewAnimator(imageSyncView,0,360);
                    animator.start();
                }*/
                Log.d(TAG," begin upload it, iamgeName is: "+ imageInfo.getDisplayName());
        }

        Log.d(TAG," begin upload to server");
        uploadFilesToBmobServer(mContext,indexMap);
    }

    /******************************/
    /* upload photo to bmob*/
    private void uploadFilesToBmobServer(Context context,List<ImageInfo> imgList,HashMap<Integer, ImageInfo>  checkBoxStatusMap) {
        int total = 0;
        if(imgList == null){
            Log.d(TAG, " wangsm uploadFilesToBmobServer listis null return:");
            return;
        }
        if(imgList.size() > 0){
            beginUpload(imgList);
        }
    }

    private void uploadFilesToBmobServer(Context context,HashMap<Integer, ImageInfo>  indexMap) {
        int total = 0;
        if(indexMap == null){
            Log.d(TAG, " uploadFilesToBmobServer listis null return:");
            fab.setVisibility(View.VISIBLE);
            return;
        }
        Log.d(TAG, " uploadFilesToBmobServer begin:indexMap.size(): " + indexMap.size());
        if(indexMap.size() > 0){
            total = indexMap.size();
            Log.d(TAG, " uploadFilesToBmobServer entry begin upload img to server, total:" + total);
            if (total == 1) {
                int position = -1;
                ImageInfo imageInfo = null;
                for(Map.Entry<Integer,ImageInfo> entry :indexMap.entrySet() ){
                    position = entry.getKey();
                    imageInfo = entry.getValue();
                }
                Log.d(TAG," The photo position is:" + position);
                isExistInCloudServerForOneFile(bmobUser,imageInfo,position);
              //  uploadOneFile(imageInfo,position);
            } else if(total > 1){
                if(photoManagerService != null){
                    photoManagerService.uploadMultiFiles(mContext,indexMap);
                }
                //uploadMultiFiles(mContext,indexMap);
            }else{
                Log.d(TAG, "  there is not new image,do not backup :" );
                fab.setVisibility(View.VISIBLE);
                Toast.makeText(mContext," there is not new image,no need update",Toast.LENGTH_SHORT).show();
            }
        }
        Log.d(TAG, " uploadFilesToBmobServer end:");
    }


    /*
     *
    upload photo,contains one file and mutifile upload
    *
    * */
   private void beginUpload(List<ImageInfo> imgList){
               int total = imgList.size();
               Log.d(TAG, " begin upload img to server, total:" + total);
               if (total == 1) {
                   uploadOneFile(imgList.get(0),-1);
               } else if(total > 1){
                   uploadMultiFiles(mContext,imgList);
               }else{
                   Log.d(TAG, "  there is not new image,do not backup :" );
                   Toast.makeText(mContext," there is not new image,no need update",Toast.LENGTH_SHORT).show();
               }
   }

    /**
     * only upload file,amd insert user info
     *
     */
    private void uploadOneFile(final ImageInfo imageInfo,final int postion){

        final ImageView imageSyncView;
        final CheckBox checkBox;
        if(imageInfo == null || postion == -1){
            Log.d(TAG," uploadOneFile  imageInfo is null");
            return;
        }

        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(postion);
        if(holder instanceof PhotoRecylerAdapter.PhotoHolder){
            imageSyncView = ((PhotoRecylerAdapter.PhotoHolder) holder).getPhotoSyncView();
            checkBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
            imageSyncView.setVisibility(View.VISIBLE);
            //animator = Utils.rotatePhotoImageViewAnimator(imageSyncView,0,360);
        }else{
            imageSyncView = null;
            checkBox = null;
            Log.d(TAG," uploadOneFile  imageSyncView is null");
            return;
        }
        final ObjectAnimator animator = Utils.rotatePhotoImageViewAnimator(imageSyncView,0,360);
        animator.start();

        File picFile = Utils.getPhoneImageFile(imageInfo);
                                 final BmobFile bmobFile = new BmobFile(picFile);
        bmobFile.uploadblock(new UploadFileListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                    //bmobFile.getFileUrl()--返回的上传文件的完整地址
                    Log.d(TAG," uploadOneFile the picfile upload success :");
                    Log.d(TAG," uploadOneFile the picfile url:" + bmobFile.getFileUrl());
                    // Toast.makeText(context,R.string.back_up_success,Toast.LENGTH_SHORT).show();
                    BmobFile file = new BmobFile(bmobFile.getFilename(),bmobFile.getGroup(),bmobFile.getFileUrl());
                    imageInfo.setFile(file);
                    insertBombObject(imageInfo);
                    Toast.makeText(mContext,"upload success.",Toast.LENGTH_SHORT).show();
                    endAnimator(animator,checkBox,imageSyncView);

                }else{
                     Toast.makeText(mContext,"upload fail.",Toast.LENGTH_SHORT).show();
                    Log.d(TAG," uploadOneFile the pic file upload fail :" + e.getMessage());
                }

            }

            @Override
            public void onProgress(Integer value) {
                // 返回的上传进度（百分比）
                Log.d(TAG," uploadOneFile the picfile upload pecent value :" + value);
/*                if(postion == -1){
                    return;
                }
                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(postion);
                if(holder instanceof PhotoRecylerAdapter.PhotoHolder){
                    imageSyncView = ((PhotoRecylerAdapter.PhotoHolder) holder).getPhotoSyncView();
                    checkBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
                    imageSyncView.setVisibility(View.VISIBLE);
                    animator = Utils.rotatePhotoImageViewAnimator(imageSyncView,0,360);
                    animator.start();
                }*/
            }
        });
    }
    /**
     * only upload file,do not insert user info
     *
     */
    private void uploadOneFile(final Context context,File file){
        if(file == null){
            Log.d(TAG," the  file is null");
            return;
        }
        // String picPath = "sdcard/temp.jpg";
        final BmobFile bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                    //bmobFile.getFileUrl()--返回的上传文件的完整地址
                    Log.d(TAG," the picfile upload success :");
                  /*  try {
                        mCallback.showSuccess(Utils.IS_BACKPU);
                    } catch (RemoteException re) {
                        re.printStackTrace();
                    }*/
                    Log.d(TAG," the picfile url:" + bmobFile.getFileUrl());
                    // Toast.makeText(context,R.string.back_up_success,Toast.LENGTH_SHORT).show();
                }else{
                    // Toast.makeText(context,R.string.back_up_fail,Toast.LENGTH_SHORT).show();
                    Log.d(TAG," the pic file upload fail :" + e.getMessage());
                }

            }

            @Override
            public void onProgress(Integer value) {
                // 返回的上传进度（百分比）
                Log.d(TAG," the picfile upload pecent value :" + value);
               /* try {
                    mCallback.updateNotifaction(value);
                    if(value == 100){
                        //insertBombObject(imageInfo);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }*/
            }
        });
    }


    /**
     *upload photos in activity ,not use photo manager
     * upload multi photoes to bmob.
     * @param context
     * @param indexMap the indexMap contains recycler itemn postion and ImageInfo. we need the postion to get view,then update view.
     */
    private void uploadMultiFiles(final Context context, final HashMap<Integer, ImageInfo>  indexMap){

        final HashMap<String,Integer> pathPositionMap = new HashMap<String,Integer>();
        //final HashMap<Integer, ImageInfo>  newIndexMap;
        List<Integer> positions = new ArrayList<Integer>();
        if(indexMap == null){
            Log.d(TAG," upload muti files fail,indexMap is null");
            return;
        }
        Log.d(TAG," upload muti files begin");
        for(Map.Entry<Integer, ImageInfo> entry : indexMap.entrySet()){
            ImageInfo imageInfo = entry.getValue();
            boolean exist = DBUtils.isExistInDb(mDatabaseHelper.getReadableDatabase(),imageInfo.getMd5(),imageInfo.getData());
            if(exist){
                positions.add(entry.getKey());
            }
        }
        for(int i=0 ;i<positions.size();i++){
            int itemPosition = positions.get(i);
            indexMap.remove(itemPosition);//update the indexmap , for upload the unupload photo
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(itemPosition);
            if(holder instanceof PhotoRecylerAdapter.PhotoHolder){//update the uploaded phohto ui
                CheckBox itemCheckBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
                if(itemCheckBox != null){
                    itemCheckBox.setChecked(false);
                }
            }
           // mRecylerAdapter.removeValueFromMap(i);
        }
        Log.d(TAG," upload muti files,need upload photo count:" + indexMap.size());
        if(indexMap.size() == 0){
            mRecylerAdapter.clearCheckBoxStatusMap();
            mRecylerAdapter.notifyDataSetChanged();
            Toast.makeText(mContext,R.string.pls_select_unuplosd_photo,Toast.LENGTH_SHORT).show();
          return;
        }
        //newIndexMap = indexMap
        // sort hash map
        List<Map.Entry<Integer, ImageInfo>> sortList = new ArrayList<Map.Entry<Integer, ImageInfo>>(indexMap.entrySet());
        Collections.sort(sortList, new Comparator<Map.Entry<Integer, ImageInfo>>() {
            @Override
            public int compare(Map.Entry<Integer, ImageInfo> o1, Map.Entry<Integer, ImageInfo> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        ArrayList<String> arrayList = new ArrayList<String>();
        int total = indexMap.size();
        for(Map.Entry<Integer, ImageInfo> entry : sortList){
            ImageInfo imgInfo =  entry.getValue();
            String path = imgInfo.getData();
            arrayList.add(path);
            pathPositionMap.put(path,entry.getKey());
        }

        final String[] pathArray = new String[total];
        arrayList.toArray(pathArray);

        BmobFile.uploadBatch(pathArray, new UploadBatchListener() {
             ImageView imageSyncView;
             CheckBox checkBox;
             int currentPosition = -1;
             int currentIndex = -1;
             ObjectAnimator animator;
            @Override
            public void onSuccess(List<BmobFile> files,List<String> urls) {
                //1、files-上传完成后的BmobFile集合，是为了方便大家对其上传后的数据进行操作，例如你可以将该文件保存到表中
                //2、urls-上传文件的完整url地址
                // Log.d(TAG," uploadMultiFiles success files.size " + files + " urls size: " + urls.size());
                int index = currentIndex;

                ImageInfo imageInfo = indexMap.get(currentPosition);
                BmobFile file = files.get(index);
                Log.d(TAG," uploadMultiFiles success files.size " + file.getFileUrl() + " url : " + urls.get(index));
                //   file.setUrl(urls.get(index));
                imageInfo.setFile(file);
                insertBombObject(imageInfo);
                Log.d(TAG," upload multi photo success end animator");
                endAnimator(animator,checkBox,imageSyncView);
                removeKeyFromSelectedMap(currentPosition,mRecylerAdapter);
                Log.d(TAG," upload multi photo set sys upload img ok");
                if(urls.size()==pathArray.length){//如果数量相等，则代表文件全部上传完成
                   // mRecylerAdapter.clearCheckBoxStatusMap();//update map ,for uploaded photo ui.need show unselected
                   // mRecylerAdapter.notifyDataSetChanged();
                    Toast.makeText(context," multi upload success ",Toast.LENGTH_SHORT).show();
                    //do something
                    // Log.d(TAG," uploadMultiFiles success ");
                }
            }

            @Override
            public void onError(int statuscode, String errormsg) {
                Log.d(TAG," uploadMultiFiles fail,statuscode:" + statuscode + " errormsg:" + errormsg );
                Toast.makeText(context,statuscode + errormsg,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int curIndex, int curPercent, int total,int totalPercent) {
                //1、curIndex--表示当前第几个文件正在上传
                //2、curPercent--表示当前上传文件的进度值（百分比）
                //3、total--表示总的上传文件数
                //4、totalPercent--表示总的上传进度（百分比）
                currentIndex = curIndex - 1;
                int position = pathPositionMap.get(pathArray[currentIndex]);
                if(currentPosition != position){
                    currentPosition = position;
                    RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(currentPosition);
                    if(holder instanceof PhotoRecylerAdapter.PhotoHolder){
                        imageSyncView = ((PhotoRecylerAdapter.PhotoHolder) holder).getPhotoSyncView();
                        checkBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
                        Log.d(TAG," upload multi photo show animator");
                        imageSyncView.setVisibility(View.VISIBLE);
                        checkBox.setChecked(false);
                        animator = Utils.rotatePhotoImageViewAnimator(imageSyncView,0,360);
                        animator.start();
                    }
                }


                Log.d(TAG," uploadMultiFiles onProgress curPercent: " + curPercent + "  curIndex: " + curIndex + " currentPosition: " + currentPosition + " totalPercent:" + totalPercent + " total " + total);
/*                try {
                    if(curPercent == 100){
                        currentIndex = curIndex;
   *//*                     ImageInfo imageInfo = imgList.get(curIndex-1);
                        insertBombObject(imageInfo);*//*
                        mCallback.updateNotifaction(totalPercent);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }*/
            }
        });
    }



    /**
     *
     * upload multi photo to bmob
     * @param context
     * @param imgList
     */
    private void uploadMultiFiles(final Context context,final List<ImageInfo> imgList){

        if(imgList == null){
            Log.d(TAG," upload muti files fail,imglist is null");
            return;
        }
        Log.d(TAG," upload muti files begin");

        ArrayList<String> arrayList = new ArrayList<String>();
        int total = imgList.size();
        for(int i=0; i < total; i++){
            ImageInfo tempImg = imgList.get(i);
            String path = tempImg.getData();
            arrayList.add(path);
        }
        final String[] pathArray = new String[total];
        arrayList.toArray(pathArray);
        BmobFile.uploadBatch(pathArray, new UploadBatchListener() {
            int currentIndex;
            @Override
            public void onSuccess(List<BmobFile> files,List<String> urls) {
                //1、files-上传完成后的BmobFile集合，是为了方便大家对其上传后的数据进行操作，例如你可以将该文件保存到表中
                //2、urls-上传文件的完整url地址
                // Log.d(TAG," uploadMultiFiles success files.size " + files + " urls size: " + urls.size());
                int index = currentIndex-1;

                ImageInfo imageInfo = imgList.get(index);
                BmobFile file = files.get(index);
                Log.d(TAG," uploadMultiFiles success files.size " + file.getFileUrl() + " url : " + urls.get(index));
                //   file.setUrl(urls.get(index));
                imageInfo.setFile(file);
                insertBombObject(imageInfo);
                if(urls.size()==pathArray.length){//如果数量相等，则代表文件全部上传完成
                    Toast.makeText(context," multi upload success ",Toast.LENGTH_SHORT).show();
                    //do something
                    // Log.d(TAG," uploadMultiFiles success ");
/*                    try {
                        mCallback.showSuccess(Utils.IS_BACKPU);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }*/
                }
            }

            @Override
            public void onError(int statuscode, String errormsg) {
                Log.d(TAG," uploadMultiFiles fail,statuscode:" + statuscode + " errormsg:" + errormsg );
                Toast.makeText(context,statuscode + errormsg,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int curIndex, int curPercent, int total,int totalPercent) {
                //1、curIndex--表示当前第几个文件正在上传
                //2、curPercent--表示当前上传文件的进度值（百分比）
                //3、total--表示总的上传文件数
                //4、totalPercent--表示总的上传进度（百分比）

                Log.d(TAG," uploadMultiFiles onProgress curPercent: " + curPercent + "  curIndex: " + curIndex + " totalPercent:" + totalPercent + " total " + total);
/*                try {
                    if(curPercent == 100){
                        currentIndex = curIndex;
   *//*                     ImageInfo imageInfo = imgList.get(curIndex-1);
                        insertBombObject(imageInfo);*//*
                        mCallback.updateNotifaction(totalPercent);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }*/
            }
        });
    }

    /**
     *
     * insert object to bmob
     * @param obj
     */
    private void insertBombObject(final ImageInfo obj){

        obj.save(new SaveListener<String>() {
            @Override
            public void done(String objectId,BmobException e) {
                if(e==null){
                    Log.d(TAG, " insertBombObject success");
                    String imgName = obj.getDisplayName();
                    String data = obj.getData();
                    String md5 = obj.getMd5();
                    int cloud = 1;
                    SQLiteDatabase sqLiteDatabase = mDatabaseHelper.getWritableDatabase();
                    if(DBUtils.isExistInDb(sqLiteDatabase,md5,data)){
                        Log.d(TAG," insertBombObject ,no not insert data into database,because has exist in db: ");
                       return;
                    }

                    ContentValues values = new ContentValues();
                    values.put(PhotoInfoTable.COLUMN_IMAGE_NAME,imgName);
                    values.put(PhotoInfoTable.COLUMN_IMAGE_DATA,data);
                    values.put(PhotoInfoTable.COLUMN_IMAGE_MD5,md5);
                    values.put(PhotoInfoTable.COLUMN_IMAGE_IS_IN_CLOUD,cloud);
                    long id = sqLiteDatabase.insert(PhotoInfoTable.PHOTO_INFO_TABLE,null,values);
                    Log.d(TAG," insertBombObject to insert phone database,the insert id is: " + id);
                    //  toast("添加数据成功，返回objectId为："+objectId);
                }else{
                    Log.d(TAG, " insertBombObject fail  " + e.getMessage());
                    Toast.makeText(mContext,"insert obj fail," + e.toString(),Toast.LENGTH_SHORT).show();
                    //  toast("创建数据失败：" + e.getMessage());
                }
            }
        });
    }
/************************end bmob**********************/

private Handler mainHandler = new Handler(){
    @Override
    public void handleMessage(Message msg) {
        if(msg == null){
            Log.d(TAG," mainHandler the msg is null");
        }
        int what = msg.what;
        int result = msg.arg1;
        Log.d(TAG," mainHandler result: " + result);
        switch (what){
            case MSG_BACKUP_IMG_FAIL:
                showErrorDialog(what,result);
               // notificationManager.cancel(NOTIFICATION_TAG);
                break;
            case MSG_NO_NEED_BACKUP_OR_RESTORE:
                showErrorDialog(what,result);
                break;
            case MSG_IMG_LIST_IS_EMPERTY:
            case MSG_NETWORK_IS_NOT_CONNECT:
            case MSG_PLS_CHECK_PERMISSION:
                showErrorDialog(what,-1);
                break;
           /* case Utils.MSG_UPDATE_NOTIFICATION:
                int percent = msg.arg1;
                if(builder == null){
                    builder = new NotificationCompat.Builder(mainContext);
                }
                builder.setSmallIcon(R.mipmap.ic_launcher_round);
                builder.setContentTitle(getString(R.string.app_name));
                String percentStr = " :" + percent +"%";
                builder.setContentText(getString(R.string.uploading) + percentStr);

                builder.setProgress(100,percent,false);
                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_ONGOING_EVENT;
                Log.d(TAG," wangsm main activity show notification percent: " + percent);
                if(percent == 100){
                    notificationManager.cancel(NOTIFICATION_TAG);
                    progressbarLL.setVisibility(View.GONE);
                    //showBackUpSuccess();
                }else{
                    notificationManager.notify(NOTIFICATION_TAG,notification);
                    progressbarLL.setVisibility(View.VISIBLE);
                    progressBar.setProgress(percent);
                    progressPercentText.setText(percentStr);
                }

                break;*/
            case MSG_SHOW_SUCCESS:
             //   int isBackup = msg.arg1;
             //   showSuccessDialog(isBackup);
                break;
            case MSG_UPLOAD_FINISH:
                fab.setVisibility(View.VISIBLE);
                break;
            case MSG_RM_KEY_FROM_ADAPTER:
                removeKeyFromSelectedMap(result,mRecylerAdapter);
                break;
            case MSG_UPDATE_CHECKBOX_UNCHECKED:// upload success,we need update the photo's checkbox status to false.
                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(result);
                if(holder instanceof PhotoRecylerAdapter.PhotoHolder){//update the uploaded phohto ui
                    CheckBox itemCheckBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
                    if(itemCheckBox != null){
                        itemCheckBox.setChecked(false);
                    }
                }
                break;
            case MSG_UNSELECT_ALL:
                unSelectAll();
                break;
            default:
                super.handleMessage(msg);
        }

    }
};

    //show wuccess dialog,backup or recovery
    private void showSuccessDialog(int isBackup){
        if(isBackup == 1){
            showBackUpSuccess();
        }else{
            showRecoverSuccess();
        }
    }
    //show back up sucess dialog
    private  void showBackUpSuccess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title).setMessage(R.string.back_up_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
    //show recover sucess dialog
    private  void showRecoverSuccess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title).setMessage(R.string.recover_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
    //show the erro information.
    private void showErrorDialog(int what, int result){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int message = R.string.dialog_title;
        switch(what){
            case MSG_BACKUP_IMG_FAIL:
                if(result == Utils.CONNECT_EXCEPTION){
                    message = R.string.network_conn_exception;
                }else if(result == Utils.FILE_NOT_FOUND){
                    message = R.string.file_not_found;
                }else if(result == Utils.USER_INVALID){
                    message = R.string.user_invalid;
                }
                else{
                    message = R.string.back_up_fail;
                }

                break;
            case MSG_IMG_LIST_IS_EMPERTY:
                message = R.string.img_is_empty;
                break;
            case MSG_NETWORK_IS_NOT_CONNECT:
                message = R.string.network_not_connect;
                break;
            case MSG_PLS_CHECK_PERMISSION:
                message = R.string.read_external_storage_exception;
                break;
            case MSG_NO_NEED_BACKUP_OR_RESTORE:
                if(result == Utils.IS_BACKPU){
                    message = R.string.no_need_backup_error;
                }else if(result == Utils.IS_RESTORE){
                    message = R.string.no_need_recover;
                }
                break;
            default:
                break;
        }
        final int temResult = result;
        builder.setTitle(R.string.dialog_title).setMessage(message).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(temResult == Utils.USER_INVALID){
                    dialog.dismiss();
                    Intent loginIntent = new Intent(Utils.ACTION_LOGIN);
                    startActivity(loginIntent);
                    finish();
                }
            }
        });
        Log.d(TAG," wangsm show error dialog ");
        AlertDialog dialog = builder.show();
    }

    /**
     * check whether the photo exist in clound,if has not ,upload,  else do other things
     * @param userInfo
     * @param imageInfo
     * @param postion
     */
    private  void isExistInCloudServerForOneFile(UserInfo userInfo,final ImageInfo imageInfo,final int postion){
        final  int position = postion;
        BmobQuery<ImageInfo> query = new BmobQuery<ImageInfo>();
        query.addWhereEqualTo("userInfo",userInfo);
        query.addWhereEqualTo("md5",imageInfo.getMd5());
        query.findObjects(new FindListener<ImageInfo>() {
            @Override
            public void done(List<ImageInfo> list, BmobException e) {
                //  Log.d(TAG," find server done:" + list.size());
                if(e == null && (list == null || list.size() == 0)){
                    if(photoManagerService != null){
                        photoManagerService.uploadOneFile(imageInfo,position);
                    }
                    //uploadOneFile(imageInfo,position);
                }else if(e == null){
                    updateItemViewCheckStatus(position);
                    fab.setVisibility(View.VISIBLE);
                    Toast.makeText(mContext,R.string.photo_has_uploaded,Toast.LENGTH_SHORT).show();
                    Log.d(TAG," find server exist:");
                }else{
                    Log.d(TAG," find server exception:"+ e.toString());
                }
            }
        });

    }

    /**
     * check the same photos,then remove it.only upload  not same photo with webserver.
     * @param userInfo
     * @param context
     * @param indexMap
     */
    private  void isExistInCloudServerForMultiFiles(UserInfo userInfo,final Context context,final HashMap<Integer, ImageInfo>  indexMap){
       // uploadMultiFiles(mContext,indexMap);
    }

    /**
     * if the item has been uploaded statsu, when upload it , need show  error,and reset the checkbox statsu ,update the view.
     * @param itemPosition
     */
    private void updateItemViewCheckStatus(int itemPosition){
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(itemPosition);
        if(holder instanceof PhotoRecylerAdapter.PhotoHolder){//update the uploaded phohto ui
            CheckBox itemCheckBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
            if(itemCheckBox != null){
                itemCheckBox.setChecked(false);
            }
        }
    }

    BroadcastReceiver mBrodcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if(Utils.ACTION_END_ANIMATOR.equals(action)){

             }else if(Utils.ACTION_UPLOAD_ONE_PHOTO_SUCCESS.equals(action)){

                 int value = intent.getIntExtra(Utils.CURRENT_POSITION,Utils.INSERT_ONE_PHOTO);
                 if(value != Utils.INSERT_ONE_PHOTO){
                     removeKeyFromSelectedMap(value,mRecylerAdapter);
                 }
             }else if(Utils.ACTION_UPLOAD_MULTI_PHOTOS_SUCCESS.equals(action)){

             }else if(Utils.ACTION_START_ANIMATOR.equals(action)){

             }else if(Utils.ACTION_UNSELECT_ALL.equals(action)){
                 unSelectAll();
             }
        }
    };

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG," service connenct");
            PhotoManagerService.LocalBinder binder = (PhotoManagerService.LocalBinder)service;
            photoManagerService = binder.getService();
            if(photoManagerService != null){
                Log.d(TAG," service setUiPresenter");
                photoManagerService.setUiPresenter(MainActivity.this);
            }
            isBoundService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG," service disconnected");
        }
    };

    /**
     * unSelectAll update the activity statsu
     */
    private void unSelectAll(){
        if(selectAllStatus){
            mRecylerAdapter.unselectedAll();
            selectAllStatus = false;
            selectedCountView.setText("0");
            selectButton.setTitle(R.string.select_all);
        }
    }

    /**
     * open worker manager ,when network conneted,
     * app will sync the data with cloud.
     */
    private void initSyncWorkManager(){
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeSyncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class).build();//.setConstraints(constraints)
       // PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(SyncWorker.class,3,TimeUnit.SECONDS).build();
        WorkManager.getInstance().enqueue(oneTimeSyncRequest);
    }


    /**
     * load data from server,
     * begin query the count,then fetch data.
     * because the limit max value is 500.
     * if count > 500 ,we need to some times   to download the data.
     */
    private void loadDataFromServer(){
        final BmobQuery<ImageInfo> query = new BmobQuery<ImageInfo>();
        UserInfo userInfo =  UserInfo.getCurrentUser(UserInfo.class);
        query.addWhereEqualTo("userInfo",userInfo);
        query.count(ImageInfo.class, new CountListener() {
            @Override
            public void done(Integer count, BmobException e) {
                if(e==null){
                    Log.d(TAG," count对象个数为: " + count);
                    if(count < MAX_LIMIT){
                        syncCloudData(0);
                    }else{
                        int pageNumber = count/MAX_LIMIT;
                        if(count%MAX_LIMIT != 0){
                            pageNumber=pageNumber+1;
                        }
                        for(int i=0; i<pageNumber + 1; i++){
                            int skip = i*MAX_LIMIT;
                            syncCloudData(skip);
                        }
                    }

                }else{
                    Log.d(TAG," 失败: " + e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    /**
     * fetch data from bmob server,use page.
     * @param skipNumber
     */
    private void syncCloudData(final int skipNumber) {
        final BmobQuery<ImageInfo> query = new BmobQuery<ImageInfo>();
        UserInfo userInfo =  UserInfo.getCurrentUser(UserInfo.class);
        query.addWhereEqualTo("userInfo",userInfo);
        query.setLimit(MAX_LIMIT);
        query.order("-createdAt");
        if(skipNumber > 0){
            query.setSkip(skipNumber);
        }
        query.findObjects(new FindListener<ImageInfo>() {
            @Override
            public void done(List<ImageInfo> list, BmobException e) {
                //  Log.d(TAG," find server done:" + list.size());
                if(e == null && (list == null || list.size() == 0)){
                    ScanPhotosAsyncTask scanPhotosAsyncTask = new ScanPhotosAsyncTask(mContext,mRecylerAdapter,mainHandler,mProgressBar);
                    scanPhotosAsyncTask.execute();
                    Log.d(TAG," syncCloudData finish:" + list.size());
                   // Toast.makeText(mContext,R.string.has_no_photos_in_cloud,Toast.LENGTH_LONG).show();
                }else if(e == null){
                    Log.d(TAG," syncCloudData to phone database begin:");
                    for(ImageInfo imageInfo: list){
                         DBUtils.syncDataToDatabase(mDatabaseHelper.getWritableDatabase(),imageInfo,Utils.SYNC_FROM_SERVER);
                    }
                    Log.d(TAG," syncCloudData to phone database end:");
                    if(skipNumber == 0){
                        ScanPhotosAsyncTask scanPhotosAsyncTask = new ScanPhotosAsyncTask(mContext,mRecylerAdapter,mainHandler,mProgressBar);
                        scanPhotosAsyncTask.execute();
                    }
                    Log.d(TAG," syncCloudData exist:" + list.size());
                }else{
                    Log.d(TAG," syncCloudData exception:"+ e.toString());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fab:
                Log.d(TAG," begin upload photo ");
                if(mRecylerAdapter!=null){
                    fab.setVisibility(View.GONE);
                    uploadPhotos();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void uploadAnimation(boolean flag,int position) {
         ImageView imageSyncView = null;
         CheckBox checkBox = null;
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(position);

        if(holder == null){
            Log.d(TAG," the view holder is null");
           return;
        }
        imageSyncView = ((PhotoRecylerAdapter.PhotoHolder) holder).getPhotoSyncView();
        checkBox = ((PhotoRecylerAdapter.PhotoHolder) holder).getCheckBoxView();
        if(flag == UIPresenter.ANIMATION_START){//start aunimation
            imageSyncView.setImageResource(android.R.drawable.stat_notify_sync);
            imageSyncView.setVisibility(View.VISIBLE);
            checkBox.setChecked(false);
            uploadAnimator = Utils.rotatePhotoImageViewAnimator(imageSyncView,0,360);
            Log.d(TAG," uploadAnimation begin start ");
            uploadAnimator.start();
        }else if(flag == UIPresenter.ANIMATION_STOP){//stop animation
            if(uploadAnimator != null){
                uploadAnimator.end();
                if(checkBox != null){
                    Log.d(TAG," uploadAnimation checkBox set false");
                    checkBox.setChecked(false);
                }
                if(imageSyncView != null) {
                    Log.d(TAG," uploadAnimation photo set sys upload img");
                    imageSyncView.setImageResource(android.R.drawable.stat_sys_upload);
                }
                uploadAnimator = null;
                Log.d(TAG," uploadAnimation  end ");
            }
        }
    }

    @Override
    public void sendMsgToMain(int msgId,int param) {
        Message msg = new Message();
        msg.what = msgId;
        msg.arg1 = param;
        mainHandler.sendMessage(msg);
    }

    /*
      if the image has been uploaded to cloud,
      we need save the image status, is uploaded finish.
     */
    @Override
    public void saveImageUploadStatus(String md5) {
        if(mRecylerAdapter != null){
            mRecylerAdapter.saveUploadStatus(md5);
        }
    }


    /**
     *  end the ObjectAnimator animation
     *
     */
    private void endAnimator(ObjectAnimator animator,CheckBox checkBox,ImageView imageSyncView){
        if(animator != null){
            animator.end();
            if(checkBox != null){
                Log.d(TAG," endAnimator checkBox set false");
                checkBox.setChecked(false);
            }
            if(imageSyncView != null) {
                Log.d(TAG," endAnimator photo set sys upload img");
                imageSyncView.setImageResource(android.R.drawable.stat_sys_upload);
            }
            Log.d(TAG," endAnimator ok.");
        }
    }

    /**
     *  if upload success, we weill update the selected map value,detail to see PhotoRecylerAdapter
     *  because, if the holderView is recycler use,the animator is null,can not set checkbox enable true or false, the UI is not refresh when pull back to check UI.
     *  so,wei need update the selected map--->checkBoxStatusMap
     * @param position, the key in map.
     */
    public static  void removeKeyFromSelectedMap(int position,PhotoRecylerAdapter adapter){
        Log.d(TAG," removeKeyFromSelectedMap ,the position is: " + position);
        if(position != -1 && adapter!= null && adapter.getCheckBoxStatusMap().containsKey(position)){//need remove the key ,value if success upload.
            adapter.getCheckBoxStatusMap().remove(position);
            Log.d(TAG," removeKeyFromSelectedMap ,remove ok");
        }
    }

    /**
     *
     * if login success,t
     * he navigation need get the user name and phone number.for ui display.
     */
    private void initNavHeaderUserInfo(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        userNameView = (TextView)navigationView.getHeaderView(0).findViewById(R.id.register_username);
        userNameView.setText(bmobUser.getUsername());
        registerPhoneNumberView = (TextView)navigationView.getHeaderView(0).findViewById(R.id.register_phone_number);
        registerPhoneNumberView.setText(bmobUser.getMobilePhoneNumber());
    }

    /**
     * logout the user.
     */
    private void logOut(){
        UserInfo.logOut();
        DBUtils.clearDbData(mDatabaseHelper.getWritableDatabase());
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
