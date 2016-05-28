package upp.foodonet.material;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Adapters.ContactsInGroupRecyclerViewAdapter;
import AsyncTasks.FetchContactsAsyncTask;
import AsyncTasks.IFetchContactsParent;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.ContactItem;

public class NewAndExistingGroupActivity extends AppCompatActivity implements View.OnClickListener, IFetchContactsParent, TextWatcher {

    private static final String MY_TAG = "food_editGroup";
    public static final String extra_key_contacts = "contacts";

    EditText et_groupTitle;
    Button btn_addMembers;
    ProgressDialog pd_loadingContacts;
    RecyclerView rv_contacts_in_group;
    FloatingActionButton fab_saveGroup;

    ContactsInGroupRecyclerViewAdapter adapter;

    boolean IsNewGroup;
    ArrayList<ContactItem> groupContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_and_existing_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_new_existing_group);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        btn_addMembers = (Button) findViewById(R.id.btn_group_add_member);
        btn_addMembers.setOnClickListener(this);
        fab_saveGroup = (FloatingActionButton) findViewById(R.id.fab_save_group);
        fab_saveGroup.setOnClickListener(this);
        fab_saveGroup.setEnabled(false);
        fab_saveGroup.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_inactive_gray)));
        et_groupTitle = (EditText) findViewById(R.id.et_groupName);
        et_groupTitle.addTextChangedListener(this);
        groupContacts = new ArrayList<>();
        rv_contacts_in_group = (RecyclerView) findViewById(R.id.rv_group_member_list);
        SetRecyclerView();
    }

    private void SetRecyclerView() {
        rv_contacts_in_group.setLayoutManager(new LinearLayoutManager(rv_contacts_in_group.getContext()));
        adapter = new ContactsInGroupRecyclerViewAdapter();
        rv_contacts_in_group.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_group_add_member:
                pd_loadingContacts = CommonUtil.ShowProgressDialog(this, getString(R.string.loading_contacts));
                FetchContactsAsyncTask contactsAsyncTask = new FetchContactsAsyncTask(getContentResolver(), this);
                contactsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, groupContacts);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (pd_loadingContacts != null) pd_loadingContacts.dismiss();
        if(resultCode == 0) return;
        HashMap<Integer, ContactItem> selectedContacts = (HashMap<Integer, ContactItem>) data.getSerializableExtra(extra_key_contacts);
        groupContacts.clear();
        groupContacts.addAll(selectedContacts.values());
        adapter.setContacts(groupContacts);
    }

    @Override
    public void OnContactsFetched(HashMap<Integer, ContactItem> contacts) {
        Intent addMembersIntent = new Intent(this, SelectContactsForGroupActivity.class);
        addMembersIntent.putExtra(extra_key_contacts, contacts);
        startActivityForResult(addMembersIntent, 0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        fab_saveGroup.setEnabled(s.length() > 0);
        fab_saveGroup.setBackgroundTintList(s.length() > 0
                ? ColorStateList.valueOf(getResources().getColor(R.color.plus_button_red))
                : ColorStateList.valueOf(getResources().getColor(R.color.fab_inactive_gray)));
    }

    @Override
    public void afterTextChanged(Editable s) {

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
