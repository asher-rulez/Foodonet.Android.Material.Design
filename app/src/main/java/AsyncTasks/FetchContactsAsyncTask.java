package AsyncTasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;

import CommonUtilPackage.ContactItem;

/**
 * Created by Asher on 28.05.2016.
 */
public class FetchContactsAsyncTask extends AsyncTask<ArrayList<ContactItem>, Void, Void> {

    ContentResolver contentResolver;
    HashMap<Integer, ContactItem> result;
    IFetchContactsParent callback;
    HashMap<String, String> existingContacts;

    public FetchContactsAsyncTask(ContentResolver resolver, IFetchContactsParent callback){
        contentResolver = resolver;
        result = new HashMap<>();
        existingContacts = new HashMap<>();
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(ArrayList<ContactItem>... params) {
        int counter = 0;
        for(ContactItem item : params[0]){
            item.setIsSelected(true);
            result.put(counter++, item);
            existingContacts.put(item.getPhoneNumber(), item.getName());
        }
        fetchContacts();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        callback.OnContactsFetched(result);
    }

    public void fetchContacts() {
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

        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {
            int counter = result.size();
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
                    if(existingContacts.containsKey(phoneNumber)) continue;
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
    }

}
