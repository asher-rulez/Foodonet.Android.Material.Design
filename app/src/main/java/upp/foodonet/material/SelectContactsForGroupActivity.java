package upp.foodonet.material;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import Adapters.ContactPhoneNumbersRecyclerViewAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.ContactItem;

public class SelectContactsForGroupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String MY_TAG = "food_contactsList";

    FloatingActionButton fab_add_members;
    RecyclerView rv_contacts;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts_for_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        fab_add_members = (FloatingActionButton) findViewById(R.id.fab_add_members_to_group);
        fab_add_members.setOnClickListener(this);

        rv_contacts = (RecyclerView)findViewById(R.id.rv_contacts_for_group);

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
    protected void onStart() {
        super.onStart();
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.loading_contacts));
        Map<Integer,ContactItem> contacts = fetchContacts();
        SetupRecyclerView(rv_contacts, contacts);
    }

    @Override
    public void onClick(View v) {

    }

    private void SetupRecyclerView(RecyclerView recyclerView, Map<Integer,ContactItem> contacts){
        rv_contacts.setLayoutManager(new LinearLayoutManager(rv_contacts.getContext()));
        rv_contacts.setAdapter(new ContactPhoneNumbersRecyclerViewAdapter(contacts));
        if(progressDialog != null)
            progressDialog.dismiss();
    }

    public Map<Integer, ContactItem> fetchContacts() {
        Map<Integer, ContactItem> result = new HashMap<>();
        String phoneNumber = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

/*
        Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;
*/

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {
            int counter = 0;
            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                    }
                    phoneCursor.close();
/*
                    // Query and loop for every email of the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{contact_id}, null);
                    while (emailCursor.moveToNext()) {
                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                        output.append("\nEmail:" + email);
                    }
                    emailCursor.close();
*/
                }
                result.put(counter++, new ContactItem(name, phoneNumber));
            }
        }
        return result;
    }

}
