package com.youphptube.youphptube;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import net.mm2d.upnp.Action;
import net.mm2d.upnp.ControlPoint;
import net.mm2d.upnp.Device;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public class MasterActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<HashMap<String, String>> VideosList;
    private ListView lv;

    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;

    FrameLayout progressBarHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBarHolder = findViewById(R.id.progressBarHolder);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        VideosList = new ArrayList<>();
        lv = findViewById(R.id.list);


        final SwipeRefreshLayout RefreshLayout = findViewById(R.id.RefreshLayout);
        RefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new GetVideos().execute();
                        RefreshLayout.setRefreshing(false);
                    }
                }
        );


        SharedPreferences Defs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (Defs.getBoolean("AutoLogin", false)){
            new UserLoginTask(Defs.getString("ServerUrl", ""), Defs.getString("UserName", ""), Defs.getString("Password", "")).execute();
        }else{
            finish();
            Intent myIntent = new Intent(MasterActivity.this, ConfigurationActivity.class);
            startActivity(myIntent);
        }
        //Now only after login has been made we will load the videos
        //new GetVideos().execute();

    }

    private class GetVideos extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            SharedPreferences Defs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String ServerUrl = Defs.getString("ServerUrl", "");
            String url = ServerUrl + "/videosAndroid.json";

            String jsonStr = sh.GetVideos(url);

            if (jsonStr != null) {
                try {
                    VideosList.clear();
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray videos = jsonObj.getJSONArray("videos");

                    // looping through All Contacts
                    for (int i = 0; i < videos.length(); i++) {
                        JSONObject c = videos.getJSONObject(i);

                        // tmp hash map for single contact
                        HashMap<String, String> video = new HashMap<>();

                        // adding each child node to HashMap key => value
                        video.put("VideoID", c.getString("id"));
                        video.put("name", c.getString("name"));
                        video.put("email", c.getString("email"));
                        video.put("photoURL", c.getString("photoURL"));
                        video.put("Thumbnail", c.getString("Thumbnail"));
                        video.put("duration", c.getString("duration"));
                        video.put("VideoUrl", c.getString("VideoUrl"));

                        video.put("title", c.getString("title"));
                        video.put("clean_title", c.getString("clean_title"));
                        video.put("description", c.getString("description"));
                        video.put("views_count", c.getString("views_count"));
                        video.put("created", c.getString("created"));
                        video.put("UserPhoto", c.getString("UserPhoto"));
                        VideosList.add(video);
                    }
                } catch (final JSONException e) {
                    //Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                //Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "There was an error contacting the server",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            VideoAdaptor adapter=new VideoAdaptor(MasterActivity.this, VideosList, R.layout.video_list_normal);

            //ListAdapter adapter = new SimpleAdapter(context,ListaAccoes, R.layout.video_list_normal, new String[] { "Title"}, new int[] {R.id.NomeFicheiro});
            lv.setAdapter(adapter);
            lv.setClickable(true);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                    Object o = lv.getItemAtPosition(position);

                    String VideoID = VideosList.get(position).get("VideoID");
                    if (VideoID!=null) {
                        Intent myIntent = new Intent(MasterActivity.this, VideoPlayer.class);
                        myIntent.putExtra("VideoID", VideoID);
                        startActivity(myIntent);
                    }


                }
            });


            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);

        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.master, menu);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {


        } else if (id == R.id.nav_manage) {
            SharedPreferences Defs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = Defs.edit();
            editor.putBoolean("AutoLogin", false);
            editor.apply();
            finish();
            Intent objIndent = new Intent(MasterActivity.this,ConfigurationActivity.class);
            startActivity(objIndent);
            return true;

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    private class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private String mServerUrl;
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String ServerUrl, String email, String password) {
            mEmail = email;
            mPassword = password;
            mServerUrl = ServerUrl;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            HttpHandler sh = new HttpHandler();
            if (mServerUrl.endsWith("/")) {
                mServerUrl = mServerUrl.substring(0, mServerUrl.length()-1);
            }
            String LoginUrl = mServerUrl + "/login";

            HttpHandler.LoginInfo serverresponse =sh.Login(LoginUrl, mEmail, mPassword);

            if (serverresponse.response.equals("")){
                return 0;
            }

            if (serverresponse.cookie == null){
                return 0;
            }
            if (serverresponse.cookie.contains("PHPSESSID=")){
                if (mEmail.length() == 0 && mPassword.length() == 0){
                    return 1;
                }
            }

            if (serverresponse.response.contains("isLogged\":true,")){
                HttpHandler.cookie = serverresponse.cookie;
                return 1;
            }else return 2;


        }

        @Override
        protected void onPostExecute(final Integer result) {
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
            if (result == 1) {
                SharedPreferences Defs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = Defs.edit();
                editor.putString("ServerUrl", mServerUrl);
                editor.putString("UserName", mEmail);
                editor.putString("Password", mPassword);
                editor.putBoolean("AutoLogin", true);
                MasterClass.ServerUrl = mServerUrl;
                MasterClass.ServerUsername = mEmail;
                MasterClass.ServerPassword = mPassword;
                editor.apply();

                //Loginok lets just get the video list
                new GetVideos().execute();
            } else {
                finish();
                Intent objIndent = new Intent(MasterActivity.this,ConfigurationActivity.class);
                objIndent.putExtra("ErrorType", result);
                startActivity(objIndent);
            }
        }

        @Override
        protected void onCancelled() {
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
        }
    }


}
