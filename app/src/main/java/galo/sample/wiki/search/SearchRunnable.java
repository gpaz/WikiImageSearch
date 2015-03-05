package galo.sample.wiki.search;

import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.util.Scanner;

import galo.sample.wiki.api.ImageQueryResults;
import galo.sample.wiki.api.ImageQueryResultsFactory;
import galo.sample.wiki.api.WikiImageQuery;

/**
 * Meant to be performed on a separate thread than the Main Looper Thread, runs the Wiki Api
 * query.  It is responsible for adding its process id to the process map, checking throughout its
 * processing to make sure it has not been cancelled (removed from the process map) and then, perform wikipedia internet search api call and jwhen
 * complete removes itself from the process map, finally checking one last time if had been canceled
 * and then proceeding to call the SearchResultListener if not cancelled.
 *
 * Created by Galo on 2/27/2015.
 */
class SearchRunnable implements Runnable
{
    private SearchResultListener mListener;
    private ProcessStateMap mPSMap;
    private Handler mHandler;
    private WikiImageQuery mQuery;

    SearchRunnable(SearchResultListener listener, ProcessStateMap psMap, Handler handler, WikiImageQuery query)
    {
        // handle all failure cases for a null object here to quickly inform the developer of wrongdoing.
        if(listener == null)
            throw new NullPointerException("The listener must not be null.");
        if(psMap == null)
            throw new NullPointerException("The input process state map cannot be null.");
        if(handler == null)
            throw new NullPointerException("The handler process cannot be null.");
        if(query == null)
            throw new NullPointerException("The query cannot be null.");
        mListener = listener;
        mPSMap = psMap;
        mHandler = handler;
        mQuery = query;
        mPSMap.addProcess(mQuery.getRequestId());
    }

    @Override
    public void run()
    {
        // check cancelled
        if(!mPSMap.contains(mQuery.getRequestId()))
            return;

        // create the request
        HttpPost postRequest = new HttpPost(mQuery.getHostAddress());
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
        Scanner inScanner = null;
        String jsonString = null;
        final ImageQueryError queryError = new ImageQueryError();
        try
        {
            HttpEntity queryEntity = new StringEntity(mQuery.generateEncodedEntityString(), HTTP.UTF_8);
            postRequest.setEntity(queryEntity);

            // execute and wait for response.
            HttpResponse httpResponse = httpClient.execute(postRequest);

            // check if cancelled while waiting for response.
            if(!mPSMap.contains(mQuery.getRequestId()))
                return;

            // handle network error code, non error is 200 ok
            StatusLine statusLine = httpResponse.getStatusLine();
            if(httpResponse.getStatusLine().getStatusCode() != 200)
            {
                queryError.status = statusLine;
                throw new IllegalStateException("Received error code from response.");
            }

            // read json string
            inScanner = new Scanner(httpResponse.getEntity().getContent());
            StringBuilder sb = new StringBuilder();
            while(inScanner.hasNextLine())
                sb.append(inScanner.nextLine());

            jsonString = sb.toString();
            Log.d("json",jsonString);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            queryError.msg = e.getMessage();
        }
        finally
        {
            if(inScanner != null)
                inScanner.close();
            httpClient.close();
        }

        if(queryError.msg != null)
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if(mPSMap.removeProcess(mQuery.getRequestId()))
                    {
                        queryError.requestId = mQuery.getRequestId();
                        mListener.onRemoteSearchError(queryError);
                    }
                }
            });
        }
        else
        {
            final ImageQueryResults results = ImageQueryResultsFactory.parseJSON(jsonString);
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // last check for process cancellation before committing to the callback.
                    if (mPSMap.removeProcess(mQuery.getRequestId()))
                        mListener.onSearchResultReceived(results);
                }
            });
        }
    }
}