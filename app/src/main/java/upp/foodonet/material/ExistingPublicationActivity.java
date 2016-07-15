package upp.foodonet.material;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;
import java.util.Date;
import java.util.List;

import Adapters.IRegisteredUserSelectedCallback;
import Adapters.RegisteredUsersForCallOrSmsRecyclerViewAdapter;
import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import FooDoNetServerClasses.ConnectionDetector;
import FooDoNetServerClasses.ImageDownloader;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import UIUtil.RoundedImageView;

public class ExistingPublicationActivity
        extends FooDoNetCustomActivityConnectedToService
        implements Toolbar.OnMenuItemClickListener,
                    View.OnClickListener,
                    IRegisteredUserSelectedCallback {

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

    //region floating buttons

    LinearLayout ll_fab_panel_my;
    FloatingActionButton fab_facebook;
    FloatingActionButton fab_twitter;
    FloatingActionButton fab_sms_reg;
    FloatingActionButton fab_call_reg;
    LinearLayout ll_fab_panel_other;
    FloatingActionButton fab_reg_unreg;
    FloatingActionButton fab_sms_owner;
    FloatingActionButton fab_call_owner;
    FloatingActionButton fab_navigate;

    //endregion

    //region select registered user

    final int SELECT_USER_DIALOG_STARTED_FOR_SMS = 1;
    final int SELECT_USER_DIALOG_STARTED_FOR_CALL = 2;
    int select_user_dialog_started_for;
    Dialog select_reg_user_dialog;
    boolean is_select_user_dialog_started;
    RecyclerView rv_select_user;

    //endregion

    protected boolean isInternetAvailable = false;
    protected boolean isGoogleServiceAvailable = false;

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

        tv_title = (TextView) findViewById(R.id.tv_pub_det_title);
        tv_title.setText(currentPublication.getTitle());
        tv_subtitle = (TextView) findViewById(R.id.tv_pub_det_subtitle);
        if (TextUtils.isEmpty(currentPublication.getSubtitle()))
            tv_subtitle.setVisibility(View.GONE);
        else
            tv_subtitle.setText(currentPublication.getSubtitle());

        InitUserData();

        tv_price = (TextView) findViewById(R.id.tv_pub_det_price);
        if (currentPublication.getPrice() == null || currentPublication.getPrice() == 0)
            tv_price.setText(getString(R.string.publication_details_price_free));
        else tv_price.setText(getString(R.string.publication_details_price_format)
                .replace("{0}", String.valueOf(currentPublication.getPrice())));

        SetReports();
        InitFABPanel();

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

    @Override
    protected void onResume() {
        isInternetAvailable = CheckInternetConnection();
        if (!isInternetAvailable)
            OnInternetNotConnected();
        isGoogleServiceAvailable = CheckPlayServices();
        if (!isGoogleServiceAvailable)
            OnGooglePlayServicesCheckError();
        super.onResume();
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

    private void InitTopInfoBar() {
        iv_group_icon = (ImageView) findViewById(R.id.iv_pub_det_group_icon);
        tv_group_name = (TextView) findViewById(R.id.tv_pub_det_group_name);
        tv_time_left = (TextView) findViewById(R.id.tv_time_left_pub_det);
        tv_users_joined = (TextView) findViewById(R.id.tv_users_joined_pub_det);

        if (currentPublication.getAudience() == 0) {
            iv_group_icon.setImageDrawable(getResources().getDrawable(R.drawable.public_group_pub_det_icon));
            tv_group_name.setText(getString(R.string.public_share_group_name));
        } else {
            iv_group_icon.setImageDrawable(getResources().getDrawable(R.drawable.group_pub_det_icon));
            tv_group_name.setText(currentPublication.get_group_name());
        }

        if (currentPublication.IsActivePublication())
            tv_time_left.setText(GetTimeLertTillPublicationEnds());
        else tv_time_left.setText(getString(R.string.time_left_ended));

        tv_users_joined.setText(getString(R.string.users_joined_format_for_list)
                .replace("{0}", String.valueOf(
                        currentPublication.getRegisteredForThisPublication() == null
                                ? 0 : currentPublication.getRegisteredForThisPublication().size())));
    }

    private void SetPublicationImage() {
        iv_publication_image = (ImageView) findViewById(R.id.iv_pub_det_image);
        final int id = currentPublication.getUniqueId();
        final int version = currentPublication.getVersion();
        imageDownloader = new ImageDownloader(this, null);
        imageDownloader.Download(id, version, iv_publication_image);
    }

    private String GetTimeLertTillPublicationEnds() {
        return CommonUtil.GetTimeLeftString(this, new Date(), currentPublication.getEndingDate());
    }

    private void InitUserData() {
        riv_user_avatar = (RoundedImageView) findViewById(R.id.riv_pub_det_user_avatar);
        imageDownloader.DownloadUserAvatar(
                getString(R.string.amazon_user_avatar_image_name).replace("{0}",
                        String.valueOf(currentPublication.getPublisherID())), riv_user_avatar);

        tv_address = (TextView) findViewById(R.id.tv_pub_det_address);
        tv_address.setText(currentPublication.getAddress());

        iv_rating_star = (ImageView) findViewById(R.id.iv_pub_det_rating_star);
        iv_rating_star.setImageDrawable(getRatingStarByUserRating(currentPublication.getRating()));

        tv_user_rating = (TextView) findViewById(R.id.tv_pub_det_user_rating);
        tv_user_rating.setText(getString(R.string.user_rating_format)
                .replace("{0}", String.valueOf((int) (currentPublication.getRating() / 1)))
                .replace("{1}", String.valueOf((int) (currentPublication.getRating() % 1))));

        tv_user_name = (TextView) findViewById(R.id.tv_pub_det_user_name);
        tv_user_name.setText(currentPublication.getPublisherUserName());
    }

    private Drawable getRatingStarByUserRating(double rating) {
        if (rating <= 0)
            return getResources().getDrawable(R.drawable.rating_star_no_rating);
        if (rating <= 2)
            return getResources().getDrawable(R.drawable.rating_star_bad);
        if (rating <= 4)
            return getResources().getDrawable(R.drawable.rating_star_half);
        return getResources().getDrawable(R.drawable.rating_star_good);
    }

    private void SetReports() {
        tv_reports_title = (TextView) findViewById(R.id.tv_pub_det_reports_title);
        if (currentPublication.getPublicationReports() == null
                || currentPublication.getPublicationReports().size() == 0) {
            tv_reports_title.setText(getString(R.string.publication_details_no_reports));
            return;
        } else {
            tv_reports_title.setText(getString(R.string.publication_details_reports));
        }
        ll_reports = (LinearLayout) findViewById(R.id.ll_pub_det_reports);
        for (PublicationReport report : currentPublication.getPublicationReports()) {
            View reportView = getLayoutInflater().inflate(R.layout.publication_details_report_item, null);
            TextView tv_report_title = (TextView) reportView.findViewById(R.id.tv_report_details);
            tv_report_title.setText(getString(R.string.report_format)
                    .replace("{0}", GetReportStringByCode(report.getReport()))
                    .replace("{1}", CommonUtil.GetTimeLeftString(this, report.getDate_reported(), new Date())));
            ll_reports.addView(reportView);
        }
    }

    private String GetReportStringByCode(int reportCode) {
        switch (reportCode) {
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

    private void InitFABPanel() {
        ll_fab_panel_my = (LinearLayout) findViewById(R.id.ll_pub_det_fab_panel_my);
        ll_fab_panel_other = (LinearLayout) findViewById(R.id.ll_pub_det_fab_panel_others);
        switch (existing_publication_mode) {
            case MODE_MY_PUBLICATION:
                ll_fab_panel_other.setVisibility(View.GONE);
                ll_fab_panel_my.setVisibility(View.VISIBLE);
                fab_facebook = (FloatingActionButton) findViewById(R.id.fab_pub_det_facebook);
                fab_facebook.setOnClickListener(this);
                fab_twitter = (FloatingActionButton) findViewById(R.id.fab_pub_det_twitter);
                fab_twitter.setOnClickListener(this);
                fab_call_reg = (FloatingActionButton) findViewById(R.id.fab_pub_det_call_reg);
                fab_call_reg.setOnClickListener(this);
                fab_sms_reg = (FloatingActionButton) findViewById(R.id.fab_pub_det_sms_reg);
                fab_sms_reg.setOnClickListener(this);
                if(currentPublication.getRegisteredForThisPublication() == null
                        || currentPublication.getRegisteredForThisPublication().size() == 0){
                    fab_call_reg.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_inactive_gray)));
                    fab_sms_reg.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_inactive_gray)));
                }
                break;
            case MODE_OTHERS_PUBLICATION:
                ll_fab_panel_other.setVisibility(View.VISIBLE);
                ll_fab_panel_my.setVisibility(View.GONE);
                fab_reg_unreg = (FloatingActionButton) findViewById(R.id.fab_pub_det_register_unregister);
                fab_reg_unreg.setOnClickListener(this);
                fab_sms_owner = (FloatingActionButton) findViewById(R.id.fab_pub_det_sms);
                fab_sms_owner.setOnClickListener(this);
                fab_call_owner = (FloatingActionButton) findViewById(R.id.fab_pub_det_call);
                fab_call_owner.setOnClickListener(this);
                fab_navigate = (FloatingActionButton) findViewById(R.id.fab_pub_det_navigate);
                if (!amIRegisteredToThisPublication) {
                    fab_reg_unreg.setImageDrawable(getResources().getDrawable(R.drawable.fab_register));
                    fab_sms_owner.setVisibility(View.GONE);
                    fab_call_owner.setVisibility(View.GONE);
                    fab_navigate.setVisibility(View.GONE);
                } else {
                    fab_reg_unreg.setImageDrawable(getResources().getDrawable(R.drawable.fab_unregister));
                    fab_navigate.setVisibility(View.VISIBLE);
                    fab_sms_owner.setVisibility(View.VISIBLE);
                    fab_call_owner.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_pub_det_facebook:
                if (!CheckInternetForAction(getString(R.string.post_on_facebook_action)))
                    return;
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
                PostOnFacebook();
                break;
            case R.id.fab_pub_det_twitter:
                if (!CheckInternetForAction(getString(R.string.post_on_tweeter_action)))
                    return;
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
                SendTweet();
                break;
            case R.id.fab_pub_det_call_reg:
            case R.id.fab_pub_det_sms_reg:
                if(currentPublication.getRegisteredForThisPublication() == null
                        || currentPublication.getRegisteredForThisPublication().size() == 0) {
                    Toast.makeText(this, getString(R.string.no_registered_user_for_sms_or_call), Toast.LENGTH_SHORT).show();
                    return;
                }
                select_user_dialog_started_for = view.getId() == R.id.fab_pub_det_call_reg
                        ? SELECT_USER_DIALOG_STARTED_FOR_CALL
                        : SELECT_USER_DIALOG_STARTED_FOR_SMS;
                ShowSelectUserDialog();
                break;
            case R.id.fab_pub_det_register_unregister:
                if(!amIRegisteredToThisPublication) {
                    PopButtonsAfterRegistration();
                    amIRegisteredToThisPublication = true;
                } else {
                    CollapseButtonsAfterUnregister();
                    amIRegisteredToThisPublication = false;
                }
                break;
            case R.id.fab_pub_det_sms:
                StartSMS(currentPublication.getContactInfo());
                break;
            case R.id.fab_pub_det_call:
                StartCall(currentPublication.getContactInfo());
                break;
            case R.id.fab_pub_det_navigate:
                try {
                    String url = "waze://?ll=" + currentPublication.getLatitude() + "," + currentPublication.getLongitude();
                    Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(navIntent);
                } catch (ActivityNotFoundException ex) {
                    Intent navIntent =
                            new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                    startActivity(navIntent);
                }
                break;
        }
    }

    private void PopButtonsAfterRegistration(){
        Animation expandIn = AnimationUtils.loadAnimation(this, R.anim.anim_fab_pop_appear);
        fab_sms_owner.setVisibility(View.VISIBLE);
        fab_sms_owner.startAnimation(expandIn);
        fab_call_owner.setVisibility(View.VISIBLE);
        fab_call_owner.startAnimation(expandIn);
        fab_navigate.setVisibility(View.VISIBLE);
        fab_navigate.startAnimation(expandIn);
    }

    private void CollapseButtonsAfterUnregister(){
        Animation collapseOut = AnimationUtils.loadAnimation(this, R.anim.anim_fab_collapse_disappear);
        fab_sms_owner.startAnimation(collapseOut);
        fab_sms_owner.setVisibility(View.GONE);
        fab_call_owner.startAnimation(collapseOut);
        fab_call_owner.setVisibility(View.GONE);
        fab_navigate.startAnimation(collapseOut);
        fab_navigate.setVisibility(View.GONE);
    }

    protected boolean CheckInternetForAction(String action) {
        if (!isInternetAvailable) {
            isInternetAvailable = CheckInternetConnection();
            if (!isInternetAvailable) {
                Toast.makeText(this,
                        getString(R.string.error_cant_perform_this_action_without_internet).replace("{0}",
                                action), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    protected boolean CheckPlayServices() {
        Log.i(MY_TAG, "checking isGooglePlayServicesAvailable...");
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.e(MY_TAG, "UserRecoverableError: " + resultCode);
            }
            Log.e(MY_TAG, "Google Play Services Error: " + resultCode);
            return false;
        }
        Log.w(MY_TAG, "Google Play Services available!");
        return true;
    }

    protected boolean CheckInternetConnection() {
        Log.i(MY_TAG, "Checking internet connection...");
        ConnectionDetector cd = new ConnectionDetector(getBaseContext());
        return cd.isConnectingToInternet();
    }

    //region Facebook method
    private void PostOnFacebook() {
        Intent facebookIntent = new Intent(Intent.ACTION_SEND);
        String msg = currentPublication.getTitle() + "\n " + getString(R.string.facebook_page_url) + "\n ";
        facebookIntent.putExtra(Intent.EXTRA_TEXT, msg);
        String fileName = currentPublication.getUniqueId() + "." + currentPublication.getVersion() + ".jpg";
        String imageSubFolder = getString(R.string.image_folder_path);
        File photo = new File(fileName);
        if (!photo.exists())
            photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);
        facebookIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photo));
        facebookIntent.setType("image/*");
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(facebookIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.facebook.katana")) {
                facebookIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            startActivity(facebookIntent);
        } else {
            Toast.makeText(this, "Facebook app isn't found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.facebook.katana"));
            startActivity(intent);
        }
        if (progressDialog != null)
            progressDialog.dismiss();
    }
    // endregion

    // region  Twitter method
    private void SendTweet() {
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        String msg = getString(R.string.hashtag) + " : " + currentPublication.getTitle() + "\n " +
                getString(R.string.facebook_page_url);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, msg);
        String fileName = currentPublication.getUniqueId() + "." + currentPublication.getVersion() + ".jpg";
        String imageSubFolder = getString(R.string.image_folder_path);
        File photo = new File(fileName);
        if (!photo.exists())
            photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);
        tweetIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photo));
        tweetIntent.setType("text/plain");
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            startActivity(tweetIntent);
        } else {
            Toast.makeText(this, "Twitter app isn't found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.twitter.android"));
            startActivity(intent);
        }
        if (progressDialog != null)
            progressDialog.dismiss();
    }
    // endregion


    //region Select registered user dialog
    private void ShowSelectUserDialog() {
        is_select_user_dialog_started = true;
        select_reg_user_dialog = new Dialog(this);
        select_reg_user_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        select_reg_user_dialog.setContentView(R.layout.select_reg_user_dialog);
        select_reg_user_dialog.setCanceledOnTouchOutside(true);
        select_reg_user_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) { }
        });
        select_reg_user_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                RegisteredUserSelected(null);
            }
        });

        rv_select_user = (RecyclerView)select_reg_user_dialog.findViewById(R.id.rv_reged_users);
        rv_select_user.setLayoutManager(new LinearLayoutManager(rv_select_user.getContext()));
        RegisteredUsersForCallOrSmsRecyclerViewAdapter adapter
                = new RegisteredUsersForCallOrSmsRecyclerViewAdapter(currentPublication.getRegisteredForThisPublication(), this);
        rv_select_user.setAdapter(adapter);

        Button btn_cancel_select = (Button)select_reg_user_dialog.findViewById(R.id.btn_cancel_select_reg_user);
        btn_cancel_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisteredUserSelected(null);
            }
        });

        select_reg_user_dialog.show();
    }

    @Override
    public void RegisteredUserSelected(String phoneNumber) {
        if(select_reg_user_dialog != null)
            select_reg_user_dialog.dismiss();
        if(phoneNumber == null || TextUtils.isEmpty(phoneNumber))
            return;
        switch (select_user_dialog_started_for){
            case SELECT_USER_DIALOG_STARTED_FOR_CALL:
                StartCall(phoneNumber);
                break;
            case SELECT_USER_DIALOG_STARTED_FOR_SMS:
                StartSMS(phoneNumber);
                break;
            default:
                return;
        }
    }
    //endregion

    private void StartCall(String phoneNumber){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void StartSMS(String phoneNumber){
        Intent intentSMS = new Intent(Intent.ACTION_SENDTO);
        intentSMS.setType("text/plain");
        intentSMS.setData(Uri.parse("smsto:" + phoneNumber));
        intentSMS.putExtra("sms_body", getString(R.string.pub_det_sms_default_text) + ": " + currentPublication.getTitle());
        if (intentSMS.resolveActivity(getPackageManager()) != null) {
            startActivity(intentSMS);
        }

    }
}
