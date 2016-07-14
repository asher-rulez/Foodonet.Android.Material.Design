package upp.foodonet.material;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import FooDoNetServerClasses.ImageDownloader;
import UIUtil.RoundedImageView;

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

    ImageView iv_publication_image;
    TextView tv_title;
    TextView tv_subtitle;

    RoundedImageView riv_user_avatar;
    TextView tv_address;
    ImageView iv_rating_star;
    TextView tv_user_rating;
    TextView tv_user_name;

    TextView tv_price;

    TextView tv_reports_title;
    LinearLayout ll_reports;

    ImageDownloader imageDownloader;

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
        SetPublicationImage();

        tv_title = (TextView)findViewById(R.id.tv_pub_det_title);
        tv_title.setText(currentPublication.getTitle());
        tv_subtitle = (TextView)findViewById(R.id.tv_pub_det_subtitle);
        if(TextUtils.isEmpty(currentPublication.getSubtitle()))
            tv_subtitle.setVisibility(View.GONE);
        else
            tv_subtitle.setText(currentPublication.getSubtitle());

        InitUserData();

        tv_price = (TextView)findViewById(R.id.tv_pub_det_price);
        if(currentPublication.getPrice() == null || currentPublication.getPrice() == 0)
            tv_price.setText(getString(R.string.publication_details_price_free));
        else tv_price.setText(getString(R.string.publication_details_price_format)
                .replace("{0}", String.valueOf(currentPublication.getPrice())));

        SetReports();

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

        if(currentPublication.IsActivePublication())
            tv_time_left.setText(GetTimeLertTillPublicationEnds());
        else tv_time_left.setText(getString(R.string.time_left_ended));

        tv_users_joined.setText(getString(R.string.users_joined_format_for_list)
                .replace("{0}", String.valueOf(
                        currentPublication.getRegisteredForThisPublication() == null
                                ? 0 : currentPublication.getRegisteredForThisPublication().size())));
    }

    private void SetPublicationImage() {
        iv_publication_image = (ImageView)findViewById(R.id.iv_pub_det_image);
        final int id = currentPublication.getUniqueId();
        final int version = currentPublication.getVersion();
        imageDownloader = new ImageDownloader(this, null);
        imageDownloader.Download(id, version, iv_publication_image);
    }

    private String GetTimeLertTillPublicationEnds(){
        return CommonUtil.GetTimeLeftString(this, new Date(), currentPublication.getEndingDate());
    }

    private void InitUserData(){
        riv_user_avatar = (RoundedImageView)findViewById(R.id.riv_pub_det_user_avatar);
        imageDownloader.DownloadUserAvatar(
                getString(R.string.amazon_user_avatar_image_name).replace("{0}",
                String.valueOf(currentPublication.getPublisherID())), riv_user_avatar);

        tv_address = (TextView)findViewById(R.id.tv_pub_det_address);
        tv_address.setText(currentPublication.getAddress());

        iv_rating_star = (ImageView)findViewById(R.id.iv_pub_det_rating_star);
        iv_rating_star.setImageDrawable(getRatingStarByUserRating(currentPublication.getRating()));

        tv_user_rating = (TextView)findViewById(R.id.tv_pub_det_user_rating);
        tv_user_rating.setText(getString(R.string.user_rating_format)
                .replace("{0}", String.valueOf((int)(currentPublication.getRating()/1)))
                .replace("{1}", String.valueOf((int)(currentPublication.getRating()%1))));

        tv_user_name = (TextView)findViewById(R.id.tv_pub_det_user_name);
        tv_user_name.setText(currentPublication.getPublisherUserName());
    }

    private Drawable getRatingStarByUserRating(double rating){
        if(rating<=0)
            return getResources().getDrawable(R.drawable.rating_star_no_rating);
        if(rating<=2)
            return getResources().getDrawable(R.drawable.rating_star_bad);
        if(rating<=4)
            return getResources().getDrawable(R.drawable.rating_star_half);
        return getResources().getDrawable(R.drawable.rating_star_good);
    }

    private void SetReports(){
        tv_reports_title = (TextView)findViewById(R.id.tv_pub_det_reports_title);
        if(currentPublication.getPublicationReports() == null
           || currentPublication.getPublicationReports().size() == 0){
            tv_reports_title.setText(getString(R.string.publication_details_no_reports));
            return;
        }else {
            tv_reports_title.setText(getString(R.string.publication_details_reports));
        }
        ll_reports = (LinearLayout)findViewById(R.id.ll_pub_det_reports);
        for(PublicationReport report : currentPublication.getPublicationReports()){
            View reportView = getLayoutInflater().inflate(R.layout.publication_details_report_item, null);
            TextView tv_report_title = (TextView)reportView.findViewById(R.id.tv_report_details);
            tv_report_title.setText(getString(R.string.report_format)
                    .replace("{0}", GetReportStringByCode(report.getReport()))
                    .replace("{1}", CommonUtil.GetTimeLeftString(this, report.getDate_reported(), new Date())));
            ll_reports.addView(reportView);
        }
    }

    private String GetReportStringByCode(int reportCode){
        switch (reportCode){
            case 1:
                return getString(R.string.report_has_more);
            case 3:
                return getString(R.string.report_took_all);
            case 5:
                return getString(R.string.report_nothing_found);
            default:
                return getString(R.string.report_error);
        }
    }
}
