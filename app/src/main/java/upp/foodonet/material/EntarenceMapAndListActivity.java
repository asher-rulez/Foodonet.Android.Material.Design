package upp.foodonet.material;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Location;
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
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import Adapters.MapMarkerInfoWindowAdapter;
import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import upp.foodonet.material.R;

public class EntarenceMapAndListActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMyLocationChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String MY_TAG = "food_mapAndList";


    private Toolbar toolbar;
    DrawerLayout drawerLayout;
    TabLayout tl;
    ViewPager viewPager;
    FloatingActionButton fab;

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

    HorizontalScrollView hsv_gallery;
    LinearLayout gallery_pubs;

    //endregion


    LinearLayout ll_map_and_gallery;
    CoordinatorLayout.LayoutParams fabLayoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_list);

        initToolBar();
        initNavVew();

        fab = (FloatingActionButton) findViewById(R.id.fab_map_and_list);
        if (fab != null) fab.setOnClickListener(this);
        fabLayoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
//        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
//        lp.setBehavior(new FrameSwitchFABBehavior(this, null));
//        fab.setLayoutParams(lp);

        ll_map_and_gallery = (LinearLayout) findViewById(R.id.ll_map_and_gallery);

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

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        //   initTabs();
    }

    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.tb_map_and_list);
        if (toolbar != null) //toolbar.setTitle(R.string.app_name);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getTitle().toString().compareToIgnoreCase("list") == 0){
                        switch (ll_map_and_gallery.getVisibility()) {
                            case View.VISIBLE:
                                ll_map_and_gallery.setVisibility(View.GONE);
                                break;
                            case View.GONE:
                                ll_map_and_gallery.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                    switch (ll_map_and_gallery.getVisibility()) {
                        case View.GONE:
//                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, fab.getWidth(), getResources().getDisplayMetrics());
//                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, fab.getHeight(), getResources().getDisplayMetrics());
                            CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(fabLayoutParams);
                            layoutParams.setAnchorId(View.NO_ID);
                            layoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                            layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                            layoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                            fab.setLayoutParams(layoutParams);
                            break;
                        case View.VISIBLE:
                            fab.setLayoutParams(fabLayoutParams);
//                layoutParams.setAnchorId(R.id.map);
//                layoutParams.anchorGravity = Gravity.BOTTOM | Gravity.RIGHT | Gravity.END;
//                layoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
//                layoutParams.bottomMargin = 0;
//                fab.setLayoutParams(layoutParams);
                            break;
                    }
                    return true;
                }
            });

        toolbar.inflateMenu(R.menu.menu);
    }

    private void initNavVew() {
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_main);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);

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
                            Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
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

    private void showNotifyTab(int TAB) {
        viewPager.setCurrentItem(TAB);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_map_and_list:

                Intent addPub = new Intent(this, AddEditPublicationActivity.class);
                startActivity(addPub);
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

    //region MAP METHODS

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
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        StartLoadingForMarkers();

        if (btn_focus_on_my_location != null && googleMap != null)
            btn_focus_on_my_location.setVisibility(View.VISIBLE);
        if (hsv_gallery != null)
            hsv_gallery.setVisibility(View.VISIBLE);

        if (progressDialog != null)
            progressDialog.dismiss();

        SetCamera();

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void StartLoadingForMarkers() {
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void RestartLoadingForMarkers() {
        getSupportLoaderManager().restartLoader(0, null, this);
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

    //endregion MAP METHODS

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

        Drawable drawable
                = CommonUtil.GetBitmapDrawableFromFile(
                publication.GetImageFileName(), getString(R.string.image_folder_path), size, size);
        if (drawable == null)
            drawable = getResources().getDrawable(R.drawable.foodonet_logo_200_200);
        imageButton.setImageDrawable(drawable);
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


    //endregion PUBS GALLERY

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        android.support.v4.content.CursorLoader cursorLoader = null;
        String[] projection;
        switch (id) {
            case 0:
                projection = FCPublication.GetColumnNamesArray();
                cursorLoader = new android.support.v4.content.CursorLoader(this, FooDoNetSQLProvider.URI_GET_PUBS_FOR_MAP_MARKERS,
                        projection, null, null, null);
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case 0:
                if (data != null && data.moveToFirst()) {
                    //Log.i(MY_TAG, "num of rows in adapter: " + data.getCount());
                    ArrayList<FCPublication> publications = FCPublication.GetArrayListOfPublicationsForMapFromCursor(data);
                    if (publications == null) {
                        Log.e(MY_TAG, "error getting publications from sql");
                        return;
                    }

                    if (myMarkers == null)
                        myMarkers = new HashMap<>();
                    else {
                        for (Marker m : myMarkers.keySet())
                            m.remove();
                        myMarkers.clear();
                    }
                    gallery_pubs.setVisibility(View.GONE);
                    gallery_pubs.removeAllViews();

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

                    gallery_pubs.setVisibility(View.VISIBLE);

                    if (isMapLoaded)
                        SetCamera();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

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
