package galo.sample.wiki.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * An array adapter strictly used in conjunction with {@link ImageLoadAndDisplayInterface}
 * objects at an attempt to load images on a non-ui thread and to have a hook into the ImageViews
 * that are supposed to display them, if available at the time.  Since GridView is a dynamic UX
 * element like a ListView, a view may not belong to the image once loaded.  So signals are sent
 * through that synchronized interface to determine what and when images should be loaded.
 *
 * Note: An issues was found between my implementation and the Grid implementation.  Consistently,
 * when loading the images to the grid, the first cell in the upper-left most corner of the grid
 * would show up BLANK until one scrolls down and then back up and then an image would display.
 *
 * There are many ways to fix this, one simple way would be to load all the images and then notify
 * the adapter to readjust.  I chose to stick with 'display as they come' type of logic to complete.
 *
 * Created by Galo on 2/27/2015.
 */
public class ImageListAdapter extends ArrayAdapter<ImageLoadAndDisplayInterface>
{
    int mImageViewResource;

    public ImageListAdapter(Context context, int imageViewResource, List<ImageLoadAndDisplayInterface> objects)
    {
        super(context, 0, 0, objects);
        mImageViewResource = imageViewResource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView iv;
        if (convertView == null)
        {
            iv = (ImageView) LayoutInflater.from(getContext()).inflate(mImageViewResource, parent, false);
        }
        else
        {
            iv = (ImageView) convertView;
            /*ImageLoadAndDisplayInterface previousWrapper = (ImageLoadAndDisplayInterface) iv.getTag();
            // null-check only because of hack below.
            if(previousWrapper != null)
                previousWrapper.bindImageView(null);*/
        }

        // HACK HACK HACK
        // This hack assumes that the 5th element in the stack trace was and always will be the method
        // 'makeAndAddView' when the ACTUAL ensures that the rest of these commands only get called
        // when the GridView doesn't just want to measure and then discard, but to keep and show.
        // --> Issue Resolved (for now): first grid cell in the top left corner would not
        // Note* This might also be resolved by having a list or a stack of weak references to the
        // Views and use instead 'newWrapper.addImageViewReference(ImageView)...For future reference.
        if(isCreatingToRetain())
        {
            ImageLoadAndDisplayInterface newWrapper = getItem(position);
            newWrapper.bindImageView(iv);
            iv.setTag(newWrapper);
        }
        return iv;
    }

    @Override
    public void clear()
    {
        int size = getCount();
        for(int i = 0; i < size; i++)
        {
            ImageLoadAndDisplayInterface pageWrapper = getItem(i);
            pageWrapper.recycle();
        }
        super.clear();
    }

    private boolean isCreatingToRetain()
    {
        return Thread.currentThread().getStackTrace()[5].getMethodName().equals("makeAndAddView");
    }
}
