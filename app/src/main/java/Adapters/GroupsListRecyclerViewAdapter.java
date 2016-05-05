package Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import DataModel.Group;
import upp.foodonet.material.R;

/**
 * Created by Asher on 30.04.2016.
 */
public class GroupsListRecyclerViewAdapter extends RecyclerView.Adapter<GroupsListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Group> groupsList;
    private IOnGroupSelecterFromListListener groupSelectedListener;

    public GroupsListRecyclerViewAdapter(ArrayList<Group> groups, IOnGroupSelecterFromListListener listener){
        groupsList = new ArrayList<>();
        groupsList.addAll(groups);
        groupSelectedListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Group group = groupsList.get(position);
        holder.setGroupID(group.Get_id());
        holder.setGroupMembersCount(group.get_members_count());
        holder.setGroupTitle(group.Get_name());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupSelectedListener.OnGroupSelected(holder.getGroupID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView groupTitle;
        private TextView groupMembersCount;
        private int groupID;
        public View view;


        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            groupTitle = (TextView)view.findViewById(R.id.tv_groups_list_item_title);
            groupMembersCount = (TextView)view.findViewById(R.id.tv_groups_list_item_members_count);
        }

        public void setGroupTitle(String title){
            groupTitle.setText(title);
        }
        public void setGroupMembersCount(int count){
            groupMembersCount.setText(count);
        }
        public void setGroupID(int id){
            groupID = id;
        }
        public int getGroupID(){
            return groupID;
        }
    }
}
