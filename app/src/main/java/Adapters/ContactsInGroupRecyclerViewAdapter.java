package Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import CommonUtilPackage.ContactItem;
import upp.foodonet.material.R;

/**
 * Created by Asher on 28.05.2016.
 */
public class ContactsInGroupRecyclerViewAdapter extends RecyclerView.Adapter<ContactsInGroupRecyclerViewAdapter.ContactInGroupViewHolder> {
    ArrayList<ContactItem> contacts;

    public ContactsInGroupRecyclerViewAdapter(){
        this.contacts = new ArrayList<>();
    }

    public void setContacts(ArrayList<ContactItem> contacts){
        this.contacts.clear();
        this.contacts.addAll(contacts);
        this.notifyDataSetChanged();
    }

    @Override
    public ContactInGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_in_group_item, parent, false);
        return new ContactInGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactInGroupViewHolder holder, int position) {
        holder.setContactTitle(contacts.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ContactInGroupViewHolder extends RecyclerView.ViewHolder{

        TextView tv_title;

        public ContactInGroupViewHolder(View itemView) {
            super(itemView);
            tv_title = (TextView)itemView.findViewById(R.id.tv_contact_in_group_title);
        }

        public void setContactTitle(String title){
            tv_title.setText(title);
        }
    }
}
