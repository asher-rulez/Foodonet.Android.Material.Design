package upp.foodonet.material;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import upp.foodonet.material.R;

public class EntarenceMapAndListActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        OnMapReadyCallback {

    private Toolbar toolbar;
    DrawerLayout drawerLayout;
    TabLayout tl;
    ViewPager viewPager;
    FloatingActionButton fab;
    SupportMapFragment mapFragment;
    GoogleMap mMap;
    LinearLayout ll_map_and_gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_list);

        initToolBar();
        initNavVew();

        fab = (FloatingActionButton)findViewById(R.id.fab_btn);
        if(fab != null) fab.setOnClickListener(this);

        ll_map_and_gallery = (LinearLayout)findViewById(R.id.ll_map_and_gallery);

        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        if(mapFragment != null) mapFragment.getMapAsync(this);
        //   initTabs();
    }

    private void initToolBar() {
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        if(toolbar != null) toolbar.setTitle(R.string.app_name);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });

        toolbar.inflateMenu(R.menu.menu);
    }

    private void initNavVew() {
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_main);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);

        if(drawerLayout != null) drawerLayout.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();

        NavigationView v = (NavigationView) findViewById(R.id.nv_main);
        if(v != null) v.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawers();

                switch (item.getItemId()) {
                    case R.id.nav_item_sharings:

                        break;
                    case R.id.nav_item_subscriptions:

                        break;
                    case R.id.nav_item_groups:

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
    private void showNotifyTab(int TAB){
        viewPager.setCurrentItem(TAB);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_btn:
                switch (ll_map_and_gallery.getVisibility()){
                    case View.VISIBLE:
                        ll_map_and_gallery.setVisibility(View.GONE);
                        break;
                    case View.GONE:
                        ll_map_and_gallery.setVisibility(View.VISIBLE);
                        break;
                }
//                Intent addPub = new Intent(this, AddEditPublicationActivity.class);
//                startActivity(addPub);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    class FrameSwitchFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton>{
        public FrameSwitchFABBehavior(Context context, AttributeSet attrs){
            super();
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
}
