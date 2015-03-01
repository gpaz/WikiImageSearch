package galo.sample.wiki.api;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the basic structure and provide the essential portions that are needed to form a Wikipedia API
 * thumbnail image query over http.  Things that are changeable are the Search String, the max image size
 * and the max number of results desired.
 *
 * Created by Galo on 2/26/2015.
 */
public class WikiImageQuery
{
    /*
     * https://en.wikipedia.org/w/api.php?
     * action=query&
     * prop=pageimages&
     * format=json&
     * piprop=thumbnail&
     * pithumbsize=96&          : maximum width (or height, whichever is larger) of images returned. some pages might not have an associated image
     * pilimit=50&              : maximum number of images to return (should be the same as gpslimit).
     * generator=prefixsearch&
     * gpssearch=Cat&           : search term for which results will be returned.
     * gpslimit=50              : maximum number of search results to return.
     */

    private static final String URL_FORMAT = "https://en.wikipedia.org/w/api.php?action=query&" +
                                             "requestid=%sprop=pageimages&format=json&piprop=thumbnail&" +
                                             "pithumbsize=%d&pilimit=%d&generator=prefixsearch&" +
                                             "gpssearch=%s&gpslimit=%3$d";

    public static final String HOST_ADDRESS = "https://en.wikipedia.org/w/api.php",
                                ACTION_KEY = "action",
                                ACTION_VALUE = "query",
                                PROP_KEY = "prop",
                                PROP_VALUE = "pageimages",
                                FORMAT_KEY = "format",
                                FORMAT_VALUE = "json",
                                PIPROP_KEY = "piprop",
                                PIPROP_VALUE = "thumbnail",
                                GENERATOR_KEY = "generator",
                                GENERATOR_VALUE = "prefixsearch";


    public static final String REQUESTID_KEY = "requestid",
                               PITHUMBSIZE_KEY = "pithumbsize",
                               PILIMIT_KEY = "pilimit",
                               GPSSEARCH_KEY = "gpssearch",
                               GPSLIMIT_KEY = "gpslimit";


    private final String mSearchTerm,
                         mRequestId;
    private final int mMaxThumbSize,
                      mRecordCount;

    public WikiImageQuery(String searchTerm, String requestId, int maxThumbSize, int recordCount)
    {
        mSearchTerm = searchTerm == null ? "" : searchTerm;
        mMaxThumbSize = maxThumbSize;
        mRecordCount = recordCount;
        mRequestId = requestId;
    }

    public String getSearchTerm()
    {
        return mSearchTerm;
    }

    public String getRequestId() {
        return mRequestId;
    }

    public int getRecordCount() {
        return mRecordCount;
    }

    public int getMaxThumbSize() {
        return mMaxThumbSize;
    }

    /**
     * Generates a full url query you would enter into the Url field in a browser.
     * @return
     * url query string.
     */
    public String generateImageQuery()
    {
        return String.format(URL_FORMAT, mRequestId, mMaxThumbSize, mRecordCount, mSearchTerm);
    }

    /**
     * Generates an encoded string used when providing the query portion of the url either in a Get
     * or in a Post request.
     * @return
     * Returns an encoded url entity string, just containing the url query
     */
    public String generateEncodedEntityString()
    {
        List<NameValuePair> nvp = new ArrayList<NameValuePair>(10);
        nvp.add(new BasicNameValuePair(ACTION_KEY, ACTION_VALUE));
        nvp.add(new BasicNameValuePair(PROP_KEY, PROP_VALUE));
        nvp.add(new BasicNameValuePair(FORMAT_KEY, FORMAT_VALUE));
        nvp.add(new BasicNameValuePair(PIPROP_KEY, PIPROP_VALUE));
        nvp.add(new BasicNameValuePair(GENERATOR_KEY, GENERATOR_VALUE));
        nvp.add(new BasicNameValuePair(REQUESTID_KEY, mRequestId));
        nvp.add(new BasicNameValuePair(PITHUMBSIZE_KEY, Integer.toString(mMaxThumbSize)));
        nvp.add(new BasicNameValuePair(PILIMIT_KEY, Integer.toString(mRecordCount)));
        nvp.add(new BasicNameValuePair(GPSLIMIT_KEY, Integer.toString(mRecordCount)));
        nvp.add(new BasicNameValuePair(GPSSEARCH_KEY, mSearchTerm));
        return URLEncodedUtils.format(nvp, HTTP.UTF_8);
    }

    /**
     * Returns the static Host Address of <a href="https://en.wikipedia.org/w/api.php">https://en.wikipedia.org/w/api.php</a>.
     * @return
     * The host address for the query.
     */
    public String getHostAddress()
    {
        return HOST_ADDRESS;
    }
}
