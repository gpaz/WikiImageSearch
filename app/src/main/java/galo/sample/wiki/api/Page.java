package galo.sample.wiki.api;

/**
 * the 'page' portion of a Wikipedia API call.
 *
 * Created by Galo on 2/26/2015.
 */
public class Page {
    private int mPageId;
    private int mNs;
    private String mTitle;
    private int mIndex;
    private Thumbnail mThumbNail;

    public Page(int pageId, int ns, String title, int index, Thumbnail thumbNail)
    {
        mPageId = pageId;
        mNs = ns;
        mTitle = title;
        mIndex = index;
        mThumbNail = thumbNail;
    }

    public int getPageId() {

        return mPageId;
    }

    public int getNs() {
        return mNs;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getIndex() {
        return mIndex;
    }

    public Thumbnail getThumbNail() {
        return mThumbNail;
    }
}
