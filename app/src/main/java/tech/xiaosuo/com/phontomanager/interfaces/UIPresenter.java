package tech.xiaosuo.com.phontomanager.interfaces;

public interface UIPresenter {
    public static final boolean ANIMATION_START = true;
    public static final boolean ANIMATION_STOP = false;

    public static final int MSG_IMG_LIST_IS_EMPERTY = 0;
    public static final int MSG_UPDATE_NOTIFICATION = 1;
    public static final int MSG_NETWORK_IS_NOT_CONNECT = 2;
    public static final int MSG_BACKUP_IMG_FAIL = 3;
    public static final int MSG_PLS_CHECK_PERMISSION = 4;
    public static final int MSG_NO_NEED_BACKUP_OR_RESTORE = 5;
    public static final int MSG_SHOW_SUCCESS = 6;
    public static final int MSG_UPLOAD_FINISH = 7;
    public static final int  MSG_RM_KEY_FROM_ADAPTER = 8;
    public static final int  MSG_UPDATE_CHECKBOX_UNCHECKED = 9;

    public void uploadAnimation(boolean flag,int position);
    public void sendMsgToMain(int msgId,int param);
    public void saveImageUploadStatus(String md5);//we use md5 to check whether the image is same as the server image.
}
