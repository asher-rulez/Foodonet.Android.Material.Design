package upp.foodonet.material;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.util.ArrayList;

import Adapters.AllPublicationsListRecyclerViewAdapter;
import Adapters.IOnPublicationFromListSelected;
import Adapters.MyPublicationsListRecyclerViewAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;

public class MyPublicationsActivity
        extends FooDoNetCustomActivityConnectedToService
        implements LoaderManager.LoaderCallbacks<Cursor>,
        IOnPublicationFromListSelected,
        TabLayout.OnTabSelectedListener,
        TextWatcher, View.OnClickListener, IFooDoNetSQLCallback {

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_my_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        fab_add_pub = (FloatingActionButton) findViewById(R.id.fab_my_list_add_new_pub);
        fab_add_pub.setOnClickListener(this);

        SetupFilterTabButtons();

        rv_my_publications_list = (RecyclerView)findViewById(R.id.rv_my_publications_list);
        SetupRecyclerViewAndAdapter();

        currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_ENDING_SOON;
        StartLoadingForPublicationsList();

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
                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_ENDING_SOON;
                break;
            case 1:
                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_ACTIVE_ID_DESC;
                break;
            case 2:
                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_NOT_ACTIVE_ID_ASC;
                break;
        }
        RestartLoadingForPublicationsList();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    //endregion

    //region recyclerview list of my pubs

    private void SetupRecyclerViewAndAdapter(){
        rv_my_publications_list.setLayoutManager(new LinearLayoutManager(rv_my_publications_list.getContext()));
        adapter = new MyPublicationsListRecyclerViewAdapter(this, new ArrayList<FCPublication>(), this, getString(R.string.public_share_group_name));
        rv_my_publications_list.setAdapter(adapter);
//        rv_my_publications_list.addOnScrollListener(new HidingScrollListener(this) {
//            @Override
//            public void onMoved(int distance) {
//                tb_search.setTranslationY(-distance);
//            }
//
//            @Override
//            public void onShow() {
//                tb_search.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
//            }
//
//            @Override
//            public void onHide() {
//                tb_search.animate().translationY(-mToolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
//            }
//        });
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
        ArrayList<FCPublication> publications = null;
        if (data != null && data.moveToFirst()) {
            publications = FCPublication.GetArrayListOfPublicationsFromCursor(data, true);
            if (publications == null || publications.size() == 0) {
                Log.e(MY_TAG, "error getting publications from sql");
                return;
            }
        }
        SetPublicationsListToAdapter(publications);
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
        if(publicationID<0){
            Toast.makeText(this, getString(R.string.new_pub_waiting_for_save_on_server), Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
        FooDoNetSQLExecuterAsync sqlGetPubAsync = new FooDoNetSQLExecuterAsync(this, getContentResolver());
        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID);
        ir.PublicationID = publicationID;
        sqlGetPubAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
/*
        switch (request.ActionCommand) {
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                FCPublication result = request.publicationForDetails;
                if (result == null)
                    Log.e(MY_TAG, "OnSQLTaskComplete got null request.publicationForDetails");
                String myIMEI = CommonUtil.GetIMEI(this);
                if (result.getPublisherUID() != null)
                    result.isOwnPublication = result.getPublisherUID().compareTo(myIMEI) == 0;
                Intent intent = new Intent(getApplicationContext(), PublicationDetailsActivity.class);
                intent.putExtra(PublicationDetailsActivity.PUBLICATION_PARAM, result);
                startActivityForResult(intent, 1);
                if(progressDialog != null){
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                break;
        }
*/
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
        switch (v.getId()) {
            case R.id.fab_map_and_list:
                Intent addPub = new Intent(this, AddEditPublicationActivity.class);
                startActivityForResult(addPub, 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    //endregion

}
