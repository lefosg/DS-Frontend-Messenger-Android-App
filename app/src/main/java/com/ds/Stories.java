package com.ds;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

public class Stories extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private View header;
    private Menu menu;
    String name_extra = "name_extra";
    String ip_extra = "ip_extra";
    String username, ip;
    TextView txtuser
            ,txtip;

    Client client;
    ViewPager viewPager;
    StoriesAdapter storiesAdapter;
    FloatingActionButton uploadStoryBtn;
    SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        client = ((MyApp) getApplication()).getClient();
        username = client.getProfile().getUsername();
        ip = this.getIntent().getStringExtra(ip_extra);
        //get header of navigation view
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view_123);


        header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        setNav();

        viewPager = findViewById(R.id.stories_viewPager);

        uploadStoryBtn = findViewById(R.id.upload_story_btn);
        refreshLayout = findViewById(R.id.stories_swiperefresh);

        client = ((MyApp) getApplication()).getClient();
        new GetStoriesTask().execute();

    }

    @Override
    protected void onStart() {
        super.onStart();

        uploadStoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new GetStoriesTask().execute();
                        storiesAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byte_arr = stream.toByteArray();
                MultimediaFile story = new MultimediaFile(byte_arr, getFileName(selectedImage) ,client.getUsername());

                new UploadStoryTask().execute(story);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Async Tasks
    private class UploadStoryTask extends AsyncTask<MultimediaFile, Void, Void> {
        @Override
        protected Void doInBackground(MultimediaFile... multimediaFiles) {
            MultimediaFile story = multimediaFiles[0];
            story.setExpiryDate(new Date(new Date().getTime()+ client.getStory_deletion_delay()));
            //client.saveFile(story, client.getStoryPath());
            client.getProfile().addStory(story);
            client.push(new Value("UPLOAD_STORY"));
            client.push(new Value(story));
            return null;
        }
    }


    private class GetStoriesTask extends AsyncTask<Void, Void, Void> {
        private final ProgressDialog progressDialog = new ProgressDialog(Stories.this);
        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Fetching...");
            this.progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            client.push(new Value("VIEW_STORIES"));
            if (client.get_stories_count() != -1) {
                while (client.get_stories_count() != 0) {
                    if (client.get_stories_count() == 0) {
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            progressDialog.dismiss();
            File[] files = new File(client.getOthersStoriesPath()).listFiles();
            System.out.println(files.length);
            ArrayList<Uri> files_uri = new ArrayList<>();
            for (File f : files) {
                files_uri.add(Uri.fromFile(f));
            }
            System.out.println(files);
            storiesAdapter = new StoriesAdapter(Stories.this, files_uri);
            viewPager.setAdapter(storiesAdapter);
            storiesAdapter.notifyDataSetChanged();
        }
    }


    //nav bar setup
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_profile:
                startProfile();
                break;
            case R.id.nav_chat:
                starMainMenu();
                break;
            case R.id.nav_blocked_users:
                starBlockedUsers();
                break;
            case R.id.nav_friends:
                startFriends();
                break;
            case R.id.nav_notifications:
                startNotifications();
                break;
            case R.id.log_out:
                startLogOut();
                break;
            default:
                break;

        }
        return true;
    }

    public void setNav(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        //set user,ip strings
        txtuser = ((TextView) header.findViewById(R.id.navbar_username));
        txtip = ((TextView) header.findViewById(R.id.navbar_userIP));
        txtuser.setText(username);
        txtip.setText(ip);

        toggle.syncState();
    }
    public void startProfile(){
        Intent intent = new Intent(Stories.this , ProfileActivity.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);

    }

    public void starMainMenu(){
        Intent intent = new Intent(Stories.this , MainMenu.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void starBlockedUsers(){
        Intent intent = new Intent(Stories.this , BlockedUsers.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startFriends(){
        Intent intent = new Intent(Stories.this , Friends.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);

    }

    public void startNotifications(){
        Intent intent = new Intent(Stories.this , Notifications.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startLogOut() {
        new MainMenu.CloseClientTask().execute(client);
        Intent intent = new Intent(Stories.this, LogIn.class);
        Toast.makeText(Stories.this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}