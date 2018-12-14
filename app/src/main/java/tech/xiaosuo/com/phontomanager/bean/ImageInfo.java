package tech.xiaosuo.com.phontomanager.bean;

import org.json.JSONObject;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by wangshumin on 2017/10/10.
 */

public class ImageInfo extends BmobObject {
   // private Long imageId;
    private String displayName;
    private Long imgSize;
    private String mimeType;
    private String bucketName;
    private String data;
    private String md5;
   // private Integer userId;
    private BmobFile file;
    private UserInfo userInfo;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public BmobFile getFile() {
        return file;
    }

    public void setFile(BmobFile file) {
        this.file = file;
    }

/*    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

       public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
    public Long getImageId() {
        return imageId;
    }
*/
    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setImgSize(Long imgSize) {
        this.imgSize = imgSize;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setData(String data) {
        this.data = data;
    }



    public String getDisplayName() {
        return displayName;
    }

    public Long getImgSize() {
        return imgSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                /*"imageId=" + imageId +*/
                ", displayName='" + displayName + '\'' +
                ", imgSize=" + imgSize +
                ", mimeType='" + mimeType + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", data='" + data + '\'' +
               ", md5='" + md5 + '\'' +
               /*  ", userId='" + userId + '\'' +*/
                ", file='" + file.toString() + '\'' +
                '}';
    }

}
