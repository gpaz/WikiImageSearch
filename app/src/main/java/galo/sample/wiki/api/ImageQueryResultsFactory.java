package galo.sample.wiki.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple class that generates an object structure base off of the return Wikipedia API JSON structure
 * sent as the content in a response after a successful query.
 *
 * Created by Galo on 2/26/2015.
 */
public class ImageQueryResultsFactory
{
    private static final String EMPTY_STRING = "";
    private static final String REQUESTID = "requestid",
                                QUERY = "query",
                                PAGES = "pages",
                                PAGEID = "pageid",
                                NS = "ns",
                                TITLE = "title",
                                INDEX = "index",
                                THUMB = "thumbnail",
                                THUMB_SRC = "source",
                                THUMB_WIDTH = "width",
                                THUMB_HEIGHT = "height";

    public static ImageQueryResults parseJSON(String jsonStr)
    {
        final List<Page> pageList = new LinkedList<Page>();
        String requestId = "";
        try
        {
            JSONObject root = new JSONObject(jsonStr);
            requestId = root.has(REQUESTID) ? root.getString(REQUESTID) : EMPTY_STRING;
            JSONObject pages = root.getJSONObject(QUERY).getJSONObject(PAGES);
            Iterator<String> keyIterator = pages.keys();
            while(keyIterator.hasNext())
            {
                String key = keyIterator.next();
                JSONObject page = pages.getJSONObject(key);
                int pageId = page.getInt(PAGEID);
                int ns = page.getInt(NS);
                String title = page.getString(TITLE);
                int index = page.getInt(INDEX);

                final Thumbnail thumbnail;
                if(page.has(THUMB))
                {
                    JSONObject thumbObj = page.getJSONObject(THUMB);
                    String source = thumbObj.getString(THUMB_SRC);
                    int width = thumbObj.getInt(THUMB_WIDTH);
                    int height = thumbObj.getInt(THUMB_HEIGHT);
                    thumbnail = new Thumbnail(source, width, height);
                }
                else
                    thumbnail = null;
                // append to the list
                pageList.add(new Page(pageId, ns, title, index, thumbnail));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return new ImageQueryResults(pageList, requestId);
    }
}
