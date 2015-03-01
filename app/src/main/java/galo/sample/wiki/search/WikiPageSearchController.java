package galo.sample.wiki.search;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import galo.sample.wiki.api.WikiImageQuery;
import galo.sample.wiki.api.WikiImageQueryFactory;

/**
 * Processes queries by avenue of an {@link SearchRunnable} object to the Wikipedia API calling on
 * the listener whenever data has been received and parsed.
 *
 * Created by Galo on 2/26/2015.
 */
public class WikiPageSearchController
{
    private int mMaxThumbSize,
                mRecordCount;

    private Handler mHandler;
    private ExecutorService mExecutorService;

    private ProcessStateMap mProcessStateMap;

    /**
     *
     * @param maxThumbSize
     * Specifies the maximum width or height dimension an image can be returned from the query.
     * @param recordCount
     * Specifes the maximum number of records that should come back.  Please Note, however, that the
     * size may return more than specified here.  For example, on occasion I will request 50, but I
     * am returned 51 records.
     */
    public WikiPageSearchController(int maxThumbSize, int recordCount)
    {
        mMaxThumbSize = maxThumbSize;
        mRecordCount = recordCount;
        mHandler = new Handler();
        mProcessStateMap = new ProcessStateMap();
        mExecutorService = Executors.newCachedThreadPool(new ThreadFactory()
        {
            @Override
            public Thread newThread(Runnable r)
            {
                return new Thread(r,"RemoteSearchThread");
            }
        });
    }

    /**
     * Starts a query to the Wikipedia api and returns a uniquely identifiable request string to so
     * that one can compare and/or associate one query with the returning results, since multiple
     * requests can be performing at the same time.  You can use this request id to also cancel running
     * tasks from coming back to the Listener with data or stop possibly queued tasks to not perform.
     *
     * @param text
     * The text to search with.
     * @param listener
     * The listener that should be notified when a result comes in.
     * @return
     * A unique process id that can be used to identify and cancel the requested process.
     */
    public String searchAsync(String text, SearchResultListener listener)
    {
        WikiImageQuery query = WikiImageQueryFactory.generateUniquelyIndentifiableQuery(text, mMaxThumbSize, mRecordCount);
        String requestId = query.getRequestId();
        mExecutorService.execute(new SearchRunnable(listener, mProcessStateMap, mHandler, query));
        return requestId;
    }

    /**
     * Use the search id returned from the {@link galo.sample.wiki.search.WikiPageSearchController#searchAsync}
     * to cancel a process.
     * @param processId
     * The process id returned by the {@link WikiPageSearchController#searchAsync(String, SearchResultListener)} call.
     * @return
     * true if the process was successfully canceled, false if the process has concluded, was previously cancelled
     * , or was never a process.
     */
    public boolean cancel(String processId)
    {
        return mProcessStateMap.removeProcess(processId);
    }

    /**
     * Cancels all processes
     */
    public void cancelAllRunningSearchProcesses()
    {
        mProcessStateMap.removeAllRunningProcesses();
    }
}
