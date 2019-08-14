package com.acentria.benslist.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.acentria.benslist.R;
import com.acentria.benslist.response.SlidImgResponse;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private String img_url = "";
    private Integer[] images = {R.mipmap.in, R.mipmap.au, R.mipmap.md};
    private List<SlidImgResponse> mlist;

    public ViewPagerAdapter(Context context, List<SlidImgResponse> list) {
        this.context = context;
        this.mlist = list;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.custom_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
//        imageView.setImageResource(images[position]);
        img_url = "https://www.benslist.com/files/slides/" + mlist.get(position).getPicture();
        Glide.with(context).load(img_url).placeholder(R.mipmap.no_image).diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().into(imageView);


        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}
