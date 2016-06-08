package upp.foodonet.material;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Location;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Adapters.AllPublicationsListRecyclerViewAdapter;
import Adapters.IOnPublicationFromListSelected;
import Adapters.MapMarkerInfoWindowAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.ImageDictionarySyncronized;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetServerClasses.ImageDownloader;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import upp.foodonet.material.R;

public class EntarenceMapAndListActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMyLocationChangeListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        IOnPublicationFromListSelected,
        TabLayout.OnTabSelectedListener,
        TextWatcher {

    private static final String MY_TAG = "food_mapAndList";

    private static final int MODE_MAP = 0;
    private static final int MODE_LIST = 1;
    private int currentMode;

    //region Map variables
    SupportMapFragment mapFragment;
    GoogleMap googleMap;
    boolean isMapLoaded;
    HashMap<Marker, Integer> myMarkers;
    double maxDistance;
    int width, height;
    LatLng average, myLocation;
    float kilometer_for_map;
    int myLocationRefreshRate;
    ImageButton btn_focus_on_my_location;
    Date lastLocationUpdateDate;
    RelativeLayout ll_map_and_gallery;
    CoordinatorLayout.LayoutParams fabLayoutParams;
    FloatingActionButton fab;

    //endregion

    //region Gallery

    HorizontalScrollView hsv_gallery;
    LinearLayout gallery_pubs;
    ImageDownloader imageDownloader;
    ImageDictionarySyncronized imageDictionary;

    //endregion

    //region Publications list variables

    RecyclerView rv_all_publications_list;
    AllPublicationsListRecyclerViewAdapter adapter;
    FrameLayout fl_search_and_list;
    int currentFilterID;
    Toolbar tb_search;
    EditText et_search;
    private Toolbar toolbar;
    TabLayout tl_list_filter_buttons;

    //endregion

    //region Nav menu variables

    DrawerLayout drawerLayout;
    boolean isSideMenuOpened;

    //endregion

    //region Activity overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_list);

        currentMode = MODE_LIST;

        initToolBar();
        initNavVew();

        fab = (FloatingActionButton) findViewById(R.id.fab_map_and_list);
        if (fab != null) fab.setOnClickListener(this);
        fabLayoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();

        fl_search_and_list = (FrameLayout) findViewById(R.id.fl_all_publications_list);
        fl_search_and_list.setVisibility(View.GONE);
        tl_list_filter_buttons = (TabLayout) findViewById(R.id.tl_list_filter_buttons);
        SetupFilterTabButtons();
        tl_list_filter_buttons.setVisibility(View.GONE);
        tb_search = (Toolbar) findViewById(R.id.tb_search_pub_in_list);
        et_search = (EditText) findViewById(R.id.et_publication_list_search);
        et_search.addTextChangedListener(this);
        //tb_search.setVisibility(View.GONE);
/*
        View tab1view = LayoutInflater.from(tl_list_filter_buttons.getContext()).inflate(R.layout.tab_button_list_filter, tl_list_filter_buttons, false);
        TextView tv_tab1_title = (TextView)tab1view.findViewById(R.id.tv_tab_button_filter_title);
        tv_tab1_title.setText("tab1");
*/

//        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
//        lp.setBehavior(new FrameSwitchFABBehavior(this, null));
//        fab.setLayoutParams(lp);

        ll_map_and_gallery = (RelativeLayout) findViewById(R.id.ll_map_and_gallery);
        rv_all_publications_list = (RecyclerView) findViewById(R.id.rv_all_publications_list);

        btn_focus_on_my_location = (ImageButton) findViewById(R.id.btn_center_on_my_location_map);
        btn_focus_on_my_location.setOnClickListener(this);
        gallery_pubs = (LinearLayout) findViewById(R.id.ll_image_btns_gallery);
        hsv_gallery = (HorizontalScrollView) findViewById(R.id.hsv_image_gallery);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.map_one_kilometer_for_calculation, typedValue, true);
        kilometer_for_map = typedValue.getFloat();
        myLocationRefreshRate = getResources().getInteger(R.integer.map_refresh_my_location_frequency_milliseconds);
        Point size = CommonUtil.GetScreenSize(this);
        width = size.x;
        height = size.y;


        imageDictionary = new ImageDictionarySyncronized();
        imageDownloader = new ImageDownloader(this, imageDictionary);

        currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST;
        //SetupRecyclerViewPublications();
        //   initTabs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SetupMode();
/*
        switch (ll_map_and_gallery.getVisibility()) {
            case View.VISIBLE:
                if (googleMap != null)
                    StartLoadingForMarkers();
                break;
            case View.GONE:
                StartLoadingForPublicationsList();
                break;
        }
*/
    }

    @Override
    public void onBackPressed() {
        if (isSideMenuOpened) {
            drawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

    public void SetupMode(){
        switch (currentMode) {
            case MODE_LIST:
                SetFrameList();
                if (adapter == null)
                    SetupRecyclerViewPublications();
                StartLoadingForPublicationsList();
                break;
            case MODE_MAP:
                SetFrameMap();
                if (googleMap == null) {
                    mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    if (mapFragment != null) mapFragment.getMapAsync(this);
                } else
                    StartLoadingForMarkers();
                break;
        }
    }

    //endregion

    //region CLICK LISTENERS

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_map_and_list:
                Intent addPub = new Intent(this, AddEditPublicationActivity.class);
                startActivityForResult(addPub, 0);
                break;
            case R.id.btn_center_on_my_location_map:
                if (myLocation == null)
                    return;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(CommonUtil.GetBoundsByCenterLatLng(myLocation, maxDistance), width, height, 0);
                googleMap.animateCamera(cu);
                break;

        }
        //CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
        //new CoordinatorLayout.LayoutParams(fab.getWidth(), fab.getHeight());//
    }

    //endregion

    //region MAP AND MARKERS METHODS

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (this.googleMap != null) {
            isMapLoaded = true;
        } else {
            myMarkers = new HashMap<>();
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnMyLocationChangeListener(this);
        googleMap.setInfoWindowAdapter(new MapMarkerInfoWindowAdapter(getLayoutInflater()));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        StartLoadingForMarkers();

        if (btn_focus_on_my_location != null && googleMap != null)
            btn_focus_on_my_location.setVisibility(View.VISIBLE);
        hsv_gallery.setVisibility(View.VISIBLE);

        if (progressDialog != null)
            progressDialog.dismiss();

        SetCamera();
    }

    private Marker AddMarker(float latitude, float longtitude, String title, BitmapDescriptor icon) {
        MarkerOptions newMarker = new MarkerOptions().position(new LatLng(latitude, longtitude)).title(title).draggable(false);
        if (icon != null)
            newMarker.icon(icon);
        return googleMap.addMarker(newMarker);
    }

    private void AnimateCameraFocusOnLatLng(LatLng latLng) {
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        //CameraUpdateFactory.newLatLngBounds(CommonUtil.GetBoundsByCenterLatLng(latLng, maxDistance), width, height, 0);
        googleMap.animateCamera(cu);
    }

    private void SetCamera() {
        if (myLocation == null) {
            Log.i(MY_TAG, "SetCamera starts getting location");
            StartGetMyLocation();
        } else {
            OnReadyToUpdateCamera();
        }
    }

    private void OnReadyToUpdateCamera() {
        if (myMarkers != null && myMarkers.size() != 0) {
            double latitude = 0;
            double longtitude = 0;
            int counter = 0;
            if (myLocation != null) {
                if (average != null && CommonUtil.GetDistance(average, myLocation) < maxDistance)
                    return;

                latitude += myLocation.latitude;
                longtitude += myLocation.longitude;
                counter++;
                maxDistance = getResources().getInteger(R.integer.map_max_distance_if_location_available) * kilometer_for_map;
            } else {
                for (Marker m : myMarkers.keySet()) {
                    latitude += m.getPosition().latitude;
                    longtitude += m.getPosition().longitude;
                    counter++;
                }
                maxDistance = getResources().getInteger(R.integer.map_max_distance_if_location_not_available) * kilometer_for_map;
            }

            average = new LatLng(latitude / counter, longtitude / counter);
            Log.i(MY_TAG, "center coordinades: " + average.latitude + ":" + average.longitude);

/*
            if (myLocation != null && GetDistance(average, myLocation) < maxDistance)
                maxDistance = GetDistance(average, myLocation);
*/

/*
            for (Marker m : myMarkers.keySet()) {
                if (GetDistance(average, m.getPosition()) > maxDistance)
                    maxDistance = GetDistance(average, m.getPosition());
            }
*/

        } else {
            average = myLocation;
            Log.i(MY_TAG, "center coordinades (by my location): " + average.latitude + ":" + average.longitude);
        }
        AnimateCameraFocusOnLatLng(average);
    }

    @Override
    public void onMyLocationChange(Location location) {
        Log.i(MY_TAG, "got location update from map");
        if (location == null)
            return;
        if (lastLocationUpdateDate == null)
            lastLocationUpdateDate = new Date();
        else {
            long millisPassed = new Date().getTime() - lastLocationUpdateDate.getTime();
            if (millisPassed < myLocationRefreshRate) {
                Log.i(MY_TAG, millisPassed + " after last update, not updating");
                return;
            } else {
                Log.i(MY_TAG, "updating location! lat: " + location.getLatitude()
                        + "; long: " + location.getLongitude());
                lastLocationUpdateDate = new Date();
            }
        }
/*
        if(myLocation == null) {
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.blue_dot_circle);
            myLocation = AddMarker(((float)location.getLatitude()), (float)location.getLongitude(), getString(R.string.my_location), icon);
        }
*/
        if (myLocation != null && myLocation.latitude == location.getLatitude() && myLocation.longitude == location.getLongitude())
            return;
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
/*
        if (mainPagerAdapter != null){
            mainPagerAdapter.NotifyListOnLocationChange(location);
        }
*/
        SetCamera();
/*
        if (GetDistance(myLocation, new LatLng(location.getLatitude(), location.getLongitude())) <= maxDistance)
            return;
*/
        //UpdateMyLocationPreferences(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void OnGotMyLocationCallback(Location location) {
        Log.i(MY_TAG, "got location callback from task");
        if (location != null)
            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (isMapLoaded)
            OnReadyToUpdateCamera();
    }

    public void ResetMarkers() {
        if (myMarkers == null)
            myMarkers = new HashMap<>();
        else {
            for (Marker m : myMarkers.keySet())
                m.remove();
            myMarkers.clear();
        }
    }

    public void SetMarkers(ArrayList<FCPublication> publications) {
        for (FCPublication publication : publications) {
            Bitmap markerIcon;
            BitmapDescriptor icon = null;

            markerIcon = CommonUtil.decodeScaledBitmapFromDrawableResource(
                    getResources(), R.drawable.map_marker, 13, 13);
            icon = BitmapDescriptorFactory.fromBitmap(markerIcon);

            myMarkers.put(AddMarker(publication.getLatitude().floatValue(),
                    publication.getLongitude().floatValue(),
                    publication.getTitle(), icon), publication.getUniqueId());

            AddImageToGallery(publication);
        }

        if (isMapLoaded)
            SetCamera();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    private void showNotifyTab(int TAB) {
        //viewPager.setCurrentItem(TAB);
        //todo: implement
    }

    //endregion MAP AND REGIONS METHODS

    //region PUBS GALLERY

    public void AddImageToGallery(final FCPublication publication) {

        int screenLayout = this.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        int size = getResources().getDimensionPixelSize(R.dimen.gallery_image_btn_height);
        ImageButton imageButton = new ImageButton(getApplicationContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(15, 30, 15, 30);

        if (screenLayout == Configuration.SCREENLAYOUT_SIZE_SMALL) lp.setMargins(5, 10, 5, 10);

        imageButton.setLayoutParams(lp);
        imageButton.setBackgroundResource(R.drawable.map_gallery_border);

        SetPublicationImage(publication, imageButton);

/*
        Drawable drawable
                = CommonUtil.GetBitmapDrawableFromFile(
                publication.GetImageFileName(), getString(R.string.image_folder_path), size, size);
        if (drawable == null)
            drawable = getResources().getDrawable(R.drawable.foodonet_logo_200_200);
        imageButton.setImageDrawable(drawable);
*/
        imageButton.setScaleType(ImageView.ScaleType.FIT_XY);
        imageButton.setOnClickListener(new View.OnClickListener() {
            int id = publication.getUniqueId();

            @Override
            public void onClick(View v) {
                ImageBtnFromGallerySelected(id);
                CommonUtil.PostGoogleAnalyticsUIEvent(getApplicationContext(), "Map and list", "Gallery item", "item pressed");
            }
        });
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.map_my_location_button_pressed));
        states.addState(new int[]{}, getResources().getDrawable(R.drawable.map_my_location_button_normal));
        imageButton.setBackground(states);
        gallery_pubs.addView(imageButton);
    }

    public void ImageBtnFromGallerySelected(int id) {
        for (Map.Entry<Marker, Integer> e : myMarkers.entrySet()) {
            if (e.getValue().intValue() == id) {
                AnimateCameraFocusOnLatLng(e.getKey().getPosition());
                e.getKey().showInfoWindow();
            }
        }
        //Toast.makeText(this, "selected image id: " + String.valueOf(id), Toast.LENGTH_SHORT).show();
    }

    private void SetPublicationImage(FCPublication publication, ImageView publicationImage) {
        final int id = publication.getUniqueId();
        final int version = publication.getVersion();
        Drawable imageDrawable;
        imageDrawable = imageDictionary.Get(id);
        if (imageDrawable == null) {
            imageDownloader.Download(id, version, publicationImage);
        } else
            publicationImage.setImageDrawable(imageDrawable);
    }

    private void SetGalleryAndMarkers(ArrayList<FCPublication> publications) {
        //gallery_pubs.setVisibility(View.GONE);
        gallery_pubs.removeAllViews();
        ResetMarkers();
        SetMarkers(publications);
        //gallery_pubs.setVisibility(View.VISIBLE);
    }

    //endregion PUBS GALLERY

    //region loading data

    private void StartLoadingForMarkers() {
        getSupportLoaderManager().initLoader(-1, null, this);
    }

    private void RestartLoadingForMarkers() {
        getSupportLoaderManager().restartLoader(-1, null, this);
    }

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
        switch (id) {
            case -1:
                projection = FCPublication.GetColumnNamesArray();
                cursorLoader = new android.support.v4.content.CursorLoader(this, FooDoNetSQLProvider.URI_GET_PUBS_FOR_MAP_MARKERS,
                        projection, null, null, null);
                break;
            default:
                projection = FCPublication.GetColumnNamesForListArray();
                cursorLoader = new android.support.v4.content.CursorLoader(
                        this, Uri.parse(FooDoNetSQLProvider.URI_GET_PUBS_FOR_LIST_BY_FILTER_ID + "/" + id),
                        projection, null, null, null);
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<FCPublication> publications = null;
        if (data != null && data.moveToFirst()) {
            publications = loader.getId() == -1
                    ? FCPublication.GetArrayListOfPublicationsForMapFromCursor(data)
                    : FCPublication.GetArrayListOfPublicationsFromCursor(data, true);
            if (publications == null || publications.size() == 0)
                Log.e(MY_TAG, "no publications got from sql");
            switch (loader.getId()) {
                case -1:
                    SetGalleryAndMarkers(publications);
                    break;
                default:
                    SetPublicationsListToAdapter(publications);
                    break;
            }
        }
    }

    //endregion

    //region Navigation and toolbar

    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.tb_map_and_list);
        if (toolbar != null) //toolbar.setTitle(R.string.app_name);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getTitle().toString().compareToIgnoreCase("list") == 0) {
                        switch (ll_map_and_gallery.getVisibility()) {
                            case View.VISIBLE:
                                currentMode = MODE_LIST;
                                SetupMode();
                                break;
                            case View.GONE:
                                currentMode = MODE_MAP;
                                SetupMode();
                                break;
                        }
                    }
                    return true;
                }
            });

        toolbar.inflateMenu(R.menu.menu);
    }

    private void SetFrameMap() {
        ll_map_and_gallery.setVisibility(View.VISIBLE);
        hsv_gallery.setVisibility(View.VISIBLE);
        tl_list_filter_buttons.setVisibility(View.GONE);
        //rv_all_publications_list.setVisibility(View.GONE);
        fl_search_and_list.setVisibility(View.GONE);

        fab.setLayoutParams(fabLayoutParams);
    }

    private void SetFrameList() {
        ll_map_and_gallery.setVisibility(View.GONE);
        tl_list_filter_buttons.setVisibility(View.VISIBLE);
        //rv_all_publications_list.setVisibility(View.VISIBLE);
        fl_search_and_list.setVisibility(View.VISIBLE);
        StartLoadingForPublicationsList();

        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(fabLayoutParams);
        layoutParams.setAnchorId(View.NO_ID);
        layoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        layoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        fab.setLayoutParams(layoutParams);
    }

    private void initNavVew() {
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_main);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close){
            @Override
            public void onDrawerClosed(View drawerView) {
                isSideMenuOpened = false;
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                isSideMenuOpened = true;
                super.onDrawerOpened(drawerView);
                CommonUtil.PostGoogleAnalyticsUIEvent(getApplicationContext(), "Map and list", "Side menu", "Open menu");
            }
        };

        if (drawerLayout != null) drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();

        NavigationView v = (NavigationView) findViewById(R.id.nv_main);
        if (v != null)
            v.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    drawerLayout.closeDrawers();

                    switch (item.getItemId()) {
                        case R.id.nav_item_sharings:
                            Intent intent = new Intent(getApplicationContext(), MyPublicationsActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_item_subscriptions:

                            break;
                        case R.id.nav_item_groups:
                            Intent intentGroups = new Intent(getApplicationContext(), GroupsListActivity.class);
                            startActivity(intentGroups);
                            break;
                        case R.id.nav_item_settings:

                            break;

                        case R.id.nav_item_contact_us:

                            break;

                        case R.id.nav_item_terms:

                            break;

                    }
                    return true;
                }
            });
    }

    //endregion

    //region All publications list

    private void SetupRecyclerViewPublications() {
        rv_all_publications_list.setLayoutManager(new LinearLayoutManager(rv_all_publications_list.getContext()));
        adapter = new AllPublicationsListRecyclerViewAdapter(this, new ArrayList<FCPublication>(), this);
        rv_all_publications_list.setAdapter(adapter);
        rv_all_publications_list.addOnScrollListener(new HidingScrollListener(this) {
            @Override
            public void onMoved(int distance) {
                tb_search.setTranslationY(-distance);
            }

            @Override
            public void onShow() {
                tb_search.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void onHide() {
                tb_search.animate().translationY(-mToolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });
    }

    private void SetPublicationsListToAdapter(ArrayList<FCPublication> publications) {
        if (adapter != null) {
            adapter.UpdatePublicationsList(publications);
        }
    }

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

    //region callback methods

    @Override
    public void OnPublicationFromListClicked(int publicationID) {

    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    //endregion

    //region Tab filter buttons

    private void SetupFilterTabButtons() {
        tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab().setText(getString(R.string.filter_closest_btn_text)), 0);
        tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab().setText(getString(R.string.filter_new_btn_text)), 1);
        tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab().setText(getString(R.string.filter_all_btn_text)), 2);
        tl_list_filter_buttons.setOnTabSelectedListener(this);
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
        RestartLoadingForPublicationsList();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean isRestart = !TextUtils.isEmpty(et_search.getText().toString())
                && currentFilterID == FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_TEXT_FILTER;
        if (TextUtils.isEmpty(et_search.getText().toString())) {
            currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST;
        } else {
            FooDoNetCustomActivityConnectedToService.UpdateFilterTextPreferences(this, et_search.getText().toString());
            currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_TEXT_FILTER;
        }
        if (isRestart)
            RestartLoadingForPublicationsList();
        else
            StartLoadingForPublicationsList();
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().length() > 0) {
            et_search.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            //Assign your image again to the view, otherwise it will always be gone even if the text is 0 again.
            et_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.toolbar_find, 0, 0, 0);
        }
    }

    //endregion
/*
    class FrameSwitchFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton>{
        Context context;

        public FrameSwitchFABBehavior(Context context, AttributeSet attrs){
            super();
            this.context = context;
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent,
                                       FloatingActionButton child, View dependency) {
            // We're dependent on all SnackbarLayouts (if enabled)
            return dependency instanceof Snackbar.SnackbarLayout || dependency instanceof LinearLayout;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency){
            return true;
        }
    }
*/
}
