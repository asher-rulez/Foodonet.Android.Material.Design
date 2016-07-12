package upp.foodonet.material;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.RegisteredUserForPublication;

public class ExistingPublicationActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private static final String MY_TAG = "food_existPub";

    private int existing_publication_mode;
    private static final int MODE_MY_PUBLICATION = 1;
    private static final int MODE_OTHERS_PUBLICATION = 2;

    private boolean amIRegisteredToThisPublication;

    public static final String PUBLICATION_EXTRA_KEY = "publication";
    FCPublication currentPublication;

    Toolbar toolbar;

    ImageView iv_group_icon;
    TextView tv_group_name;
    TextView tv_time_left;
    TextView tv_users_joined;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_publication);

        Intent thisIntent = getIntent();
        currentPublication = (FCPublication) thisIntent.getSerializableExtra(PUBLICATION_EXTRA_KEY);
        if (currentPublication == null) {
            Log.e(MY_TAG, "no publication got from intent");
            Toast.makeText(this, "Error: no publication", Toast.LENGTH_SHORT).show();
            finish();
        }
        existing_publication_mode
                = currentPublication.getPublisherID() == CommonUtil.GetMyUserID(this)
                ? MODE_MY_PUBLICATION : MODE_OTHERS_PUBLICATION;

        amIRegisteredToThisPublication = false;
        if (existing_publication_mode == MODE_OTHERS_PUBLICATION && currentPublication.getRegisteredForThisPublication() != null) {
            for (RegisteredUserForPublication reg : currentPublication.getRegisteredForThisPublication()) {
                if (reg.getUserID() == CommonUtil.GetMyUserID(this)) {
                    amIRegisteredToThisPublication = true;
                    break;
                }
            }
        }

        InitToolBar();
        InitTopInfoBar();
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
    }

    private void InitToolBar() {
        toolbar = (Toolbar) findViewById(R.id.tb_existing_publication);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(null);
        if (toolbar != null)
            toolbar.setOnMenuItemClickListener(this);

        switch (existing_publication_mode) {
            case MODE_MY_PUBLICATION:
                toolbar.inflateMenu(currentPublication.IsActivePublication()
                        ? R.menu.existing_publication_my_active_menu
                        : R.menu.existing_publication_my_inactive_menu);
                break;
            case MODE_OTHERS_PUBLICATION:
                if (amIRegisteredToThisPublication)
                    toolbar.inflateMenu(R.menu.existing_publication_others_menu);
                break;
        }
        //toolbar.inflateMenu(R.menu.menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_edit)) == 0) {

        } else if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_delete)) == 0) {

        } else if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_stop_event)) == 0) {

        } else if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_restart_event)) == 0) {

        } else if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_report)) == 0) {

        }
        return true;
    }

    private void InitTopInfoBar(){
        iv_group_icon = (ImageView)findViewById(R.id.iv_pub_det_group_icon);
        tv_group_name = (TextView)findViewById(R.id.tv_pub_det_group_name);
        tv_time_left = (TextView)findViewById(R.id.tv_time_left_pub_det);
        tv_users_joined = (TextView)findViewById(R.id.tv_users_joined_pub_det);

        if(currentPublication.getAudience() == 0){
            iv_group_icon.setImageDrawable(getResources().getDrawable(R.drawable.public_group_pub_det_icon));
            tv_group_name.setText(getString(R.string.public_share_group_name));
        } else {
            iv_group_icon.setImageDrawable(getResources().getDrawable(R.drawable.group_pub_det_icon));
            tv_group_name.setText(currentPublication.get_group_name());
        }
    }
}
