package tech.xiaosuo.com.phontomanager;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

//import com.bumptech.glide.Glide;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tech.xiaosuo.com.phontomanager.bean.ImageInfo;
import tech.xiaosuo.com.phontomanager.swipelib.SwipeMenuListView;

public class CloudImageAdapter extends BaseAdapter {

    private static final String TAG = "CloudImageAdapter";
    List<ImageInfo> cloudList;
    Context context;
    CloudImageListener listener;
    HashMap<Integer,Boolean> checkBoxStatusMap;
    public static int NORMAL_MODE = 0;
    public static int SELECT_MODE = 1;
    int currMode = NORMAL_MODE;
    boolean refreshLayout = false;
    SwipeMenuListView swipeMenuListView;


    public int getCurrMode() {
        return currMode;
    }

    public void setCurrMode(int currMode) {
        this.currMode = currMode;
        if(currMode == NORMAL_MODE){

           if(checkBoxStatusMap != null){
                checkBoxStatusMap.clear();
                refreshLayout = false;
                notifyDataSetChanged();
            }
        }
    }

    public void setListener(CloudImageListener listener) {
        this.listener = listener;
    }

    public CloudImageAdapter(Context context, SwipeMenuListView swipeMenuListView){
            this.context = context;
            this.swipeMenuListView = swipeMenuListView;
            checkBoxStatusMap = new HashMap<Integer, Boolean>();
    }
    public void setCloudData(List<ImageInfo> cloudList){
          this.cloudList = cloudList;
          notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return cloudList == null ? 0 :cloudList.size();
    }

    @Override
    public ImageInfo getItem(int position) {
        return cloudList.get(position) ;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ItemCloudHolder holder;
        boolean refreshItem = false;
        //reset the value,after entry selected mode.

        if(currMode == SELECT_MODE && refreshLayout && convertView != null){
             convertView = null;
           //  Log.d(TAG," need refresh the layout.");
             //refreshLayout = false;//reset the value,after entry selected mode.
         }

         if(position == getCount()){
             refreshLayout = false;//reset the value,after entry selected mode.
         }
        // back to normal mode from selected mode, need reload the layout,for checkbox reset checked value.

        Log.d(TAG," need refresh the layout.refreshItem: " + refreshItem + " position: " + position);

        if(convertView == null){
          //  Log.d(TAG," need refresh the layout.reset convertView");
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cloud_photo,null,false);
            holder = new ItemCloudHolder();
            holder.cloudImageView = (ImageView)convertView.findViewById(R.id.cloud_image);
            holder.imageName = (TextView)convertView.findViewById(R.id.cloud_image_name);
            holder.imageTime = (TextView)convertView.findViewById(R.id.cloud_image_time);
         //   holder.checkBox = (CheckBox) convertView.findViewById(R.id.cloud_check_box);
            convertView.setTag(holder);
        }else{
           // Log.d(TAG," need refresh the layout.do not reset convertView");
            holder = (ItemCloudHolder)convertView.getTag();
        }

/*        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int selectedCount = 0;
                if(currMode == SELECT_MODE){
                    checkBoxStatusMap.put(Integer.valueOf(position),isChecked);
                }
                for(Integer position : checkBoxStatusMap.keySet()){
                      boolean isSelected = checkBoxStatusMap.get(position).booleanValue();
                     if(isSelected){
                         selectedCount++;
                     }
                }
                listener.updateSelectedItemCount(selectedCount,getCount());
            }
        });*/

/*        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageInfo imageInfo = (ImageInfo)getItem(position);
                if(imageInfo == null){
                   return;
                }
*//*                if(currMode == SELECT_MODE){
                    boolean status = false;
                    if(checkBoxStatusMap!=null && checkBoxStatusMap.containsKey(Integer.valueOf(position))){
                         status = checkBoxStatusMap.get(Integer.valueOf(position)).booleanValue();
                    }
                    holder.checkBox.setChecked(!status);
                    return;
                }*//*

              //  listener.previewCloudPhoto(imageInfo.getFile().getUrl());
                Log.d(TAG," cloud item convert view onclick,preview photo");
                listener.previewCloudPhoto(imageInfo,position);
            }
        });*/

/*        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(currMode == SELECT_MODE){
                    Log.d(TAG," select mode do nothing..return");
                   return false;
                }
                currMode = SELECT_MODE;
                refreshLayout = true;//when entry selected mode,need refresh the list item layout for checkbox do not refresh bug.
                checkBoxStatusMap.clear();
                for(int i=0;i<cloudList.size();i++){
                    if(i == position){
                        checkBoxStatusMap.put(Integer.valueOf(i),Boolean.TRUE);
                    }else{
                        checkBoxStatusMap.put(Integer.valueOf(i),Boolean.FALSE);
                    }

                }
                if(listener != null){
                    listener.onCloudItemLongClickListener(position);
                    listener.updateSelectedItemCount(1,getCount());
                }
                return false;
            }
        });*/
        ImageInfo imageInfo = cloudList.get(position);
        String url = imageInfo.getFile().getFileUrl();//. "http://img1.imgtn.bdimg.com/it/u=3634103621,488471414&fm=27&gp=0.jpg";
        Log.d(TAG," position is: " + position + " the url is: " + url );
        Glide.with(context).load(url).into(holder.cloudImageView);
        holder.imageName.setText(imageInfo.getDisplayName());
        holder.imageTime.setText(imageInfo.getCreatedAt());
        updateItemViewBackgrond(position,convertView);
/*       if(currMode == SELECT_MODE){
            boolean checked = checkBoxStatusMap.get(position).booleanValue();
            holder.checkBox.setChecked(checked);
            holder.checkBox.setVisibility(View.VISIBLE);
        }else{
            if(holder.checkBox.getVisibility() == View.VISIBLE){
                holder.checkBox.setVisibility(View.GONE);
            }
        }*/

        return convertView;
    }

    /**
     * seletedAll
     */
    public void seletedAll(){

/*        if(currMode != SELECT_MODE){
            return;
        }*/
        int count = getCount();
/*        for(int i=0;i<count;i++){
            checkBoxStatusMap.put(Integer.valueOf(i),Boolean.TRUE);
        }*/
       for(int i=0;i<count;i++){
           swipeMenuListView.setItemChecked(i,true);
       }
        notifyDataSetChanged();
        listener.updateSelectedItemCount(count,getCount());
    }

    /**
     * seletedAll
     */
    public void unseletedAll(){

/*        if(currMode != SELECT_MODE){
            return;
        }*/

 /*       for(int i=0;i<getCount();i++){
            checkBoxStatusMap.put(Integer.valueOf(i),Boolean.FALSE);
        }*/
        swipeMenuListView.clearChoices();
        notifyDataSetChanged();
        listener.updateSelectedItemCount(0,getCount());
    }

    private int getSelectedCount(){
        int selectedCount = 0;
/*        for(Integer position : checkBoxStatusMap.keySet()){
            if(checkBoxStatusMap.get(position).booleanValue()){
                selectedCount++;
            }
        }*/
        selectedCount = swipeMenuListView.getCheckedItemCount();
        return selectedCount;
    }

    private void updateMainSelectAllView(){
        int count = getSelectedCount();
        if(count == getCount()){
            listener.onCloudItemClickListener(true);
        }else{
            listener.onCloudItemClickListener(false);
        }

    }

    /**
     * get the selected imageInfo list ,for delete,download.
     * @return
     */
    public  HashMap<ImageInfo,Integer> getCloudSelectedPhotoData(){
        HashMap<ImageInfo,Integer> selectedMap = new HashMap<ImageInfo,Integer>();;
        SparseBooleanArray positionArray = swipeMenuListView.getCheckedItemPositions();
        if(positionArray == null || positionArray.size() == 0){
            Log.d(TAG," get cloud selected data is null.");
           return null;
        }
        for(int i=0;i<positionArray.size();i++){
           int position =  positionArray.keyAt(i);
           boolean isChecked = positionArray.get(position);
           Log.d(TAG," get cloud selected data ,the position is: " + position + " the value is: " + isChecked);
           if(isChecked){
               ImageInfo imageInfo = getItem(position);
               selectedMap.put(imageInfo,position);
           }
        }
/*            if(checkBoxStatusMap != null){
                selectedMap = new HashMap<ImageInfo,Integer>();
                for(Integer position : checkBoxStatusMap.keySet()){
                    if(checkBoxStatusMap.get(position).booleanValue()){
                         ImageInfo imageInfo = getItem(position.intValue());
                         selectedMap.put(imageInfo,position);
                    }
                }
            }*/
            return selectedMap;
    }

    /**
     * when the listview is muti choice mode,selected item need update the backgroud to show isSelected.
     * @param position the item position.
     */
    private void updateItemViewBackgrond(int position,View convertView){
        boolean isChecked = swipeMenuListView.isItemChecked(position);
        Log.d(TAG," updateItemViewBackgrond: position: "+ position + " isChecked: " + isChecked );
        if(isChecked){
            convertView.setBackgroundResource(R.drawable.item_selected);
        }else{
            convertView.setBackgroundResource(R.drawable.item_normal);
        }
    }
   /* public void removeCloudSelectMapData(Integer position){
        checkBoxStatusMap.remove(position);
    }*/
    private class ItemCloudHolder{
          ImageView cloudImageView;
          TextView imageName;
          TextView imageTime;
       //   CheckBox checkBox;
    }

    public interface CloudImageListener{
        public void onCloudItemLongClickListener(int position);
        public void onCloudItemClickListener(boolean isSelectAll);
        public void updateSelectedItemCount(int count,int total);
        public void previewCloudPhoto(String url);
        public void previewCloudPhoto(ImageInfo imageInfo,int position);
    }
}
