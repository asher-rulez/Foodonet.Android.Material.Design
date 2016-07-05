package upp.foodonet.material;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;

public class SettingsSelectActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener {

    Button btn_profile_settings;
    Button btn_settings_notifications;
    Button btn_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_settings_select);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btn_profile_settings = (Button)findViewById(R.id.btn_profile_settings);
        btn_profile_settings.setOnClickListener(this);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_profile_settings:
                Intent intent = new Intent(this, ProfileViewAndEditActivity.class);
                startActivity(intent);
                break;
        }
    }


    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }
}
