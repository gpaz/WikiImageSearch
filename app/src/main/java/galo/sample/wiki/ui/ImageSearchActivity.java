package galo.sample.wiki.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.File;

import galo.sample.wiki.R;
import galo.sample.wiki.api.ImageQueryResults;
import galo.sample.wiki.api.Page;
import galo.sample.wiki.search.WikiImageSearchFieldDelegate;

/**
 * An activity that presents a user with edit field that when the user types, an internet search will
 * occur using Wikipedia's api found at <a href=" https://en.wikipedia.org/w/api.php"> https://en.wikipedia.org/w/api.php</a>
 * for pages with thumbnail images matching a 96px size limit and the whatever the user enters as text.
 *
 * @author Galo
 */
public class ImageSearchActivity extends FragmentActivity implements WikiImageSearchFieldDelegate.ImageSearchResultListener, ThumbnailPageFragment.OnFragmentActionListener
{
    private static final String TAG_IMAGE_PAGE_PREVIEW_FRAG = "image_pane_preview_fragment";

    private EditText mSearchField;
    private WikiImageSearchFieldDelegate mSearchDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);
        mSearchField = (EditText) findViewById(R.id.search_field);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        FragmentManager fm = getSupportFragmentManager();

        if(fm.findFragmentByTag(TAG_IMAGE_PAGE_PREVIEW_FRAG) == null)
        {
            Fragment frag = ThumbnailPageFragment.newInstance(R.drawable.no_image_available_thumb);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, frag, TAG_IMAGE_PAGE_PREVIEW_FRAG).commit();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(mSearchDelegate == null)
            mSearchDelegate = new WikiImageSearchFieldDelegate(mSearchField, this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mSearchDelegate.detach();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if(isFinishing())
        {
            mSearchDelegate.detach();
            mSearchDelegate.clearCache();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Acts as a proxy between the fragment and the ImageSearchDelegate.
     */
    @Override
    public void onImageFound(Page page, File imageFile)
    {
        ThumbnailPageFragment fragment = getThumbnailFragment();
        if(fragment!=null)
            fragment.onImageFound(page, imageFile);
    }

    /**
     * Acts as a proxy between the fragment and the ImageSearchDelegate.
     */
    @Override
    public void onNewPageSet(ImageQueryResults newSetOfResults)
    {
        ThumbnailPageFragment fragment = getThumbnailFragment();
        if(fragment!=null)
            fragment.onNewPageSet(newSetOfResults);
    }

    /**
     * Acts as a proxy between the fragment and the ImageSearchDelegate.
     */
    @Override
    public void onImageNotFound(Page page)
    {
        ThumbnailPageFragment fragment = getThumbnailFragment();
        if(fragment!=null)
            fragment.onImageNotFound(page);
    }


    /**
     * Retrieves the ThumbnailPageFragment.
     * @return
     * The ThumbnailPageFragment or null if not found.
     */
    private ThumbnailPageFragment getThumbnailFragment()
    {
        return (ThumbnailPageFragment) getSupportFragmentManager().findFragmentByTag(TAG_IMAGE_PAGE_PREVIEW_FRAG);
    }

    @Override
    public void onImageSelected(View v, Page p)
    {
        // TODO: display full copy of image on a separate fragment.
    }
}
