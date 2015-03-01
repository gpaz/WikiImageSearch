package galo.sample.wiki.api;

import java.util.Collections;
import java.util.List;

/**
 * Following the JSON string returned by the Wikipedia API.  Examples of the return structure can
 * be found at <a href="https://www.mediawiki.org/wiki/API:Data_formats">https://www.mediawiki.org/wiki/API:Data_formats</a>
 * and the full specifications can be found at <a href=" https://en.wikipedia.org/w/api.php"> https://en.wikipedia.org/w/api.php</a>.
 *
 * Created by Galo on 2/26/2015.
 */
public class ImageQueryResults
{
    private List<Page> mPages;
    private String mRequestId;

    public ImageQueryResults(List<Page> pages, String requestId)
    {
        mPages = Collections.unmodifiableList(pages);
        mRequestId = requestId == null ? "" : requestId;
    }

    public List<Page> getPages()
    {
        return mPages;
    }

    public String getRequestId()
    {
        return mRequestId;
    }
}
