package galo.sample.wiki.search;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import galo.sample.wiki.R;
import galo.sample.wiki.api.ImageQueryResults;
import galo.sample.wiki.api.Page;
import galo.sample.wiki.image.RemoteImageCache;
import galo.sample.wiki.util.DeviceInfoUtil;

/**
 * Takes care of all of the tasks associated with searching for relevant pages using Wikipedia's
 * API found at <a href=" https://en.wikipedia.org/w/api.php"> https://en.wikipedia.org/w/api.php</a>,
 * loading the results into objects, taking care of downloading found image sources to local cache,
 * and returning the files with which to load to the ImageSearchResultListener.  It will also register
 * a broadcast receiver to receive network state changes and enable or disable the edittext based on the
 * network state
 *
 * Created by Galo on 2/27/2015.
 */
public class WikiImageSearchFieldDelegate implements SearchResultListener
{

    private static final String EMPTY_STRING = "";
    private static final int MAX_IMG_COUNT = 50;
    private static final int MAX_IMG_SIZE = 96;

    private static final long DEFAULT_SEARCH_DELAY = 500;

    private EditText mSearchField;
    private WikiPageSearchController mSearchManager;
    private RemoteImageCache mImageCache;
    private ImageSearchResultListener mImageSearchResultListener;
    private Handler mHandler;
    private TextWatcher mTextWatcher;
    private BroadcastReceiver mNetworkChangeReceiver;
    private boolean mIsDetached;

    private ThreadFactory mRemoteCacheExecutorThreadFactory;
    private ExecutorService mRemoteCacheExecutor;

    private volatile String mCurrentSearchId = "";

    public WikiImageSearchFieldDelegate(EditText searchField, ImageSearchResultListener listener)
    {
        mSearchField = searchField;
        mImageSearchResultListener = listener;
        mSearchManager = new WikiPageSearchController(MAX_IMG_SIZE, MAX_IMG_COUNT);
        mImageCache = new RemoteImageCache(searchField.getContext());
        mHandler = new Handler();
        mRemoteCacheExecutorThreadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "UrlFileLoaderThread");
            }
        };
        mRemoteCacheExecutor = null;
        registerConnectivityReceiver();
        registerSearchFieldTextWatcher();
    }

    private void registerSearchFieldTextWatcher()
    {
        mSearchField.addTextChangedListener(mTextWatcher = new TextWatcher() {
            Timer mExecuteSearchTimer = new Timer("SearchExecutionTimer");
            TimerTask mSearchTask;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                // cancel any preceding operations scheduled
                mSearchManager.cancel(mCurrentSearchId);
                if (mSearchTask != null) {
                    mSearchTask.cancel();
                }
                // cancel callbacks for that last request, assuming that request ids are never the EMPTY_STRING
                mCurrentSearchId = EMPTY_STRING;
                if (!s.toString().isEmpty()) {
                    // schedule Wiki Api search, giving time to the user to enter more text before commiting to the search.
                    mSearchTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (DeviceInfoUtil.checkInternetConnectivity(mSearchField.getContext()))
                                mCurrentSearchId = mSearchManager.searchAsync(s.toString(), WikiImageSearchFieldDelegate.this);
                            else
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mSearchField.getContext().getApplicationContext()
                                                , "Unable to connect to network.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        }
                    };
                    mExecuteSearchTimer.schedule(mSearchTask, DEFAULT_SEARCH_DELAY);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing here.
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                if (mExecuteSearchTimer != null) {
                    mExecuteSearchTimer.cancel();
                    mExecuteSearchTimer.purge();
                    mExecuteSearchTimer = null;
                }
            }
        });
    }

    private void registerConnectivityReceiver()
    {
        mSearchField.getContext().registerReceiver(mNetworkChangeReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
                {
                    NetworkInfo netInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if(netInfo != null && mSearchField != null)
                    {
                        boolean bEnableSearch = netInfo.isConnectedOrConnecting();
                        mSearchField.setEnabled(bEnableSearch);
                        mSearchField.setHint(bEnableSearch ? R.string.search_hint : R.string.network_disconnected_hint);
                    }
                }
            }
        },new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onSearchResultReceived(ImageQueryResults results)
    {
        final String requestId = results.getRequestId();
        if(mCurrentSearchId.equals(requestId))
        {
            if(mRemoteCacheExecutor != null)
            {
                mRemoteCacheExecutor.shutdownNow();
                try
                {
                    mRemoteCacheExecutor.awaitTermination(200, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException e)
                {
                    Log.w(WikiImageSearchFieldDelegate.class.getSimpleName(), "Executor interrupted", e);
                }
            }
            mRemoteCacheExecutor = Executors.newFixedThreadPool(DeviceInfoUtil.getNumberOrAvailableVMProcessors()*2, mRemoteCacheExecutorThreadFactory);
            ImageSearchResultListener listener = mImageSearchResultListener;
            if(listener != null)
                listener.onNewPageSet(results);
            // load the images

            List<Page> pages = results.getPages();
            for(final Page p : pages) {
                mRemoteCacheExecutor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final File imgFile = mImageCache.getLocalCacheFileForPage(p);
                        if(!Thread.currentThread().isInterrupted())
                        {
                            mHandler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // final check before sending
                                    if (mCurrentSearchId.equals(requestId))
                                    {
                                        ImageSearchResultListener listener = mImageSearchResultListener;
                                        if(listener != null)
                                        {
                                            // fork based on whether imgFile exists.
                                            if (imgFile != null)
                                                listener.onImageFound(p, imgFile);
                                            else
                                                listener.onImageNotFound(p);
                                        }
                                    }

                                } // end run() on handler thread.
                            });
                        }
                    }// end run
                });
            } // end for-loop
        }
    }

    /**
     * detaches from
     */
    public synchronized void detach()
    {
        if(!mIsDetached)
        {
            mImageSearchResultListener = null;
            mCurrentSearchId = EMPTY_STRING;
            if (mTextWatcher != null)
                mSearchField.removeTextChangedListener(mTextWatcher);
            mSearchField.getContext().unregisterReceiver(mNetworkChangeReceiver);
            mSearchField = null;
            mTextWatcher = null;
            mSearchManager.cancelAllRunningSearchProcesses();
            mIsDetached = true;
        }
    }

    /**
     * explicitly clears the disk cache.
     */
    public void clearCache()
    {
        mImageCache.clearCache();
    }

    @Override
    public void onRemoteSearchError(ImageQueryError resultError)
    {
        // TODO: do some error handling an return to the ImageSearchResultListener
    }

    public static interface ImageSearchResultListener
    {
        public void onImageFound(Page page, File imgFile);
        public void onNewPageSet(ImageQueryResults newSetOfResults);
        public void onImageNotFound(Page page);
    }
}
