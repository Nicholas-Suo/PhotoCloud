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
  //  HashMap<Integer,Boolean> checkBoxStatusMap;
    SwipeMenuListView swipeMenuListView;

    public SwipeMenuListView getSwipeMenuListView() {
        return swipeMenuListView;
    }

    public void setListener(CloudImageListener listener) {
        this.listener = listener;
    }

    public CloudImageAdapter(Context context, SwipeMenuListView swipeMenuListView){
            this.context = context;
            this.swipeMenuListView = swipeMenuListView;
           // checkBoxStatusMap = new HashMap<Integer, Boolean>();
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

        ImageInfo imageInfo = cloudList.get(position);
        String url = imageInfo.getFile().getFileUrl();//. "http://img1.imgtn.bdimg.com/it/u=3634103621,488471414&fm=27&gp=0.jpg";
        Log.d(TAG," position is: " + position + " the url is: " + url );
        Glide.with(context).load(url).into(holder.cloudImageView);
        holder.imageName.setText(imageInfo.getDisplayName());
        holder.imageTime.setText(imageInfo.getCreatedAt());
      //  updateItemViewBackgrond(position,convertView);
        return convertView;
    }

    /**
     * seletedAll
     */
    public void seletedAll(){
        int count = getCount();
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
        swipeMenuListView.clearChoices();
        notifyDataSetChanged();
        listener.updateSelectedItemCount(0,getCount());
    }

    private int getSelectedCount(){
        int selectedCount = 0;
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
            return selectedMap;
    }

/*    *//**
     * when the listview is muti choice mode,selected item need update the backgroud to show isSelected.
     * @param position the item position.
     *//*
    private void updateItemViewBackgrond(int position,View convertView){
        boolean isChecked = swipeMenuListView.isItemChecked(position);
        Log.d(TAG," updateItemViewBackgrond: position: "+ position + " isChecked: " + isChecked );
        if(isChecked){
            convertView.setBackgroundResource(R.drawable.item_selected);
        }else{
            convertView.setBackgroundResource(R.drawable.item_normal);
        }
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
