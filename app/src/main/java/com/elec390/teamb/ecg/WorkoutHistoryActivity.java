package com.elec390.teamb.ecg;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class WorkoutHistoryActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DataStorage dataStorage;
    private List<SessionEntity> sessions;


    //UI
    private ListView mListView;
    private MenuItem editMenuItem = null;
    private boolean deleteMode = false;
    private SharedPreferenceHelper sharedPreferenceHelper;
    private Profile profile;
    private TextView nameTextView, emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);
        final Context context = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Workout History");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(this);
        Profile profile = new Profile(sharedPreferenceHelper.getProfile());
        nameTextView = headerView.findViewById(R.id.userNameView);
        emailTextView = headerView.findViewById(R.id.userEmailView);
        nameTextView.setText(profile.getName());
        emailTextView.setText(profile.getEmail());

        dataStorage = new DataStorage(context);
        sessions = dataStorage.getSessionList();
        mListView = (ListView) findViewById(R.id.sessionListView);

        // Set ListView adapter to display the toString() of each session in a separate TextView
        final UsersAdapter adapter = new UsersAdapter(this, sessions);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SessionEntity selectedSession = sessions.get(position);
                final int positionToRemove = position;
                AlertDialog.Builder adb1=new AlertDialog.Builder(context);
                adb1.setTitle("View or Delete?");
                adb1.setMessage("Would you like to view or delete session?");
                adb1.setNegativeButton("View", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent detailIntent = new Intent(context, WorkoutSessionDetailsActivity.class);
                        String startTime = DateTypeConverter.dateToString(selectedSession.mSessionStart);
                        detailIntent.putExtra("SESSION_DATE", startTime);
                        detailIntent.putExtra("SESSION_DETAILS", selectedSession.detailsString());
                        detailIntent.putExtra("SESSION_FILENAME",startTime+".ecg");
                        startActivity(detailIntent);
                    }});
                adb1.setPositiveButton("Delete", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder adb2=new AlertDialog.Builder(context);
                        adb2.setTitle("Delete?");
                        adb2.setMessage("Are you sure you want to delete Session?");
                        adb2.setNegativeButton("Cancel", null);
                        adb2.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Remove from database
                                dataStorage.deleteSession(selectedSession);
                                // Delete file
                                File ecgdataroot = new File(Environment.getExternalStorageDirectory(), "ECGData");
                                File ecgdatafile = new File(ecgdataroot,
                                        DateTypeConverter.dateToString(selectedSession.mSessionStart)+".ecg");
                                try {
                                    ecgdatafile.delete();
                                } catch (Exception e) {e.printStackTrace();}
                                sessions.remove(positionToRemove);
                                adapter.notifyDataSetChanged();
                            }});
                        adb2.show();
                    }});
                adb1.show();
            }

        });
    }

    public class UsersAdapter extends ArrayAdapter<SessionEntity> {
        public UsersAdapter(Context context, List<SessionEntity> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            SessionEntity session = sessions.get(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_listview, parent, false);
            }
            // Lookup view for data population
            TextView sId = (TextView) convertView.findViewById(R.id.sId);
            TextView date = (TextView) convertView.findViewById(R.id.date);
            //TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);
            // Populate the data into the template view using the data object
            double duration = session.mSessionEnd.getTime()-session.mSessionStart.getTime();
            Integer minutes = (int) duration/60000;
            Integer seconds = (int) (duration%60000)/1000;
            String line1 = "Workout #" + ((Integer)session.sId).toString() + ": "
                    + minutes.toString() + " min "
                    + seconds.toString()+" sec";
            String line2 = session.mSessionStart.toString();
            sId.setText(line1);
            date.setText(line2);

            // tvHome.setText(user.hometown);
            // Return the completed view to render on screen
            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.workout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_Workout) {
            Log.d("TAG", "Drawer: Workout was selected.");
            intent = new Intent(this,WorkoutActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityIfNeeded(intent,0);
        }
        else if (id == R.id.nav_WorkoutHistory) {
            Log.d("TAG", "Drawer: Session History was selected.");
            intent = new Intent(this,WorkoutHistoryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityIfNeeded(intent,0);
        }
        else if (id == R.id.nav_Settings) {
            Log.d("TAG", "Drawer: Settings was selected.");
            intent = new Intent(this,SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityIfNeeded(intent,0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}