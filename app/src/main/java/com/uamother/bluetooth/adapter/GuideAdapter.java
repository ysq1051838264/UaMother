package com.uamother.bluetooth.adapter;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * 标题：给程序启动的的引导界面设置adapter
 */
public class GuideAdapter extends PagerAdapter {

    private List<ImageView> list;


    public GuideAdapter(List<ImageView> list) {

        this.list = list;
    }
    @Override
    public int getCount() {

        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView(list.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(list.get(position), 0);
        return list.get(position);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

}
