package galo.sample.wiki.api;

/**
 * The 'thumbnail' portion of a Wikipedia API call result.  Has information about the size and the url
 * to download it from.  This does not store any bitmap nor file representation of the thumbnail, only
 * the textual information sent from the results of a query.  This is part of the 'page' JSON object.
 * Created by Galo on 2/26/2015.
 */
public class Thumbnail
{
    String mSource;
    int mWidth;
    int mHeight;

    Thumbnail(String source, int width, int height)
    {
        mSource = source;
        mWidth = width;
        mHeight = height;
    }

    public String getSource() {
        return mSource;
    }

    public int getWidth()
    {
        return mWidth;
    }

    public int getHeight()
    {
        return mHeight;
    }
}
