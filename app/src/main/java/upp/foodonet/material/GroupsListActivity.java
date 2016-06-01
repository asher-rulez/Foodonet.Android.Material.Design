package upp.foodonet.material;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import Adapters.GroupsListRecyclerViewAdapter;
import Adapters.IOnGroupSelecterFromListListener;
import CommonUtilPackage.CommonUtil;
import DataModel.Group;
import DataModel.GroupMember;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;

public class GroupsListActivity
        extends FooDoNetCustomActivityConnectedToService
        implements  View.OnClickListener,
                    IOnGroupSelecterFromListListener {

    private static final String MY_TAG = "food_groupsList";
    public static final int requestCodeNewGroup = 0;
    public static final int requestCodeExistingGroup = 1;

    private FloatingActionButton fab_add_group;
    private RecyclerView rv_groups_list;
    ProgressDialog pd_loadingGroup;

    GroupsListRecyclerViewAdapter groupsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_groups_list);
        //setSupportActionBar(toolbar);

        fab_add_group = (FloatingActionButton) findViewById(R.id.fab_groups);
        fab_add_group.setOnClickListener(this);

        rv_groups_list = (RecyclerView)findViewById(R.id.rv_groups_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(pd_loadingGroup != null)
            pd_loadingGroup.dismiss();
        setupRecyclerView();
    }

    private void setupRecyclerView(){
        rv_groups_list.setLayoutManager(new LinearLayoutManager(rv_groups_list.getContext()));
        ArrayList<Group> groupsList = LoadGroups();
        groupsListAdapter = new GroupsListRecyclerViewAdapter(groupsList, this);
        rv_groups_list.setAdapter(groupsListAdapter);
    }

    private ArrayList<Group> LoadGroups(){
        Cursor groupsCursor = getContentResolver().query(FooDoNetSQLProvider.URI_GROUPS_LIST, Group.GetColumnNamesForListArray(), null, null, null);
        return Group.GetGroupsFromCursorForList(groupsCursor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_groups:
                Intent newGroupIntent = new Intent(this, NewAndExistingGroupActivity.class);
                startActivityForResult(newGroupIntent, requestCodeNewGroup);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void OnGroupSelected(int groupID) {
        pd_loadingGroup = CommonUtil.ShowProgressDialog(this, getString(R.string.loading_group));
        ExistingGroupGetter groupGetter = new ExistingGroupGetter();
        groupGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, groupID);
    }

    public void OnGroupFetchedForOpening(Group group){
        Intent intent = new Intent(this, NewAndExistingGroupActivity.class);
        intent.putExtra(NewAndExistingGroupActivity.extra_key_is_new_group, false);
        intent.putExtra(NewAndExistingGroupActivity.extra_key_existing_group, group);
        startActivityForResult(intent, 1);
        if(pd_loadingGroup != null)
            pd_loadingGroup.dismiss();
    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

/*
    new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
*/

    @Override
    public void onBroadcastReceived(Intent intent){
        int actionCode = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, -1);
        switch (actionCode) {
            case ServicesBroadcastReceiver.ACTION_CODE_RELOAD_DATA_SUCCESS:
                ArrayList<Group> groupList = LoadGroups();
                if(groupsListAdapter == null)
                    setupRecyclerView();
                else {
                    groupsListAdapter.UpdateGroupsList(groupList);
                }
                break;
            default:
                super.onBroadcastReceived(intent);
                break;
        }
    }


    private class ExistingGroupGetter extends AsyncTask<Integer, Void, Void>{
        Group group;

        public ExistingGroupGetter(){}

        @Override
        protected Void doInBackground(Integer... params) {
            group = Group.GetGroupsFromCursor(getContentResolver()
                    .query(Uri.parse(FooDoNetSQLProvider.URI_GROUP + "/" + params[0]),
                            Group.GetColumnNamesArray(), null, null, null)).get(0);
            ArrayList<GroupMember> members = GroupMember.GetGroupMembersFromCursor(getContentResolver()
                    .query(Uri.parse(FooDoNetSQLProvider.URI_GROUP_MEMBERS_BY_GROUP + "/" + params[0]),
                            GroupMember.GetColumnNamesArray(), null, null, null));
            group.set_group_members(members);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(group != null)
                OnGroupFetchedForOpening(group);
        }

    }

}
