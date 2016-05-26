package Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import CommonUtilPackage.ContactItem;
import upp.foodonet.material.R;

/**
 * Created by Asher on 26.05.2016.
 */
public class ContactPhoneNumbersRecyclerViewAdapter extends RecyclerView.Adapter<ContactPhoneNumbersRecyclerViewAdapter.ContactViewHolder> {

    private Map<Integer,ContactItem> contacts;
    private Map<Integer,ContactItem> selectedContacts;

    public ContactPhoneNumbersRecyclerViewAdapter(Map<Integer,ContactItem> contacts){
        this.contacts = contacts;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
        return new ContactViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        holder.setContactItem(position, contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void AddToSelected(int id){
        if(selectedContacts == null)
            selectedContacts = new HashMap<>();
        selectedContacts.put(id, contacts.get(id));
    }

    public void RemoveFromSelected(int id){
        selectedContacts.remove(id);
    }

    public Map<Integer, ContactItem> getSelectedContacts(){
        return selectedContacts;
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        public final View view;
        private ContactPhoneNumbersRecyclerViewAdapter parent;

        public void setContactPhoneNumber(String phoneNumber){
            contactPhoneNumber.setText(phoneNumber);
        }

        public void setContactTitle(String name){
            contactTitle.setText(name);
        }

        public void setContactItem(int id, ContactItem item){
            setId(id);
            setContactTitle(item.getName());
            setContactPhoneNumber(item.getPhoneNumber());
        }

        private int id;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public final TextView contactTitle;
        public String title;
        public final TextView contactPhoneNumber;
        public String phoneNumber;
        public final CheckBox cb_isSelected;
        public boolean isSelected;

        public ContactViewHolder(View itemView, ContactPhoneNumbersRecyclerViewAdapter adapter) {
            super(itemView);
            view = itemView;
            parent = adapter;
            contactTitle = (TextView)itemView.findViewById(R.id.tv_contact_title);
            contactPhoneNumber = (TextView)itemView.findViewById(R.id.tv_contact_phone_number);
            cb_isSelected = (CheckBox)itemView.findViewById(R.id.cb_contact_selected);
            cb_isSelected.setOnCheckedChangeListener(this);
        }


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked)
                parent.AddToSelected(getId());
            else parent.RemoveFromSelected(getId());
        }
    }

}
