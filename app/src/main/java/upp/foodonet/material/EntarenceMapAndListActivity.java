package upp.foodonet.material;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import upp.foodonet.material.R;

public class EntarenceMapAndListActivity extends AppCompatActivity implements View.OnClickListener{

    private Toolbar toolbar;
    DrawerLayout drawerLayout;
    TabLayout tl;
    ViewPager viewPager;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_list);

        initToolBar();
        initNavVew();

        fab = (FloatingActionButton)findViewById(R.id.fab_btn);
        fab.setOnClickListener(this);
        //   initTabs();
    }

    private void initToolBar() {
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.app_name);
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

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();

        NavigationView v = (NavigationView) findViewById(R.id.nv_main);
        v.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
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
//                Intent addPub = new Intent(this, AddEditPublicationActivity.class);
//                startActivity(addPub);
                break;
        }
    }
}
