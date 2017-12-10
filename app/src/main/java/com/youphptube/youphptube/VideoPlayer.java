package com.youphptube.youphptube;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import net.mm2d.upnp.Device;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.xml.transform.Source;

import static com.youphptube.youphptube.MasterClass.getTimeString;

public class VideoPlayer extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, VideoControllerView.MediaPlayerControl {
    ArrayList<HashMap<String, String>> RelatedVideosList;
    private String VideoID;
    DisplayMetrics dm;
    /*AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    FrameLayout progressBarHolder;*/
    ProgressBar VideoLoadingBar;
    View video_information;
    Spinner QualitySelector;
    MasterClass.VideoInformation CurrentVideo = new MasterClass.VideoInformation();
    MasterClass.VideoSources PlaySource;
    int currenttimestamp = 0;

    SurfaceView videoSurface;
    MediaPlayer player;
    VideoControllerView controller;
    public static Device UpnPDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_player);
       //progressBarHolder = findViewById(R.id.progressBarHolder);
        VideoLoadingBar = findViewById(R.id.videoloding);
        video_information = findViewById(R.id.video_information);
        QualitySelector = findViewById(R.id.qualityselector);

        videoSurface = findViewById(R.id.videoSurface);
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);

        player = new MediaPlayer();
        controller = new VideoControllerView(this);

        QualitySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (PlaySource != null) {
                    if (QualitySelector.getSelectedItemPosition() != PlaySource.SelectedID){
                        for (int i = 0; i < CurrentVideo.Sources.size(); i++) {
                            if (CurrentVideo.Sources.get(i).Quality.equals(QualitySelector.getSelectedItem().toString().trim())) {
                                PlaySource = CurrentVideo.Sources.get(i);
                                PlaySource.SelectedID = i;
                                //Uri uri = Uri.parse(PlaySource.Src);
                                VideoLoadingBar.setVisibility(View.VISIBLE);
                                currenttimestamp = player.getCurrentPosition();
                                StopVideo();
                                try {
                                    player.setOnErrorListener(VideoPlayer.this);
                                    player.setOnPreparedListener(VideoPlayer.this);
                                    player.setDataSource(PlaySource.Src);
                                    player.prepareAsync();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //player.setVideoURI(uri);
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        RelatedVideosList = new ArrayList<>();



        Intent intent = getIntent();
        VideoID = intent.getStringExtra("VideoID");
        if (VideoID!=null){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Getting related videos list
                    new GetRelatedVideos().execute();
                }
            }, 1000);

            //Getting video information and add view count
            //In the future only videoID or URL will be passed and all video information will be get from here
            new GetVideo(VideoID).execute();


            //Add functions to like, dislike and share buttons


            TextView likebutton = findViewById(R.id.like);
            likebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new LikeVideo(VideoID, "like").execute();
                }
            });

            TextView dislikeaction = findViewById(R.id.dislike);
            dislikeaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new LikeVideo(VideoID, "dislike").execute();
                }
            });

            TextView shareaction = findViewById(R.id.share);
            shareaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Share option not available yet", Toast.LENGTH_LONG).show();
                }
            });

            TextView playlist = findViewById(R.id.addto);
            playlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Add to playlist option not available yet", Toast.LENGTH_LONG).show();
                }
            });

            TextView PlayOnTv = findViewById(R.id.playon);
            PlayOnTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent objIndent = new Intent(VideoPlayer.this,UpnPSearch.class);

                    MasterClass.VideoPlaying VideoInformation = new MasterClass.VideoPlaying();

                    VideoInformation.VideoTitle = CurrentVideo.title;
                    VideoInformation.VideoURI = PlaySource.Src;
                    VideoInformation.VideoDuration = CurrentVideo.duration;
                    VideoInformation.CurrentTime =getTimeString(player.getCurrentPosition());

                    objIndent.putExtra("VideoInformation", VideoInformation);
                    startActivity(objIndent);
                    //Stop video in internal player
                    PauseVideo();
                }
            });
        }


    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            AdjustVideoView(true);
            video_information.setVisibility(View.GONE);
        }

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            AdjustVideoView(true);
            video_information.setVisibility(View.VISIBLE);
        }


    }


    void AdjustVideoView(Boolean AdjustHeight){
        dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;

        Double mm = (width / 1.77);

        FrameLayout ff = findViewById(R.id.videoframe);

        if (AdjustHeight){
            ff.setLayoutParams(new LinearLayout.LayoutParams(width,mm.intValue()));
        }else{
            ff.setLayoutParams(new LinearLayout.LayoutParams(width,height));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }

    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        UpnPDevice = null;
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoframe));
        if (currenttimestamp != 0){
            player.seekTo(currenttimestamp);
        }
        player.start();



        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START)
                    VideoLoadingBar.setVisibility(View.VISIBLE);
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END)
                    VideoLoadingBar.setVisibility(View.GONE);

                return false;
            }
        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    // Implement VideoMediaController.MediaPlayerControl
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {
        int orientation = this.getResources().getConfiguration().orientation;

        switch(orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
    }
    // End VideoMediaController.MediaPlayerControl

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        VideoLoadingBar.setVisibility(View.GONE);
        return false;
    }


    private class LikeVideo extends AsyncTask<Void, Void, MasterClass.VideoLikes> {
        private String VideoID;
        private String LikeType;
        private LikeVideo(String videoid, String likeType){
            VideoID = videoid;
            LikeType = likeType;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected MasterClass.VideoLikes doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            MasterClass.VideoLikes LikeInformation = new MasterClass.VideoLikes();
            String jsonStr = sh.LikeVideo(MasterClass.ServerUrl + "/" + LikeType, VideoID, LikeType);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    LikeInformation.VideoID = Integer.parseInt(jsonObj.getString("videos_id"));
                    LikeInformation.Like = Integer.parseInt(jsonObj.getString("likes"));
                    LikeInformation.DisLike = Integer.parseInt(jsonObj.getString("dislikes"));
                    LikeInformation.MyVote = Integer.parseInt(jsonObj.getString("myVote"));


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
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            return LikeInformation;
        }

        @Override
        protected void onPostExecute(MasterClass.VideoLikes LikeInformation) {
            super.onPostExecute(LikeInformation);

            if (LikeInformation.VideoID != null) {
                TextView Likes = findViewById(R.id.like);
                Likes.setText(String.valueOf(LikeInformation.Like));

                TextView DisLikes = findViewById(R.id.dislike);
                DisLikes.setText(String.valueOf(LikeInformation.DisLike));

                if (LikeInformation.MyVote == 1) {
                    Toast.makeText(getApplicationContext(), getString(R.string.AddedToLikeList), Toast.LENGTH_LONG).show();
                    Likes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_up_black_24dp, Color.BLUE), null, null);
                    DisLikes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_down_black_24dp, Color.BLACK), null, null);
                } else if (LikeInformation.MyVote == -1) {
                    Likes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_up_black_24dp, Color.BLACK), null, null);
                    DisLikes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_down_black_24dp, Color.BLUE), null, null);
                } else {
                    Likes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_up_black_24dp, Color.BLACK), null, null);
                    DisLikes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_down_black_24dp, Color.BLACK), null, null);
                }
            }



        }
    }

    Drawable GetLikeButton(int file, int color){
        Drawable myIcon = getResources().getDrawable(file);
        ColorFilter filter = new LightingColorFilter(color,color);
        myIcon.setColorFilter(filter);
        return  myIcon;
    }


    private class GetVideo extends AsyncTask<Void, Void, MasterClass.VideoInformation> {
        private String VideoID;
        private GetVideo(String videoid){
            VideoID = videoid;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected MasterClass.VideoInformation doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            final MasterClass.VideoInformation VideoInfo = new MasterClass.VideoInformation();
            String jsonStr = sh.GetVideo(MasterClass.ServerUrl + "/videoAndroid.json", VideoID);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    VideoInfo.VideoID = jsonObj.getString("id");
                    VideoInfo.Creator = jsonObj.getString("name");
                    VideoInfo.CreatorImage = jsonObj.getString("photoURL");
                    VideoInfo.likes = Integer.parseInt(jsonObj.getString("likes"));
                    VideoInfo.dislikes = Integer.parseInt(jsonObj.getString("dislikes"));
                    VideoInfo.dislikes = Integer.parseInt(jsonObj.getString("dislikes"));
                    VideoInfo.videoAdsCount = Integer.parseInt(jsonObj.getString("videoAdsCount"));
                    VideoInfo.rotation = Integer.parseInt(jsonObj.getString("rotation"));
                    VideoInfo.views_count = Integer.parseInt(jsonObj.getString("views_count"));
                    VideoInfo.order = Integer.parseInt(jsonObj.getString("order"));
                    VideoInfo.myVote = Integer.parseInt(jsonObj.getString("myVote"));

                    VideoInfo.title = jsonObj.getString("title");
                    VideoInfo.clean_title = jsonObj.getString("clean_title");
                    VideoInfo.description = jsonObj.getString("description");
                    VideoInfo.filename = jsonObj.getString("filename");
                    VideoInfo.duration = jsonObj.getString("duration");
                    VideoInfo.type = jsonObj.getString("type");
                    VideoInfo.category = jsonObj.getString("category");
                    VideoInfo.clean_category = jsonObj.getString("clean_category");
                    VideoInfo.videoCreation = jsonObj.getString("videoCreation");
                    VideoInfo.Thumbnail = jsonObj.getString("Thumbnail");
                    VideoInfo.CreatorImage = jsonObj.getString("CreatorImage");


                    JSONArray srcs = new JSONArray(jsonObj.getString("VideoSources"));
                    ArrayList<MasterClass.VideoSources> Sources = new ArrayList<>();
                    for (int i=0; i<srcs.length(); i++) {
                        MasterClass.VideoSources Source = new MasterClass.VideoSources();
                        JSONObject sourceitem = srcs.getJSONObject(i);
                        Source.Src = sourceitem.getString("src");
                        Source.Type = sourceitem.getString("type");
                        Source.Quality = sourceitem.getString("Label");
                        Source.QualityLevel = Integer.valueOf(sourceitem.getString("LevelIndex"));
                        Sources.add(Source);
                    }
                    VideoInfo.Sources = Sources;



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
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            return VideoInfo;
        }

        @Override
        protected void onPostExecute(MasterClass.VideoInformation VideoInformation) {
            super.onPostExecute(VideoInformation);
            CurrentVideo = VideoInformation;

            TextView ViewsCount = findViewById(R.id.views_count);
            ViewsCount.setText(VideoInformation.views_count + " " + getString(R.string.views));

            TextView Likes = findViewById(R.id.like);
            Likes.setText(String.valueOf(VideoInformation.likes));

            TextView DisLikes = findViewById(R.id.dislike);
            DisLikes.setText(String.valueOf(VideoInformation.dislikes));


            if (VideoInformation.myVote == 1){
                Likes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_up_black_24dp, Color.BLUE), null, null);
                DisLikes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_down_black_24dp, Color.BLACK), null, null);
            }else if (VideoInformation.myVote == -1){
                Likes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_up_black_24dp, Color.BLACK), null, null);
                DisLikes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_down_black_24dp, Color.BLUE), null, null);
            }else{
                Likes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_up_black_24dp, Color.BLACK), null, null);
                DisLikes.setCompoundDrawablesWithIntrinsicBounds(null, GetLikeButton(R.drawable.ic_thumb_down_black_24dp, Color.BLACK), null, null);
            }

            //Uri uri= Uri.parse("https://live.youphptube.com:444/live/59be13fe009a6/index.m3u8");
            PlaySource = GetBestVideoSource(VideoInformation.Sources);

            PlayVideo(PlaySource.Src, 0);

            TextView VideoDescriptionView = findViewById(R.id.videodescription);
            VideoDescriptionView.setText(VideoInformation.title);

            TextView ChannelName = findViewById(R.id.channelname);
            ChannelName.setText(VideoInformation.Creator);

            ImageView ChannelImage = findViewById(R.id.creator_image);
            Picasso.with(VideoPlayer.this).load(VideoInformation.CreatorImage).into(ChannelImage);


        }
    }

    void PlayVideo(String VideoURL, int position){
        VideoLoadingBar.setVisibility(View.VISIBLE);
        StopVideo();
        currenttimestamp = position;
        try {
            player.reset();
            player.setOnErrorListener(VideoPlayer.this);
            player.setOnPreparedListener(VideoPlayer.this);
            player.setDataSource(VideoURL);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void StopVideo(){
        if (player.isPlaying()){
            player.stop();
        }
    }
    void PauseVideo(){
        if (player.isPlaying()){
            player.pause();
        }
    }

    MasterClass.VideoSources GetBestVideoSource(ArrayList<MasterClass.VideoSources> Sources){
        //Lets find out the best video quality
        MasterClass.VideoSources PlaySource = new MasterClass.VideoSources();
        //String Source = "";
        Integer Level = 0;
        //Check if wireless is connected
        ConnectivityManager cm = (ConnectivityManager) getBaseContext().getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected()){
            //Lets try to use best quality
            for (int i=0; i<Sources.size(); i++){
                if (Sources.get(i).QualityLevel >= Level){
                    //lets ignore formats 999
                    if (Sources.get(i).QualityLevel != 999){
                        Level = Sources.get(i).QualityLevel;
                        PlaySource.Quality = Sources.get(i).Quality;
                        PlaySource.QualityLevel = Sources.get(i).QualityLevel;
                        PlaySource.Src = Sources.get(i).Src;
                        PlaySource.Type = Sources.get(i).Type;
                    }
                }
            }
        }else{
            //Lets try to use lowest quality
            Level = 500;
            for (int i=0; i<Sources.size(); i++){
                if (Sources.get(i).QualityLevel <= Level){
                    //lets ignore formats 999
                    if (Sources.get(i).QualityLevel != 999) {
                        Level = Sources.get(i).QualityLevel;
                        PlaySource.Quality = Sources.get(i).Quality;
                        PlaySource.QualityLevel = Sources.get(i).QualityLevel;
                        PlaySource.Src = Sources.get(i).Src;
                        PlaySource.Type = Sources.get(i).Type;
                    }
                }
            }
        }

        ArrayAdapter<String> adapter;
        List<String> VideoFormats = new ArrayList<>();
        for (int i=0; i< CurrentVideo.Sources.size(); i++){
            if (CurrentVideo.Sources.get(i).Src.equals(PlaySource.Src)){
                //This is the selected source
                PlaySource.SelectedID = i;
            }
            VideoFormats.add(CurrentVideo.Sources.get(i).Quality);
        }

        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, VideoFormats);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        QualitySelector.setAdapter(adapter);
        QualitySelector.setSelection(PlaySource.SelectedID);

        //Only show format selector if there is more than 1 source
        if (VideoFormats.size()>1){
            QualitySelector.setVisibility(View.GONE);
        }else{
            QualitySelector.setVisibility(View.VISIBLE);
        }

        return PlaySource;
    }


    private class GetRelatedVideos extends AsyncTask<Void, Void, Void> {
        LinearLayout relatedvideos;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            relatedvideos = findViewById(R.id.relatedvideos);
            relatedvideos.removeAllViews();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.GetVideos(MasterClass.ServerUrl + "/videosAndroid.json");
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray Videos = jsonObj.getJSONArray("videos");
                    // looping through All Videos
                    for (int i = 0; i < Videos.length(); i++) {
                        JSONObject c = Videos.getJSONObject(i);
                        // tmp hash map for single contact
                        HashMap<String, String> Video = new HashMap<>();
                        // adding each child node to HashMap key => value
                        Video.put("VideoID", c.getString("id"));
                        Video.put("name", c.getString("name"));
                        Video.put("email", c.getString("email"));
                        Video.put("photoURL", c.getString("photoURL"));
                        Video.put("Thumbnail", c.getString("Thumbnail"));
                        Video.put("duration", c.getString("duration"));
                        Video.put("VideoUrl", c.getString("VideoUrl"));
                        Video.put("title", c.getString("title"));
                        Video.put("clean_title", c.getString("clean_title"));
                        Video.put("description", c.getString("description"));
                        Video.put("views_count", c.getString("views_count"));
                        Video.put("created", c.getString("created"));
                        Video.put("UserPhoto", c.getString("UserPhoto"));

                        RelatedVideosList.add(Video);


                    }
                } catch (final JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "The has an error connecting to server: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "The has an error connecting to server",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ProgressBar relatedvideoloding = findViewById(R.id.relatedvideoloding);
            relatedvideoloding.setVisibility(View.GONE);

            ScrollView relatedscroll = findViewById(R.id.relatedscroll);
            relatedscroll.scrollTo(0,0);


            final VideoAdaptor adapter=new VideoAdaptor(VideoPlayer.this, RelatedVideosList, R.layout.video_list_horizontal);
            final int adapterCount = adapter.getCount();

            //Lets do a random video list
            //We want a maximum of 16 videos to this block
            ArrayList<Integer> RandomVideos = new ArrayList<>();
            Random r = new Random();
            for (int i = 0; i<16; i++){
                int RandomID = r.nextInt(RelatedVideosList.size() - 0) + 0;
                if (RandomVideos.contains(RandomID)){
                    i = i-1;
                }else{
                    RandomVideos.add(RandomID);
                }
            }

            for (int i = 0; i < adapterCount; i++) {
                if (RandomVideos.contains(i)) {
                    View item = adapter.getView(i, null, null);
                    item.setTag(RelatedVideosList.get(i).get("VideoID"));
                    item.setOnClickListener(new AdapterView.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String VideoID = v.getTag().toString();
                            if (VideoID != null) {
                                ScrollView relatedscroll = findViewById(R.id.relatedscroll);
                                relatedscroll.scrollTo(0, 0);
                                ProgressBar relatedvideoloding = findViewById(R.id.relatedvideoloding);
                                relatedvideoloding.setVisibility(View.VISIBLE);
                                relatedvideos.removeAllViews();
                                new GetVideo(VideoID).execute();
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Getting related videos list
                                        new GetRelatedVideos().execute();
                                    }
                                }, 1000);

                            }
                        }

                    });
                    relatedvideos.addView(item);
                }
            }
        }
    }

}


