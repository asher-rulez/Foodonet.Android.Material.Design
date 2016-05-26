package upp.foodonet.material;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class NewAndExistingGroupActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_addMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_and_existing_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_new_existing_group);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        btn_addMembers = (Button)findViewById(R.id.btn_group_add_member);
        btn_addMembers.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_group_add_member:
                Intent addMembersIntent = new Intent(this, SelectContactsForGroupActivity.class);
                startActivityForResult(addMembersIntent, 0);
                break;
        }
    }
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
