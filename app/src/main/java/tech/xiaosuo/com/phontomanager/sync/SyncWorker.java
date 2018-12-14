package tech.xiaosuo.com.phontomanager.sync;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import androidx.work.Worker;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import tech.xiaosuo.com.phontomanager.bean.ImageInfo;
import tech.xiaosuo.com.phontomanager.bean.UserInfo;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    @NonNull
    @Override
    public WorkerResult doWork() {
        Log.d(TAG," begin dowork sync cloud data,then insert db");
        fetchCloudData();
        return WorkerResult.SUCCESS;
    }

    private void fetchCloudData(){
        BmobQuery<ImageInfo> query = new BmobQuery<ImageInfo>();
        UserInfo userInfo = UserInfo.getCurrentUser(UserInfo.class);
        query.addWhereEqualTo("userInfo", userInfo);

        //返回50条数据，如果不加上这条语句，默认返回10条数据
        //query.setLimit(50);
        //执行查询方法
        query.findObjects(new FindListener<ImageInfo>() {
            @Override
            public void done(List<ImageInfo> list, BmobException e) {
                if(e==null){
                    Log.d(TAG, " fetchCloudData success the server list number: " + list.size());
                    for(int i=0;i<list.size();i++){
                        ImageInfo serverImageInfo = list.get(i);
                        String sData = serverImageInfo.getData();
                        Long sSize = serverImageInfo.getImgSize();
                        String sMd5 = serverImageInfo.getMd5();
                    }
                }
            }

        });
    }
}
