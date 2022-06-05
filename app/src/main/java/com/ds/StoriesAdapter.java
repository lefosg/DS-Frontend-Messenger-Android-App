package com.ds;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.io.File;
import java.util.ArrayList;


public class StoriesAdapter extends PagerAdapter {

    private Context context;
    ArrayList<File> files;

    StoriesAdapter(Context context, ArrayList<File> files) {
        this.context = context;
        this.files = files;
    }

    public void setFilesList(ArrayList<File> files) {
        this.files = files;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //imageView.setImageResource(imageArray[position]);
        imageView.setImageBitmap(BitmapFactory.decodeFile(files.get(position).getAbsolutePath()));
        System.out.println(files.get(position).getAbsolutePath());
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView)object);
    }
}
