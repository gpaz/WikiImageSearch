package galo.sample.wiki.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import galo.sample.wiki.R;
import galo.sample.wiki.api.ImageQueryResults;
import galo.sample.wiki.api.Page;
import galo.sample.wiki.search.WikiImageSearchFieldDelegate;
import galo.sample.wiki.util.DeviceInfoUtil;

/**
 * A {@link Fragment} that loads images from a file and displays them in a grid view.
 * Activities that contain this fragment must implement the
 * {@link ThumbnailPageFragment.OnFragmentActionListener} interface
 * to handle interaction events.
 * Use the {@link ThumbnailPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThumbnailPageFragment extends Fragment implements WikiImageSearchFieldDelegate.ImageSearchResultListener
{
    private static final String ARG_ALT_IMG_RES = "alternateImageResource";
    private static final ThreadFactory IMAGE_LOADER_THREADFACTORY = new ThreadFactory()
    {
        @Override
        public Thread newThread(Runnable r)
        {
            return new Thread(r, "ImageLoaderThread");
        }
    };

    private ExecutorService mImageLoaderService;
    private int mAlternateImageResourceId;
    private Bitmap mAlternameImageBitmap;
    private final Object mAlternateImageBitmapLock = new Object();
    private ImageListAdapter mImageListAdapter;
    private GridView mImageGrid;
    private Comparator<Page> mPageComparator = new Comparator<Page>()
    {
        @Override
        public int compare(Page lhs, Page rhs)
        {
            return lhs.getIndex() - rhs.getIndex();
        }
    };

    private OnFragmentActionListener mListener;

    /**
     * Creates a thumbnail preview fragment with an image resource that is shown for those pages
     * without images.
     *
     * @param alternateImageResource
     * The resource id to set as an alternate image to a page if no image is found for it.
     * @return
     * A new instance of the fragment.
     */
    public static ThumbnailPageFragment newInstance(int alternateImageResource)
    {
        ThumbnailPageFragment fragment = new ThumbnailPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ALT_IMG_RES, alternateImageResource);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            // TODO: use this to notify activity of events on the images.
            mListener = (OnFragmentActionListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentActionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mAlternateImageResourceId = getArguments().getInt(ARG_ALT_IMG_RES);
        setRetainInstance(true);
        mImageLoaderService = Executors.newFixedThreadPool(DeviceInfoUtil.getNumberOfProcessors()*2, IMAGE_LOADER_THREADFACTORY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_image_page_preview, container, false);
        mImageGrid = (GridView) v.findViewById(R.id.image_preview_pane);
        if(mImageListAdapter == null)
            mImageListAdapter = new ImageListAdapter(getActivity(), R.layout.thumbnail_layout, new ArrayList<ImageLoadAndDisplayInterface>());
        mImageGrid.setAdapter(mImageListAdapter);

        return v;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        recycleAdapterImages();
        if(mAlternameImageBitmap != null)
            mAlternameImageBitmap.recycle();
    }

    private void recycleAdapterImages()
    {
        ImageListAdapter adapter = (ImageListAdapter) mImageGrid.getAdapter();
        adapter.clear();
    }

    @Override
    public void onNewPageSet(ImageQueryResults newSetOfResults)
    {
        ImageListAdapter adapter = (ImageListAdapter) mImageGrid.getAdapter();
        adapter.setNotifyOnChange(false);
        adapter.clear();
        List<Page> pages = new ArrayList<Page>(newSetOfResults.getPages());
        Collections.sort(pages, mPageComparator);
        for(Page p : pages)
            adapter.add(new ImageLoadAndDisplayInterface(p));
        adapter.setNotifyOnChange(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onImageFound(Page page, File imageFile)
    {
        final ImageLoadAndDisplayInterface pageWrapper = ((ImageListAdapter)mImageGrid.getAdapter())
                .getItem(page.getIndex() - 1);
        pageWrapper.setImageFile(imageFile);
        mImageLoaderService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                if(!pageWrapper.isRecycled())
                {
                    Activity activity = getActivity();
                    if(activity != null && pageWrapper.getImageFile().exists())
                    {
                        final Bitmap bmap = BitmapFactory.decodeFile(pageWrapper.getImageFile().getPath());
                        getActivity().runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run() {
                                if (!pageWrapper.isRecycled())
                                    pageWrapper.setBitmap(bmap, true, true);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onImageNotFound(Page page)
    {
        final ImageLoadAndDisplayInterface pageWrapper = ((ImageListAdapter)mImageGrid.getAdapter())
                .getItem(page.getIndex()-1);
        mImageLoaderService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                if(!pageWrapper.isRecycled())
                {
                    synchronized (mAlternateImageBitmapLock)
                    {
                        if(mAlternameImageBitmap == null)
                            mAlternameImageBitmap = BitmapFactory.decodeResource(getResources(), mAlternateImageResourceId);
                    }
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(!pageWrapper.isRecycled())
                                pageWrapper.setBitmap(mAlternameImageBitmap, false, false);
                        }
                    });
                }
            }
        });
    }

    // TODO: Not Used
    public interface OnFragmentActionListener
    {
        public void onImageSelected(View v, Page p);
    }
}
