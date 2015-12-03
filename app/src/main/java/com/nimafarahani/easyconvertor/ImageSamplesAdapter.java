package com.nimafarahani.easyconvertor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.yazeed44.imagepicker.model.ImageEntry;

import java.util.ArrayList;

/**
 * Created by Sledd on 11/21/2015.
 *
 *
 Daniel Sledd
 David Clay
 Nima Farahani

 **This is the recycle view adapter**

 */
public class ImageSamplesAdapter extends RecyclerView.Adapter<ImageSampleViewHolder>{


    private Context context;
    private ArrayList<ImageEntry> mSelectedImages;
    public ImageSamplesAdapter(ArrayList<ImageEntry> myDataset, Context myContext){
        mSelectedImages = myDataset;
        context = myContext;
    }


    @Override
    public ImageSampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ImageView imageView = new ImageView(parent.getContext());
        return new ImageSampleViewHolder(imageView);
    }

    // loads image thumbnails
    @Override
    public void onBindViewHolder(ImageSampleViewHolder holder, int position) {

        final String path = mSelectedImages.get(position).path;
        loadImage(path, holder.thumbnail);
    }

    // count of selected images
    @Override
    public int getItemCount() {
        if (mSelectedImages == null)
            return 0;
        else
            return mSelectedImages.size();
    }


    private void loadImage(final String path, final ImageView imageView) {
        imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 440));

        Glide.with(context)
                .load(path)
                .asBitmap()
                .into(imageView);


    }


}

class ImageSampleViewHolder extends RecyclerView.ViewHolder {

    protected ImageView thumbnail;

    public ImageSampleViewHolder(View itemView) {
        super(itemView);
        thumbnail = (ImageView) itemView;
    }
}

