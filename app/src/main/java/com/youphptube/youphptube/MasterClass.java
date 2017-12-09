package com.youphptube.youphptube;

import java.io.Serializable;
import java.util.ArrayList;


class MasterClass {
    static String ServerUrl;
    static String ServerUsername;
    static String ServerPassword;


   static class VideoInformation {
        String VideoID = "";
        ArrayList<MasterClass.VideoSources> Sources = new ArrayList<>();
        String title = "";
        String clean_title = "";
        String description = "";
        String Creator = "";
        String CreatorImage = "";
        Integer views_count = 0;
        String filename = "";
        String duration = "";
        String type = "";
        Integer order = 0;
        Integer rotation = 0;
        String category = "";
        String clean_category = "";
        String videoCreation = "";
        Integer likes = 0;
        Integer dislikes = 0;
        Integer videoAdsCount = 0;
        Integer myVote = 0;
        String Thumbnail = "";
    }

    static class VideoLikes{
       Integer VideoID=0;
       Integer Like = 0;
       Integer DisLike = 0;
       Integer MyVote = 0;
    }

    static class VideoSources{
       String Type = "";
       String Src = "";
       String Quality = "";
       Integer QualityLevel = 0;
       Integer SelectedID = 0;
    }

    static class VideoPlaying implements Serializable{
       String VideoTitle = "";
       String VideoURI = "";
       String VideoDuration = "";
       String CurrentTime = "00:00:00";
    }


    static String getTimeString(long millis) {
       StringBuilder buf = new StringBuilder();

        int hours = (int) (millis / (1000*60*60));
        int minutes = (int) ( millis % (1000*60*60) ) / (1000*60);
        int seconds = (int) ( ( millis % (1000*60*60) ) % (1000*60) ) / 1000;

        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }
}
