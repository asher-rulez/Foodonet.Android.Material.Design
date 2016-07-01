package upp.foodonet.material;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.util.Log;
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
import CommonUtilPackage.InternalRequest;
import DataModel.Group;
import DataModel.GroupMember;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;

public class NewAndExistingGroupActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        IFetchContactsParent,
        IFooDoNetServerCallback {

    private static final String MY_TAG = "food_editGroup";
    public static final String extra_key_contacts = "contacts";
    public static final String extra_key_is_new_group = "isNew";
    public static final String extra_key_existing_group = "group";

    int groupID;

    TextView tv_groupName;
    Button btn_addMembers;
    ProgressDialog pd_loadingContacts;
    RecyclerView rv_contacts_in_group;
    FloatingActionButton fab_saveGroup;
    ProgressDialog pb_savingGroup;

    ContactsInGroupRecyclerViewAdapter adapter;

    boolean IsNewGroup;
    ArrayList<ContactItem> groupContacts;
    Group existingGroup;

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
        //fab_saveGroup.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_inactive_gray)));
        tv_groupName = (TextView)findViewById(R.id.tv_group_name_title);

        Intent intent = getIntent();
        IsNewGroup = intent.getBooleanExtra(extra_key_is_new_group, true);

        if (IsNewGroup) {
            groupID = 0;
            tv_groupName.setText(getIntent().getStringExtra(GroupsListActivity.GROUP_NAME_EXTRA_KEY));
        } else {
            existingGroup = (Group)intent.getSerializableExtra(extra_key_existing_group);
            tv_groupName.setText(existingGroup.Get_name());
        }

        groupContacts = new ArrayList<>();
        rv_contacts_in_group = (RecyclerView) findViewById(R.id.rv_group_member_list);
        SetRecyclerView();
    }

    private void SetRecyclerView() {
        rv_contacts_in_group.setLayoutManager(new LinearLayoutManager(rv_contacts_in_group.getContext()));
        adapter = new ContactsInGroupRecyclerViewAdapter();
        if(!IsNewGroup){
            ArrayList<ContactItem> contactItems = new ArrayList<>();
            for(GroupMember member : existingGroup.get_group_members())
                contactItems.add(new ContactItem(member.get_name(), member.get_phone_number()));
            adapter.setContacts(contactItems);
        }
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
            case R.id.fab_save_group:
                pb_savingGroup = CommonUtil.ShowProgressDialog(this, getString(R.string.saving_group));
                HttpServerConnectorAsync connector = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
                Group g = new Group(tv_groupName.getText().toString(), CommonUtil.GetMyUserID(this));
                GroupMember owner = new GroupMember(0, CommonUtil.GetMyUserID(this), 0, true,
                        CommonUtil.GetMyPhoneNumberFromPreferences(this),
                        CommonUtil.GetSocialAccountNameFromPreferences(this));
                InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_GROUP, getString(R.string.server_post_new_group), g);
                ir.groupOwner = owner;
                ir.groupMembersToAdd = GetGroupMembers();
                ir.MembersServerSubPath = getString(R.string.server_post_add_members_to_group);
                connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (pd_loadingContacts != null) pd_loadingContacts.dismiss();
        if (resultCode == 0) return;
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

    private ArrayList<GroupMember> GetGroupMembers() {
        ArrayList<GroupMember> result = new ArrayList<>();
        for (ContactItem item : groupContacts)
            result.add(new GroupMember(0, 0, groupID, false, item.getPhoneNumber(), item.getName()));
        return result;
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.ActionCommand) {
            case InternalRequest.ACTION_POST_NEW_GROUP:
                switch (response.Status) {
                    case InternalRequest.STATUS_OK:
                        getContentResolver().insert(FooDoNetSQLProvider.URI_GROUP, response.group.GetContentValuesRow());
                        for(GroupMember member : response.group.get_group_members())
                            getContentResolver().insert(FooDoNetSQLProvider.URI_GROUP_MEMBERS, member.GetContentValuesRow());
                        Log.e(MY_TAG, "succeeded to save group");
                        finish();
                        break;
                    case InternalRequest.STATUS_FAIL:
                        Log.e(MY_TAG, "failed to save group");
                        break;
                }
                if (pb_savingGroup != null) pb_savingGroup.dismiss();
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
