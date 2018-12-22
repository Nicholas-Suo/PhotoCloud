package tech.xiaosuo.com.phontomanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.LocaleDisplayNames;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DeleteBatchListener;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.UpdateListener;
import tech.xiaosuo.com.phontomanager.bean.ImageInfo;
import tech.xiaosuo.com.phontomanager.bean.UserInfo;
import tech.xiaosuo.com.phontomanager.database.DataBaseHelper;
import tech.xiaosuo.com.phontomanager.swipelib.SwipeMenu;
import tech.xiaosuo.com.phontomanager.swipelib.SwipeMenuCreator;
import tech.xiaosuo.com.phontomanager.swipelib.SwipeMenuItem;
import tech.xiaosuo.com.phontomanager.swipelib.SwipeMenuListView;
import tech.xiaosuo.com.phontomanager.tools.DBUtils;
import tech.xiaosuo.com.phontomanager.tools.Utils;

public class CloudPhotosActivity extends AppCompatActivity implements AdapterView.OnItemClickListener , AdapterView.OnItemLongClickListener,CloudImageAdapter.CloudImageListener,View.OnClickListener {

    private static final String TAG = "CloudPhotosActivity";
    Toolbar toolbar;
    SwipeMenuListView cloudImageListView;
    CloudImageAdapter cloudImageAdapter;
    Context mContext;
    List<ImageInfo> mList;
    SmartRefreshLayout smartRefreshLayout;
    private static final int LIMIT_NUMBER = 20;
    private int curPage = 0;
    DataBaseHelper mDatabaseHelper;
    //RelativeLayout toolbarSelectLayout;
    RelativeLayout toolbarSelectLayout;
    LinearLayout toolbarNormalLayout;
    LinearLayout bottomLayout;
    ImageView selectedModeCancelView;
    TextView cloudSelectNumber;
    TextView cloudSelectAll;
    ImageView cloudDeletView;
    ImageView cloudDownloadView;
    boolean isSelectAll = false;
    int downloadCount = 0;
    boolean isSelectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_photos);

        mContext = getApplicationContext();
        smartRefreshLayout = (SmartRefreshLayout)findViewById(R.id.smart_refresh_layout);
        smartRefreshLayout.setEnableLoadMore(true);
        smartRefreshLayout.setEnableRefresh(true);
        mDatabaseHelper = DataBaseHelper.getDbHelperInstance(mContext);

        cloudImageListView = (SwipeMenuListView)findViewById(R.id.cloud_listview);

        cloudImageListView.setOnItemClickListener(this);
        cloudImageListView.setOnItemLongClickListener(this);
       // registerForContextMenu(cloudImageListView);
      //  cloudImageListView.setOnCreateContextMenuListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarSelectLayout = (RelativeLayout)findViewById(R.id.toolbar_select_layout);
        toolbarNormalLayout = (LinearLayout) findViewById(R.id.toolbar_normal_layout);
        selectedModeCancelView = (ImageView) findViewById(R.id.cloud_cancel);
        selectedModeCancelView.setOnClickListener(this);
        cloudSelectNumber = (TextView) findViewById(R.id.cloud_select_number);
        cloudSelectAll = (TextView) findViewById(R.id.cloud_select_all);
        cloudSelectAll.setOnClickListener(this);

        bottomLayout = (LinearLayout) findViewById(R.id.cloud_select_bottom_layout);
        cloudDeletView = (ImageView) findViewById(R.id.cloud_delete);
        cloudDeletView.setOnClickListener(this);
        cloudDownloadView = (ImageView)findViewById(R.id.cloud_download);
        cloudDownloadView.setOnClickListener(this);

        cloudImageAdapter = new CloudImageAdapter(mContext,cloudImageListView);
        cloudImageAdapter.setListener(this);
       // cloudImageListView.setAdapter(cloudImageAdapter);
        cloudImageListView.setAdapter(cloudImageAdapter);
        configSwipeMenu();
        mList = new ArrayList<ImageInfo>();
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                Log.d(TAG," onRefresh ...");
            }
        });
       // smartRefreshLayout.setEnableRefresh()
        smartRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if(isSelectedModeAm()){
                    smartRefreshLayout.finishLoadMore();
                    Log.d(TAG," onLoadMore ...selected mode need not load more data,return");
                     return;
                }
                Log.d(TAG," onLoadMore ...");
                curPage++;
                loadDataFromServer(LIMIT_NUMBER*curPage);

            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if(isSelectedModeAm()){
                    smartRefreshLayout.finishRefresh();
                    Log.d(TAG," onLoadMore ..Refresh...selected mode need not refresh data,return");
                    return;
                }
                Log.d(TAG," onLoadMore ..Refresh...");
                curPage = 0;
                loadDataFromServer(0);
            }
        });
      //  mDatabaseHelper = new DataBaseHelper(mContext,null,null,DataBaseHelper.VERSION);
        loadDataFromServer(0);
    }


   private void loadDataFromServer( int skipNumber){

       BmobQuery<ImageInfo> query = new BmobQuery<ImageInfo>();
       UserInfo userInfo =  UserInfo.getCurrentUser(UserInfo.class);
       query.addWhereEqualTo("userInfo",userInfo);
       query.setLimit(LIMIT_NUMBER);
       query.order("-createdAt");
       if(skipNumber > 0){
           query.setSkip(skipNumber);
       }else{
           if(mList != null){
               mList.clear();
           }
       }
       query.findObjects(new FindListener<ImageInfo>() {
           @Override
           public void done(List<ImageInfo> list, BmobException e) {
               //  Log.d(TAG," find server done:" + list.size());
               if(e == null && (list == null || list.size() == 0)){
                   Toast.makeText(mContext,R.string.has_no_photos_in_cloud,Toast.LENGTH_LONG).show();
               }else if(e == null){
                   if(mList == null || mList.size() == 0){
                       mList = list;
                   }else{
                       mList.addAll(list);
                   }

                   cloudImageAdapter.setCloudData(mList);
                   Log.d(TAG," loadDataFromServer exist:");
               }else{
                   Log.d(TAG," loadDataFromServer exception:"+ e.toString());
               }
              if(smartRefreshLayout.getState() == RefreshState.Loading){
                   smartRefreshLayout.finishLoadMore();
                   smartRefreshLayout.setNoMoreData(false);
              }else if(smartRefreshLayout.getState() == RefreshState.Refreshing){
                  smartRefreshLayout.finishRefresh();
              }

           }
       });
   }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
/*        if(isSelectMode){
            return;
        }*/
         Log.d(TAG," onItemClick position is: " + position);
        if(cloudImageListView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
            Log.d(TAG," onItemClick , is multichoice mode,set item checked .");
            updateSelectedItemCount(cloudImageListView.getCheckedItemCount(),cloudImageAdapter.getCount());
            cloudImageAdapter.notifyDataSetChanged();
            return;
        }

        ImageInfo imageInfo = (ImageInfo)cloudImageAdapter.getItem(position);
        previewPhoto(imageInfo,position);
        //previewPhoto(imageInfo.getFile().getUrl());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG," onItemLongClick position is: " + position);
        entryMultiSelectMode(position);
    //    onCloudItemLongClickListener(position);
        updateSelectedItemCount(1,cloudImageAdapter.getCount());
        return true;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.d(TAG," onCreateContextMenu ");
        getMenuInflater().inflate(R.menu.item_cloud_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = menuInfo.position;
        Log.d(TAG," menu,menuInfo.position: " + menuInfo.position);
        ImageInfo imageInfo = (ImageInfo)cloudImageAdapter.getItem(position);
        switch (id){
            case R.id.preview_photo:
                Log.d(TAG," menu,preview photo ");
               // previewPhoto(imageInfo.getFile().getFileUrl());
                break;
            case R.id.download_photo:
                Log.d(TAG," menu,download photo ");
                downloadFile(imageInfo);
                break;
            case R.id.delete_photo:
                Log.d(TAG," menu,delete photo ");
                deletPhotoInCloud(imageInfo.getFile());
                deleteDataInCloud(imageInfo,position);
                DBUtils.deleteDataFromDatabase(mDatabaseHelper.getWritableDatabase(),imageInfo);
                break;
                default:
                    Log.d(TAG," menu,default nothing ");
        }
        return super.onContextItemSelected(item);
    }

    /**
     *
     * the photo url in cloud, for glide to load.
     * startActivity PreviewPhoto
     * @param url
     */
    private void previewPhoto(String url){
        Intent intent = new Intent(this,PreviewPhotoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Utils.PHOTO_URL_KEY,url);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * the imageinfo use to send the previce activity,for preview,delete,download
     * @param imageInfo
     */
    private void previewPhoto(ImageInfo imageInfo,int position){
        Intent intent = new Intent(this,PreviewPhotoActivity.class);
        Bundle bundle = new Bundle();
        if(imageInfo == null || position < 0){
          Log.d(TAG," previewPhoto the imageinfo is null");
        }
        Log.d(TAG," previewPhoto to start preview activity");
        bundle.putSerializable(Utils.PHOTO_IMAGEINFO_KEY,imageInfo);
        bundle.putInt(Utils.PHOTO_IMAGEINFO_POSITION_KEY,position);
        intent.putExtras(bundle);
        startActivityForResult(intent,Utils.PREVIEW_CLOUD_PHOTO_REQUEST_CODE);
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
     * delete the item data from table imageinfo.
     * @param imageInfo
     */
    private void deleteDataInCloud(ImageInfo imageInfo,final int position){
        if(imageInfo == null || position < 0){
             return;
        }
/*        ImageInfo gameScore = new ImageInfo();
        imageInfo.setObjectId("dd8e6aff28");
        imageInfo.getObjectId();*/
        imageInfo.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                    Log.d(TAG," the item data in imageinfo table  delete success");
                    mList.remove(position);
                    cloudImageAdapter.setCloudData(mList);
                }else{
                    Log.d(TAG," the item data in imageinfo table  delete fail "+e.getMessage()+","+e.getErrorCode());
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
                }else{
                    Log.d(TAG,"  downloadFile -> download fail...savePath: " +e.getErrorCode()+","+e.getMessage());
                }
            }
            @Override
            public void onProgress(Integer value, long newworkSpeed) {
                Log.d(TAG," downloadFile -> 下载进度："+value+","+newworkSpeed);
            }
        });
    }

    /**
     * down load  the photo from server.
     * @param dataList
     */
    private void downloadMultiFiles(HashMap<ImageInfo,Integer> dataList){
        //允许设置下载文件的存储路径，默认下载文件的目录为：context.getApplicationContext().getCacheDir()+"/bmob/"

        if(dataList == null || mContext == null){
            return;
        }

        final ProgressDialog progressDialog = getProgressDialog(R.string.is_downloading,ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        // remove the exist data in phone ,only download not exist photo.
        for(ImageInfo imageInfo : dataList.keySet()){
            if(DBUtils.isExistInSysImageDb(mContext,imageInfo)){
                dataList.remove(imageInfo);
            }
        }
        final int total = dataList.size();

        for(final ImageInfo imageInfo : dataList.keySet()){
  /*          if(DBUtils.isExistInSysImageDb(mContext,imageInfo)){
               continue;
            }*/

            File saveFile = Utils.getPhoneImageFile(imageInfo);//new File(imageInfo.getData());
            BmobFile file = imageInfo.getFile();
            file.download(saveFile, new DownloadFileListener() {

                @Override
                public void onStart() {
                    Log.d(TAG," downloadMultiFiles -> begin download... ");
                }

                @Override
                public void done(String savePath,BmobException e) {
                    if(e==null){
                        Log.d(TAG,"  downloadMultiFiles -> download success...savePath: " + savePath);
                        DBUtils.insertRestoreImageInfoToDb(mContext,imageInfo);
                        downloadCount++;
                        int percent = downloadCount*100/total;
                        progressDialog.setProgress(percent);
                        if(downloadCount == total){
                            progressDialog.dismiss();
                            showDownloadFinishDialog(downloadCount,total);
                            downloadCount = 0;

                        }
   /*                     if(){

                        }*/
                    }else{
                        Log.d(TAG,"  downloadMultiFiles -> download fail...savePath: " +e.getErrorCode()+","+e.getMessage());
                        downloadCount = 0;
                        progressDialog.dismiss();
                    }
                }
                @Override
                public void onProgress(Integer value, long newworkSpeed) {
                    Log.d(TAG," downloadMultiFiles -> 下载进度："+value+","+newworkSpeed);
      /*              int percent = value*100/total;
                    progressDialog.setProgress(percent);*/
                }
            });
        }
    }
    @Override
    public void onCloudItemLongClickListener(int position) {
/*        disablePullRefreshLoadMore();
        showSelectToolbarLayout(true);
        showBottomLayout(true);
        //cloudImageAdapter.setCloudData(mList);
        cloudImageAdapter.notifyDataSetChanged();*/
        entryMultiSelectMode(position);

    }

    @Override
    public void onCloudItemClickListener(boolean isSelectAll) {
        this.isSelectAll = isSelectAll;
        updateSelectAllView(isSelectAll);
    }

    @Override
    public void previewCloudPhoto(String url) {
        if(url == null){
            Log.d(TAG," onCloudItemClickListener urs is null ,return ");
            return;
        }
        previewPhoto(url);
    }

    @Override
    public void previewCloudPhoto(ImageInfo imageInfo,int position) {
        if(imageInfo == null || position < 0){
            Log.d(TAG," onCloudItemClickListener imageInfo is null ,return ");
            return;
        }
        previewPhoto(imageInfo,position);
    }

    @Override
    public void updateSelectedItemCount(int count,int total) {
            if(cloudSelectNumber != null){
                cloudSelectNumber.setText(getString(R.string.already_select) + String.valueOf(count));
            }
            if(count == total){
                isSelectAll = true;
            }else{
                isSelectAll = false;
            }
           updateSelectAllView(isSelectAll);
            if(count == 0){ //if the count is 0, no selected item,need exit the multi choice mode.
                cancelCloudSelectMode();
            }
    }

    /**
     * show the select toolbar when long press item.
     * @param show
     */
    private void showSelectToolbarLayout(boolean show){
      if(show){
          toolbarNormalLayout.setVisibility(View.GONE);
          toolbarSelectLayout.setVisibility(View.VISIBLE);
      }else{
          toolbarNormalLayout.setVisibility(View.VISIBLE);
          toolbarSelectLayout.setVisibility(View.GONE);
      }
    }

    /**
     * showBottomLayout when long press item.
     * @param show
     */
    private void showBottomLayout(boolean show){
       if(show){
           bottomLayout.setVisibility(View.VISIBLE);
       }else{
           bottomLayout.setVisibility(View.GONE);
       }
    }

    /**
     * whether the activity is selected mode.
     * @return
     */
    private boolean isSelectedModeAm(){
        if(cloudImageAdapter != null && cloudImageAdapter.getCurrMode() == CloudImageAdapter.SELECT_MODE){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.cloud_cancel:
                cancelCloudSelectMode();
                /*showSelectToolbarLayout(false);
                showBottomLayout(false);
                if(cloudImageAdapter != null){
                    cloudImageAdapter.setCurrMode(CloudImageAdapter.NORMAL_MODE);
                }
                enablePullRefreshLoadMore();*/
                break;
            case R.id.cloud_select_all:
                if(cloudImageAdapter == null){
                    return;
                }
                if( isSelectAll == false){
                    cloudImageAdapter.seletedAll();
                    isSelectAll = true;
                    //cloudSelectAll.setText(R.string.un_select_all);
                }else{
                    cloudImageAdapter.unseletedAll();
                    isSelectAll = false;
                   // cloudSelectAll.setText(R.string.select_all);
                }
                updateSelectAllView(isSelectAll);
                break;
            case R.id.cloud_delete:
                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.dialog_title);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage(getString(R.string.is_deleting));
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
              /*  AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle(R.string.dialog_title);
                builder.setView(R.layout.dialog_delete_progress_layout);
                AlertDialog dialog = builder.show();*/
                final HashMap<ImageInfo,Integer> seletedData = cloudImageAdapter.getCloudSelectedPhotoData();

                if(seletedData == null || seletedData.isEmpty()){
                      return;
                }
                final List<BmobObject> imageInfoList = new ArrayList<BmobObject>();
                for(ImageInfo imageInfo : seletedData.keySet()){
                     imageInfoList.add(imageInfo);
                }
                new BmobBatch().deleteBatch(imageInfoList).doBatch(new QueryListListener<BatchResult>() {
                    @Override
                    public void done(List<BatchResult> o, BmobException e) {
                        if(e==null){
                            for(int i=0;i<o.size();i++){
                                BatchResult result = o.get(i);
                                BmobException ex =result.getError();
                                if(ex==null){
                                    Log.d(TAG,"第"+i+"个数据批量删除成功");
                                    ImageInfo imageInfo = (ImageInfo)imageInfoList.get(i);
                                    DBUtils.deleteDataFromDatabase(mDatabaseHelper.getWritableDatabase(),imageInfo);
                                    mList.remove(imageInfo);
                                    //cloudImageAdapter.removeCloudSelectMapData(seletedData.get(imageInfo));
                                }else{
                                    Log.d(TAG,"第"+i+"个数据批量删除失败："+ex.getMessage()+","+ex.getErrorCode());
                                }
                            }
                            dialog.dismiss();
                            cancelCloudSelectMode();
                            cloudImageAdapter.setCloudData(mList);
                        }else{
                            Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                            dialog.dismiss();
                            cloudImageAdapter.setCloudData(mList);
                        }
                    }
                });
                int count = imageInfoList.size();
                String[] urls =new String[count];
                for(int i=0;i<count;i++){
                    ImageInfo imageInfo = (ImageInfo)imageInfoList.get(i);
                    urls[i] = imageInfo.getFile().getUrl();
                }

                BmobFile.deleteBatch(urls, new DeleteBatchListener() {
                    @Override
                    public void done(String[] failUrls, BmobException e) {
                        if(e==null){
                            Log.d(TAG," 文件全部删除成功");
                        }else{
                            if(failUrls!=null){
                                Log.d(TAG,"文件删除失败个数："+failUrls.length+","+e.toString());
                            }else{
                                Log.d(TAG,"全部文件删除失败："+e.getErrorCode()+","+e.toString());
                            }
                        }
                    }
                });
/*                for(Map.Entry<Integer,ImageInfo> entry : seletedData.entrySet()){
                    int position = entry.getKey().intValue();
                    ImageInfo imageInfo = entry.getValue();
                    deletPhotoInCloud(imageInfo.getFile());
                    deleteDataInCloud(imageInfo,position);
                    DBUtils.deleteDataFromDatabase(mDatabaseHelper.getWritableDatabase(),imageInfo);
                }*/
                //dialog.dismiss();
                break;
            case R.id.cloud_download:
                 HashMap<ImageInfo,Integer> downloadData = cloudImageAdapter.getCloudSelectedPhotoData();
                Log.d(TAG," begin,download photo ");
                cancelCloudSelectMode();
                downloadMultiFiles(downloadData);
  /*              for(ImageInfo imageInfo : downloadData.keySet()){
                    downloadFile(imageInfo);
                }*/
                break;
            default:
                    break;
        }
    }

    /**
     * disablePullRefreshLoadMore
     */
    private void disablePullRefreshLoadMore(){
        if(smartRefreshLayout != null){
            smartRefreshLayout.setEnableRefresh(false);
            smartRefreshLayout.setEnableLoadMore(false);
        }
    }

    private void enablePullRefreshLoadMore(){
        if(smartRefreshLayout != null){
            smartRefreshLayout.setEnableRefresh(true);
            smartRefreshLayout.setEnableLoadMore(true);
        }
    }

    /**
     *
     * @param status
     */
    private void updateSelectAllView(boolean status){
           if(status){
               cloudSelectAll.setText(R.string.un_select_all);
           }else{
               cloudSelectAll.setText(R.string.select_all);
           }
        toolbar.invalidate();
    }

    /**
     *  cancel cloud select mode,entry normal mode.
     */
    private void cancelCloudSelectMode(){
        cloudImageListView.clearChoices();
        cloudImageListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        showSelectToolbarLayout(false);
        showBottomLayout(false);
/*        if(cloudImageAdapter != null){
            cloudImageAdapter.setCurrMode(CloudImageAdapter.NORMAL_MODE);
        }*/
        enablePullRefreshLoadMore();
    }

    /**
     *
     * @param message the string id.
     * @param type    the progressbar style SPINNER , HORISONTAL
     * @return
     */
    private ProgressDialog getProgressDialog(int message,int type){
        ProgressDialog progressDialog  = new ProgressDialog(this);
        progressDialog.setTitle(R.string.dialog_title);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(type);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setMessage(getString(message));
        return  progressDialog;
    }

    private void showDownloadFinishDialog(int finishCount,int total){
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setTitle(R.string.dialog_title);
     //StringBuilder sb = new StringBuilder();
     builder.setMessage(R.string.download_finish);
     builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){

         @Override
         public void onClick(DialogInterface dialog, int which) {
             dialog.dismiss();
         }
     });
        AlertDialog dialog = builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            return;
        }
        if(requestCode == Utils.PREVIEW_CLOUD_PHOTO_REQUEST_CODE){
           //int position =//need get the position ,startactivityForResult setResult.
        }
    }

    /**
     * configSwipeMenu, set the swipe menu item.
     */
    private void configSwipeMenu(){
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.mipmap.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        if(cloudImageListView != null){
            cloudImageListView.setMenuCreator(creator);
        }

    }

    /**
     *
     * @param dp
     * @return
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     *
     * entry multiselectMode.
     * @param position  the select item position.
     */
    private void entryMultiSelectMode(int position){
/*        if(isSelectMode){
           return;
        }
       // isSelectMode = true;*/
       if(cloudImageListView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
            Log.d(TAG," listview is at choice mode multi modal,return .");
            return;
        }
        cloudImageListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        cloudImageListView.setItemChecked(position,true);
        disablePullRefreshLoadMore();
        showSelectToolbarLayout(true);
        showBottomLayout(true);
        //cloudImageAdapter.setCloudData(mList);
        cloudImageAdapter.notifyDataSetChanged();
    }

    /**
     * exit the mulit select mode
     */
    private void exitMultiSelectMode(){
        isSelectMode = false;
    }
    /*******************************Bmob interface,for multi file and data delete******************
     批量删除文件

     自 BmobSDKv3.4.6 版本，SDK提供了文件的批量删除接口deleteBatch，且只能删除通过CDN文件服务（v3.4.6开始采用CDN文件服务）上传的文件。

     示例代码如下：

     //此url必须是上传文件成功之后通过bmobFile.getUrl()方法获取的。
     String[] urls =new String[]{url};
     BmobFile.deleteBatch(urls, new DeleteBatchListener() {
    @Override
    public void done(String[] failUrls, BmobException e) {
    if(e==null){
    toast("全部删除成功");
    }else{
    if(failUrls!=null){
    toast("删除失败个数："+failUrls.length+","+e.toString());
    }else{
    toast("全部文件删除失败："+e.getErrorCode()+","+e.toString());
    }
    }
    }
    });
     为方便大家理解文件服务的使用，Bmob提供了一个文件上传的案例和源码，大家可以到示例和教程中查看和下载。







     批量删除

     List<BmobObject> persons = new ArrayList<BmobObject>();
     Person p1 = new Person();
     p1.setObjectId("38ea274d0c");
     Person p2 = new Person();
     p2.setObjectId("01e29165bc");
     Person p3 = new Person();
     p3.setObjectId("d8226c4828");
     persons.add(p1);
     persons.add(p2);
     persons.add(p3);
     //第一种方式：v3.5.0之前的版本
     new BmobObject().deleteBatch(this, persons, new DeleteListener() {
    @Override
    public void onSuccess() {
    toast("批量删除成功");
    }
    @Override
    public void onFailure(int code, String msg) {
    toast("批量删除失败:"+msg);
    }
    });
     //第二种方式：v3.5.0开始提供
     new BmobBatch().deleteBatch(persons).doBatch(new QueryListListener<BatchResult>() {
    @Override
    public void done(List<BatchResult> o, BmobException e) {
    if(e==null){
    for(int i=0;i<o.size();i++){
    BatchResult result = o.get(i);
    BmobException ex =result.getError();
    if(ex==null){
    log("第"+i+"个数据批量删除成功");
    }else{
    log("第"+i+"个数据批量删除失败："+ex.getMessage()+","+ex.getErrorCode());
    }
    }
    }else{
    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
    }
    }
    });
     */
}
