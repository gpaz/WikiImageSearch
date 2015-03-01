package galo.sample.wiki.search;

import galo.sample.wiki.api.ImageQueryResults;

/**\
 * Listener interface for the {@link WikiPageSearchController} and given to the {@link SearchResultListener}
 * to update its success or failure of an attempted query.
 *
 * Created by Galo on 2/27/2015.
 */
public interface SearchResultListener
{
    /**
     * Called whenever a result has been received.  This will get call on the same handler thread
     * that the {@link WikiPageSearchController} was created on, most-likely the Main Looper thread.
     *
     * @param results
     * The parsed object structure returned from a Wikipedia API call.
     */
    public void onSearchResultReceived(ImageQueryResults results);

    /**
     * Called when a Network error or other exception has taken place which caused normal operation
     * to discontinue.
     *
     * @param resultError
     * Information about the error that occurred.
     */
    public void onRemoteSearchError(ImageQueryError resultError);
}
