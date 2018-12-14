package tech.xiaosuo.com.phontomanager;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import tech.xiaosuo.com.phontomanager.bean.PhotoInfoTable;
import tech.xiaosuo.com.phontomanager.database.DataBaseHelper;
import tech.xiaosuo.com.phontomanager.disklru.DiskLruCache;
import tech.xiaosuo.com.phontomanager.tools.Utils;
import tech.xiaosuo.com.phontomanager.bean.ImageInfo;

/**
 * Created by wangshumin on 5/28/2018.
 *   https://android.googlesource.com/platform/libcore/+/android-4.1.1_r1/luni/src/main/java/libcore/io/DiskLruCache.java
 */

public class PhotoRecylerAdapterGlide extends RecyclerView.Adapter<PhotoRecylerAdapterGlide.PhotoHolder> {

    private static final String TAG = "PhotoRecylerAdapter";
    private static final String CACHE = "suo";
    private static final int DISK_LRU_CACHE = 1024 * 1024 * 2;
    private boolean scrollFlag = false;
    private final int REQ_WIDTH = 118;// this value is equal layou:recyler_time.xml's imageView widt
    private final int REQ_HEIGHT = 118;
    List<ImageInfo> listData;
    HashMap<Integer,Boolean> checkBoxStatusMap;//only sace the checked status: true.
    HashMap<String,Boolean> syncStatusMap;
    Context context;
    LruCache<String,Bitmap> lruCache;
    DiskLruCache diskLruCache;
    Bitmap defaultBitmap;
    OnRecyclerItemClickListener itemListener;
    DataBaseHelper dbHelper;
    RecyclerView recyclerView;
    //SQLiteDatabase sqLiteDatabase;

    public PhotoRecylerAdapterGlide(Context context,DataBaseHelper dbHelper,RecyclerView recyclerView){
        this.context = context;
        this.dbHelper = dbHelper;
        this.recyclerView = recyclerView;
      //  sqLiteDatabase = dbHelper.getReadableDatabase();
        int appMemory = (int)Runtime.getRuntime().maxMemory();
        lruCache = new LruCache<String,Bitmap>(appMemory/8){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

        String cacheDir = context.getCacheDir().getPath();
        Log.d(TAG," disk lrc cache dir: " + cacheDir);
        File cacheFile = new File(cacheDir,CACHE);
        if(!cacheFile.exists()){
            cacheFile.mkdirs();
        }

        try {
            diskLruCache = DiskLruCache.open(cacheFile,1,1,DISK_LRU_CACHE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        defaultBitmap = BitmapFactory.decodeResource(context.getResources(),R.mipmap.default_icon);
        checkBoxStatusMap = new HashMap<Integer, Boolean>();
        syncStatusMap = new HashMap<String,Boolean>();

    }
    public void setPhotoData(List<ImageInfo> list){
        this.listData = list;
        if(listData == null || listData.size() == 0){
            return;
        }
        for(ImageInfo info : listData){
            boolean uploadStatus = queryUploadStatusFromDB(info.getMd5());// here,perhaps need the thread to query the database. I think...A lasy man...haha
            if(uploadStatus){//update sync image view
                syncStatusMap.put(info.getMd5(),uploadStatus);// update the sync image view for has uploaded img view.
            }
        }
        recyclerView.setVisibility(View.VISIBLE);
        notifyDataSetChanged();
    }
    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recyler_item,parent,false);
        PhotoHolder photoHolder = new PhotoHolder(view);
        Log.d(TAG,"  create holder ");
        return photoHolder;
    }

    @Override
    public void onBindViewHolder(PhotoHolder holder, final int position) {
        Bitmap bitmap = null;
        boolean sysnStatus = false;
        Log.d(TAG," bindView postion: " + position);

        ImageInfo imageInfo = listData.get(position);
        String imgMd5 = imageInfo.getMd5();
        final CheckBox checkBox = holder.getCheckBoxView();
        final ImageView photoView = holder.getPhotoView();
        final ImageView photoSyncView = holder.getPhotoSyncView();
        photoSyncView.setVisibility(View.GONE);

/*    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) ;*/
    /*    select * from table where mdr = 'md5';*/
/*        String[] columns = new String[]{PhotoInfoTable.COLUMN_IMAGE_IS_IN_CLOUD};
        String selection = PhotoInfoTable.COLUMN_IMAGE_MD5 + " =? ";
        String[] selectionArgs = new String[]{imageInfo.getMd5()};
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(PhotoInfoTable.PHOTO_INFO_TABLE,columns,selection,selectionArgs,null,null,null);
        if(cursor != null && cursor.moveToFirst()){

           int index =  cursor.getColumnIndex(PhotoInfoTable.COLUMN_IMAGE_IS_IN_CLOUD);
            Log.d(TAG," query db,cloud ,the index is:" + index +  " cursor.getCount() " + cursor.getCount());
            int cloud = cursor.getInt(index);
            Log.d(TAG," the cloud:" + cloud);
            if(cloud == 1){
                photoSyncView.setVisibility(View.VISIBLE);
                photoSyncView.setImageResource(android.R.drawable.stat_sys_upload_done);
            }
        }*/

        if(syncStatusMap.containsKey(imgMd5)){
            sysnStatus = syncStatusMap.get(imgMd5);
            Log.d(TAG," sysnStatus is:" + sysnStatus + " position is: " + position + " imgMd5 is: " + imgMd5);
            if(sysnStatus){
                photoSyncView.setVisibility(View.VISIBLE);
                photoSyncView.setImageResource(android.R.drawable.stat_sys_upload_done);
            }else{
                photoSyncView.setVisibility(View.GONE);
            }
        }

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isContainsCheckedBox(position)){
                    Log.d(TAG," the image status ,1 ");
                    checkBox.setChecked(false);
                }else{
                    Log.d(TAG," the image status ,2 ");
                    checkBox.setChecked(true);
                }
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked && !isContainsCheckedBox(position)){
                    checkBoxStatusMap.put(position,isChecked);
                    Log.d(TAG," the check box status isChecked: " + isChecked + " position is: " + position);
                    scalePhotoImageView(photoView,0.85f,0.85f);
                }else if(isChecked == false && isContainsCheckedBox(position)){
                    Log.d(TAG," the check box status isChecked equal false ");
                    checkBoxStatusMap.remove(position);
                    scalePhotoImageView(photoView,1.0f,1.0f);
                }
                if(itemListener != null && checkBoxStatusMap != null){
                    int number = checkBoxStatusMap.size();
                    itemListener.onRecyclerItemClick(number);
                }

            }
        });
        if(checkBoxStatusMap != null && isContainsCheckedBox(position)){
            checkBox.setChecked(true);
        }else{
            checkBox.setChecked(false);
        }
        File file = new File(imageInfo.getData());
        Glide.with(context).load(file).into(photoView);
        if(isContainsCheckedBox(position)){
            Log.d(TAG," load bitmap from lru cache,the selected position: " + position);
            scalePhotoImageView(photoView,0.85f,0.85f);
        }else{//avoid the recycle holder view.
            scalePhotoImageView(photoView,1.0f,1.0f);
        }
        //get from memory lru cache.
       /* bitmap = getBitmapFromLrucache(imageInfo.getData());
        if(bitmap != null){
            Log.d(TAG," load bitmap from lru cache");
            if(isContainsCheckedBox(position)){
                Log.d(TAG," load bitmap from lru cache,the selected position: " + position);
                scalePhotoImageView(photoView,0.85f,0.85f);
            }else{//avoid the recycle holder view.
                scalePhotoImageView(photoView,1.0f,1.0f);
            }
            photoView.setImageBitmap(bitmap);
            return;
        }
        //get from disk lru cache

        bitmap = getBitmapFromDiskLrucache(imageInfo.getData());
        if(bitmap != null){
            Log.d(TAG," load bitmap from diskcache");
            photoView.setImageBitmap(bitmap);
            return;
        }


         String path = imageInfo.getData();
      //   Uri fileUri = Uri.parse(path);
       //  photoView.setImageURI(fileUri);
       if(canCurrImageViewTask(photoView,path)){
            DecodeBitmapTask decodeTask = new DecodeBitmapTask(photoView);
            AsyncDrawable drawable = new AsyncDrawable(context.getResources(),defaultBitmap,decodeTask);
            photoView.setImageDrawable(drawable);
            decodeTask.execute(path);
        }*/

    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

     public class PhotoHolder extends RecyclerView.ViewHolder{
         private CheckBox checkBoxView;
         private ImageView photoView;
         private ImageView photoSyncView;
         private DecodeBitmapTask decodeTask = null;
         private String path = null;

         public PhotoHolder(View itemView) {
            super(itemView);
            checkBoxView = (CheckBox)itemView.findViewById(R.id.photo_check_box);
            photoView = (ImageView)itemView.findViewById(R.id.photo_view);
            photoSyncView = (ImageView)itemView.findViewById(R.id.photo_sync_view);
        }

         public ImageView getPhotoSyncView() {
             return photoSyncView;
         }

         public CheckBox getCheckBoxView(){
            return checkBoxView;
        }

        ImageView getPhotoView(){
            return photoView;
        }

         public String getPath() {
             return path;
         }

         public void setPath(String path) {
             this.path = path;
         }

     }
/**
 * save bitmap to lurcache
 */

    private void setLruCache(String path,Bitmap bitmap){
        lruCache.put(path,bitmap);
    }

    private Bitmap getBitmapFromLrucache(String path){
        return lruCache.get(path);
    }

    /**
     * get bitmap from disklru cache
     */
    private Bitmap getBitmapFromDiskLrucache(String path){
        Bitmap bitmap = null;
        try {
            String key = Utils.getStringMd5(path);
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if(snapshot == null){
                return null;
            }
            //FileInputStream inputStream = (FileInputStream)snapshot.getInputStream(0);
           // FileDescriptor discriptor = inputStream.getFD();
            //ImageResizer.decodeBitmapFromFileDescriptor(discriptor,REQ_WIDTH,REQ_HEIGHT);
            bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(0));
            lruCache.put(path,bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    public void setScrollFlag(boolean flag){
        scrollFlag = flag;
    }

    private class DecodeBitmapTask extends AsyncTask<String,Integer,Bitmap>{
        WeakReference<ImageView> imageViewWeak;

        private String path = null;

        public String getPath() {
            return path;
        }

        public DecodeBitmapTask(ImageView imageView){
            imageViewWeak = new WeakReference<ImageView>(imageView);
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            int reqW = (int)(ImageResizer.dp2px(context,REQ_WIDTH));
          //  int reqH = (int)ImageResizer.dp2px(context,REQ_HEIGHT);
            path = params[0];
            Log.d(TAG," the path is " + path);
            Bitmap bitmap = ImageResizer.decodeBitmapFromFile(path,reqW,reqW);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView iamgeView = imageViewWeak.get();
            if(iamgeView == null){
                return;
            }
            Drawable drawable = iamgeView.getDrawable();
            if(drawable instanceof AsyncDrawable){
                DecodeBitmapTask task = ((AsyncDrawable) drawable).getDecodeTaskWeak().get();
                 if(task == this){
                     iamgeView.setImageBitmap(bitmap);
                 }
            }
            String md5 = Utils.getStringMd5(path);
            setLruCache(path,bitmap);
            try {
                DiskLruCache.Editor editor = diskLruCache.edit(md5);
                if(editor != null){
                    OutputStream outputStream = editor.newOutputStream(0);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    editor.commit();
                    diskLruCache.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

/*    class AsyncDrawable extends BitmapDrawable {
        private WeakReference<cn.com.zyh.MyRecyclerViewAdapter_Best.DownLoadTask> downLoadTaskWeakReference;

        public AsyncDrawable(Resources resources, Bitmap bitmap, cn.com.zyh.MyRecyclerViewAdapter_Best.DownLoadTask downLoadTask){
            super(resources,bitmap);
            downLoadTaskWeakReference = new WeakReference<cn.com.zyh.MyRecyclerViewAdapter_Best.DownLoadTask>(downLoadTask);
        }

        private cn.com.zyh.MyRecyclerViewAdapter_Best.DownLoadTask getDownLoadTaskFromAsyncDrawable(){
            return downLoadTaskWeakReference.get();
        }
    }*/

    class AsyncDrawable extends BitmapDrawable{
        WeakReference<DecodeBitmapTask> decodeTaskWeak;

        public AsyncDrawable(Resources rs,Bitmap bitmap,DecodeBitmapTask task){
               super(rs,bitmap);
            decodeTaskWeak = new WeakReference<DecodeBitmapTask>(task);
        }

        public WeakReference<DecodeBitmapTask> getDecodeTaskWeak() {
            return decodeTaskWeak;
        }
    }

    /*
*   cancel the current task.
*
* */
    public boolean canCurrImageViewTask(ImageView imageView,String path){
           boolean result = true;
           DecodeBitmapTask task;
           BitmapDrawable drawable = (BitmapDrawable)imageView.getDrawable();
           if(drawable instanceof AsyncDrawable){
               task = ((AsyncDrawable) drawable).getDecodeTaskWeak().get();
               if(task != null && !path.equals(task.getPath())){
                   task.cancel(true);
                   result = true;
               }else{
                   result = false;
               }
           }
           return result;
    }
/*
*    set the item listener.
*    interface
* */
    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener listener){
        itemListener = listener;
    }
    public interface OnRecyclerItemClickListener{
        void onRecyclerItemClick(int count);
    }

    /*
    *    animator view
    *
    * */
    private void scalePhotoImageViewAnimator(ImageView photoView,float X,float Y){
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(photoView,"scaleX",X);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(photoView,"scaleY",Y);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.playTogether(scaleX,scaleY);
        animatorSet.start();
    }



    /*
   *   use update the ImageView layout,to work scale animator
   *   the defined width is REQ_WIDTH
   */
    private void scalePhotoImageView(ImageView photoView,float scaleX,float Y){
        ViewGroup.LayoutParams layoutParams = photoView.getLayoutParams();
        layoutParams.width = (int)ImageResizer.dp2px(context,(int)(REQ_WIDTH*scaleX));//(int)(photoView.getMeasuredWidth()*scaleX);//
        layoutParams.height = layoutParams.width;
        photoView.setLayoutParams(layoutParams);
    }

/*
* check  whether the map contains checked CheckBox status for postion.
* */
    private boolean isContainsCheckedBox(int position){

        boolean result = false;
        if(checkBoxStatusMap.containsKey(position)){
            result = true;
        }
        Log.d(TAG," isContainsCheckedBox the position: " + position + " result: " + result);
        return result;
    }

    /*
* select all items
* */
    public boolean selectedAll(){

        if(listData == null || listData.size() == 0){
               return false;
        }
        for(int i =0;i<listData.size();i++){
            checkBoxStatusMap.put(i,true);
        }
        notifyDataSetChanged();
        return true;
    }

    /*
* unselect all items
* */
    public boolean unselectedAll(){

        if(checkBoxStatusMap == null || checkBoxStatusMap.size() == 0){
            return false;
        }
        checkBoxStatusMap.clear();
        notifyDataSetChanged();
        return true;
    }





    public List<ImageInfo> getListData() {
        return listData;
    }

    /**
     *
     * get the current checked Box map
     * @return
     */
    public HashMap<Integer, Boolean> getCheckBoxStatusMap() {
        return checkBoxStatusMap;
    }

    /**
     * clean the checked box map
     */
    public void clearCheckBoxStatusMap(){
        if(checkBoxStatusMap != null){
            checkBoxStatusMap.clear();
        }
    }

    /**
     * remove  value from checed box map.
     * @param key
     */
    public  void removeValueFromMap(Integer key){
        if(checkBoxStatusMap != null && checkBoxStatusMap.containsKey(key)){
            checkBoxStatusMap.remove(key);
        }
    }

    /**
     * query the upload status from database.for update the syn image view
     * @param md5 ->image path to md5
     * @return
     */
    private boolean queryUploadStatusFromDB(String md5){
        String[] columns = new String[]{PhotoInfoTable.COLUMN_IMAGE_IS_IN_CLOUD};
        String selection = PhotoInfoTable.COLUMN_IMAGE_MD5 + " =? ";
        String[] selectionArgs = new String[]{md5};
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(PhotoInfoTable.PHOTO_INFO_TABLE,columns,selection,selectionArgs,null,null,null);
        if(cursor != null && cursor.moveToFirst()){

            int index =  cursor.getColumnIndex(PhotoInfoTable.COLUMN_IMAGE_IS_IN_CLOUD);
            Log.d(TAG," query db,cloud ,the index is:" + index +  " cursor.getCount() " + cursor.getCount());
            int cloud = cursor.getInt(index);
            Log.d(TAG," the cloud:" + cloud);
            if(cloud == 1){
               return true;
            }
        }
        return false;
    }

    /**
     *
     * @param md5
     */
    public void saveUploadStatus(String md5){
        if(syncStatusMap != null && !syncStatusMap.containsKey(md5)){
            syncStatusMap.put(md5,true);
        }

    }
}
