package upp.foodonet.material;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;


import java.util.ArrayList;

import Adapters.IOnPublicationFromListSelected;
import Adapters.MyPublicationsListRecyclerViewAdapter;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;

public class MyPublicationsActivity
        extends FooDoNetCustomActivityConnectedToService
        implements LoaderManager.LoaderCallbacks<Cursor>,
        IOnPublicationFromListSelected,
        TabLayout.OnTabSelectedListener,
        TextWatcher, View.OnClickListener {

    private static final String MY_TAG = "food_myPubsList";

    //region list vars

    RecyclerView rv_my_publications_list;
    MyPublicationsListRecyclerViewAdapter adapter;
    FloatingActionButton fab_add_pub;

    int currentFilterID;

    //endregion

    //region tabbar vars

    TabLayout tl_my_pubs_filter;

    //endregion


    //region Activity overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_publications);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab_add_pub = (FloatingActionButton) findViewById(R.id.fab_my_list_add_new_pub);
        fab_add_pub.setOnClickListener(this);

        SetupFilterTabButtons();

    }

    //endregion

    //region tab filter buttons

    private void SetupFilterTabButtons() {
        tl_my_pubs_filter = (TabLayout) findViewById(R.id.tl_my_list_filter_buttons);
        tl_my_pubs_filter.addTab(tl_my_pubs_filter.newTab().setText(getString(R.string.filter_all_my_pubs)), 0);
        tl_my_pubs_filter.addTab(tl_my_pubs_filter.newTab().setText(getString(R.string.filter_active_my_pubs)), 1);
        tl_my_pubs_filter.addTab(tl_my_pubs_filter.newTab().setText(getString(R.string.filter_ended_my_pubs)), 2);
        tl_my_pubs_filter.setOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case 0:
                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST;
                break;
            case 1:
                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_NEWEST;
                break;
            case 2:
                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_LESS_REGS;
                break;
        }
        StartLoadingForPublicationsList();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    //endregion

    //region Loader

    private void StartLoadingForPublicationsList() {
        getSupportLoaderManager().initLoader(currentFilterID, null, this);
    }

    private void RestartLoadingForPublicationsList() {
        getSupportLoaderManager().restartLoader(currentFilterID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        android.support.v4.content.CursorLoader cursorLoader = null;
        String[] projection;
        projection = FCPublication.GetColumnNamesForListArray();
        cursorLoader = new android.support.v4.content.CursorLoader(
                this, Uri.parse(FooDoNetSQLProvider.URI_GET_PUBS_FOR_LIST_BY_FILTER_ID + "/" + id),
                projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            ArrayList<FCPublication> publications = null;
            publications = FCPublication.GetArrayListOfPublicationsFromCursor(data, true);
            if (publications == null || publications.size() == 0) {
                Log.e(MY_TAG, "error getting publications from sql");
                return;
            }
            SetPublicationsListToAdapter(publications);
        }
    }

    private void SetPublicationsListToAdapter(ArrayList<FCPublication> publications) {
        if (adapter != null) {
            adapter.UpdatePublicationsList(publications);
        }
    }

    //endregion

    //region recycler view methods

    public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {
        private static final float HIDE_THRESHOLD = 10;
        private static final float SHOW_THRESHOLD = 70;
        private int mToolbarOffset = 0;
        private boolean mControlsVisible = true;
        public int mToolbarHeight;

        public HidingScrollListener(Context context) {
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                mToolbarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (mControlsVisible) {
                    if (mToolbarOffset > HIDE_THRESHOLD) {
                        setInvisible();
                    } else {
                        setVisible();
                    }
                } else {
                    if ((mToolbarHeight - mToolbarOffset) > SHOW_THRESHOLD) {
                        setVisible();
                    } else {
                        setInvisible();
                    }
                }
            }
        }

        private void setVisible() {
            if (mToolbarOffset > 0) {
                onShow();
                mToolbarOffset = 0;
            }
            mControlsVisible = true;
        }

        private void setInvisible() {
            if (mToolbarOffset < mToolbarHeight) {
                onHide();
                mToolbarOffset = mToolbarHeight;
            }
            mControlsVisible = false;
        }


        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            clipToolbarOffset();
            onMoved(mToolbarOffset);

            if ((mToolbarOffset < mToolbarHeight && dy > 0) || (mToolbarOffset > 0 && dy < 0)) {
                mToolbarOffset += dy;
            }
        }

        private void clipToolbarOffset() {
            if (mToolbarOffset > mToolbarHeight) {
                mToolbarOffset = mToolbarHeight;
            } else if (mToolbarOffset < 0) {
                mToolbarOffset = 0;
            }
        }

        public abstract void onMoved(int distance);

        public abstract void onShow();

        public abstract void onHide();
    }

    //endregion

    //region callbacks and overrides

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    @Override
    public void OnPublicationFromListClicked(int publicationID) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {

    }

    //endregion

}
