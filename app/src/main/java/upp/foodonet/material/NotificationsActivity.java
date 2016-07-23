package upp.foodonet.material;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import Adapters.NotificationsRecyclerViewAdapter;
import DataModel.FNotification;

public class NotificationsActivity extends AppCompatActivity {

    private final static String MY_TAG = "food_notif";

    RecyclerView rv_notifications;
    NotificationsRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_notifications);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        InitRecyclerView();

    }

    private void InitRecyclerView(){
        rv_notifications = (RecyclerView)findViewById(R.id.rv_notifications);
        rv_notifications.setLayoutManager(new LinearLayoutManager(rv_notifications.getContext()));
        adapter = new NotificationsRecyclerViewAdapter(this);
        rv_notifications.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(notificationsItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv_notifications);
        adapter.UpdateNotificationsList(MakeTestRecords(10));
    }

    ItemTouchHelper.SimpleCallback notificationsItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int swipePosition = viewHolder.getAdapterPosition();
            adapter.RemoveNotificationItem(swipePosition);
            RemoveNotification(((NotificationsRecyclerViewAdapter.NotificationViewHolder)viewHolder).notificationID);
        }
    };

    private void RemoveNotification(int notificationID){
//        int rowsDeleted = getContentResolver().delete(Uri.parse(FooDoNetSQLProvider.URI_NOTIFICATIONS + "/" + String.valueOf(notificationID)), null, null);
//        Toast.makeText(this, "notifications deleted: " + String.valueOf(rowsDeleted), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "notification deleted: " + String.valueOf(notificationID), Toast.LENGTH_SHORT).show();
    }

    private ArrayList<FNotification> MakeTestRecords(int count){
        ArrayList<FNotification> result = new ArrayList<>();
        if(count<=0) return result;
        for(int i = 0; i < count; i++){
            FNotification notification = new FNotification();
            notification.set_id(i);
            notification.set_type(i%5);
            notification.set_date_arrived(new Date());
            notification.set_latitude(0);
            notification.set_longitude(0);
            notification.set_publication_or_group_id(i);
            notification.set_publication_or_group_title("title " + i);
            result.add(notification);
        }
        return result;
    }
}
