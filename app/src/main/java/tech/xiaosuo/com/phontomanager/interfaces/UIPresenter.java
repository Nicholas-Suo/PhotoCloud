package tech.xiaosuo.com.phontomanager.interfaces;

public interface UIPresenter {
    public static final boolean ANIMATION_START = true;
    public static final boolean ANIMATION_STOP = false;
    public void uploadAnimation(boolean flag,int position);
}
