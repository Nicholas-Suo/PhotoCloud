package tech.xiaosuo.com.phontomanager.bean;

import android.provider.BaseColumns;
import android.provider.SyncStateContract;

/**
 * Created by wangshumin on 6/6/2018.
 */

public class PhotoInfoTable implements BaseColumns {

    public static final String PHOTO_INFO_TABLE = "PhotoInfo";
    public static final String COLUMN_IMAGE_NAME = "name";
    public static final String COLUMN_IMAGE_DATA = "data";
    public static final String COLUMN_IMAGE_MD5 = "md5";
    public static final String COLUMN_IMAGE_IS_IN_CLOUD = "cloud";
}
