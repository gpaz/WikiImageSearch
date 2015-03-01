package galo.sample.wiki.image;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import galo.sample.wiki.api.Page;
import galo.sample.wiki.api.Thumbnail;

/**
 * An engine that loads images from a URL into the local cache and returns the file associated with
 * that image url.
 *
 * Created by Galo on 2/27/2015.
 */
public class RemoteImageCache
{
    private static final String DEFAULT_PATH = "remoteimages";
    private Context mContext;
    private File mCacheDir;


    public RemoteImageCache(Context context)
    {
        mContext = context;
        mCacheDir = new File(mContext.getCacheDir(), DEFAULT_PATH);
        if(!mCacheDir.exists())
            mCacheDir.mkdirs();
    }

    /**
     * Clears the disk cache of images.
     */
    public void clearCache()
    {
        for(File file : mCacheDir.listFiles())
        {
            file.delete();
        }
    }

    /**
     * Retrieves the file associated with the page, downloading it if necessary from the internet.
     * NOTE: You must have permissions to access the internet.
     *
     * TODO: make generic for URL, not for Page.  Extend the class for using Page.
     * @param page
     * The Page object containing Source information about the thumbnail image to download.
     * @return
     * The local file with the image for the Page.  Null could happen if there is no image associated
     * with the Page or if a network or file io error occurred, so a check for null should be done
     * before doing any work on the File.
     */
    public File getLocalCacheFileForPage(Page page)
    {
        Thumbnail thumbInfo;
        File cachedFile = null;
        if(page != null && (thumbInfo = page.getThumbNail()) != null)
        {
            // construct file name
            Uri srcUri = Uri.parse(thumbInfo.getSource());
            cachedFile = new File(mCacheDir
                    , String.valueOf(page.getPageId()) + "__" + srcUri.getLastPathSegment());
            if(!cachedFile.exists())
            {
                boolean bExceptionOccurred = false;
                // load from uri
                FileOutputStream fos = null;
                InputStream inStream = null;
                try
                {
                    URL imgUrl = new URL(srcUri.toString());
                    inStream = imgUrl.openStream();
                    fos = new FileOutputStream(cachedFile);
                    byte[] buffer = new byte[4096];
                    int nBytes;
                    while((nBytes = inStream.read(buffer)) > 0)
                    {
                        fos.write(buffer, 0, nBytes);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    bExceptionOccurred = true;
                }
                finally
                {
                    if(fos != null)
                    {
                        try {
                            fos.close();
                        } catch (IOException e) {/*do nothing*/}
                    }
                    if(inStream != null)
                    {
                        try {
                            inStream.close();
                        } catch (IOException e) {/*do nothing*/}
                    }
                }

                if(bExceptionOccurred)
                {
                    cachedFile.delete();
                    cachedFile = null;
                }
            }
        }

        return cachedFile;
    }
}
