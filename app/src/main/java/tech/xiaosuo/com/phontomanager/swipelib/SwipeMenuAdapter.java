package tech.xiaosuo.com.phontomanager.swipelib;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.WrapperListAdapter;

import tech.xiaosuo.com.phontomanager.R;
import tech.xiaosuo.com.phontomanager.swipelib.SwipeMenuView.OnSwipeItemClickListener;

/**
 *
 * @author wangshumin
 * @date 2018-12-18
 *
 */
public class SwipeMenuAdapter implements WrapperListAdapter,
		OnSwipeItemClickListener {

    private static final String TAG = "SwipeMenuAdapter";
    private ListAdapter mAdapter;
    private Context mContext;
    private SwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener;

    public SwipeMenuAdapter(Context context, ListAdapter adapter) {
        mAdapter = adapter;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mAdapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SwipeMenuLayout layout = null;
        SwipeMenuListView listView = (SwipeMenuListView) parent;
        if (convertView == null) {
            View contentView = mAdapter.getView(position, convertView, parent);
            SwipeMenu menu = new SwipeMenu(mContext);
            menu.setViewType(getItemViewType(position));
            createMenu(menu);
            SwipeMenuView menuView = new SwipeMenuView(menu,
                    (SwipeMenuListView) parent);
            menuView.setOnSwipeItemClickListener(this);
           // SwipeMenuListView listView = (SwipeMenuListView) parent;
            layout = new SwipeMenuLayout(contentView, menuView,
                    listView.getCloseInterpolator(),
                    listView.getOpenInterpolator());
            layout.setPosition(position);
        } else {
            layout = (SwipeMenuLayout) convertView;
            layout.closeMenu();
            layout.setPosition(position);
            View view = mAdapter.getView(position, layout.getContentView(),
                    parent);
        }
/*        if (mAdapter instanceof BaseSwipListAdapter) {
            boolean swipEnable = (((BaseSwipListAdapter) mAdapter).getSwipEnableByPosition(position));
            layout.setSwipEnable(swipEnable);
        }*/
        updateItemViewBackgrond(position,layout,listView);
        return layout;
    }

    public void createMenu(SwipeMenu menu) {
        // Test Code
        SwipeMenuItem item = new SwipeMenuItem(mContext);
        item.setTitle("Item 1");
        item.setBackground(new ColorDrawable(Color.GRAY));
        item.setWidth(300);
        menu.addMenuItem(item);

        item = new SwipeMenuItem(mContext);
        item.setTitle("Item 2");
        item.setBackground(new ColorDrawable(Color.RED));
        item.setWidth(300);
        menu.addMenuItem(item);
    }

    @Override
    public void onItemClick(SwipeMenuView view, SwipeMenu menu, int index) {
        if (onMenuItemClickListener != null) {
            onMenuItemClickListener.onMenuItemClick(view.getPosition(), menu,
                    index);
        }
    }

    public void setOnSwipeItemClickListener(
            SwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return mAdapter.isEnabled(position);
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public ListAdapter getWrappedAdapter() {
        return mAdapter;
    }

    /**
     * when the listview is muti choice mode,selected item need update the backgroud to show isSelected.
     * @param position the item position.
     */
    private void updateItemViewBackgrond(int position,SwipeMenuLayout convertView,SwipeMenuListView listView){
        boolean isChecked = listView.isItemChecked(position);
        Log.d(TAG," updateItemViewBackgrond: position: "+ position + " isChecked: " + isChecked );
        if(listView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
            convertView.setSwipEnable(false);
        }else{
            convertView.setSwipEnable(true);
        }
        if(isChecked){
            convertView.setBackgroundResource(R.drawable.item_selected);
        }else{
            convertView.setBackgroundResource(R.drawable.item_normal);
        }
    }
}
