package com.suypower.stereo.videomonitor;

import android.content.ContentValues;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Stereo on 2017/4/1.
 */

public class VideoSplit {

    private String uuid;
    private SplitInterFace splitInterFace;


    public VideoSplit(String uuid, SplitInterFace splitInterFace) {
        this.uuid = uuid;
        this.splitInterFace = splitInterFace;
    }



    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            splitInterFace.SplitState((msg.what==1)?true:false);
        }
    };


    public void StartSplit() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        handler.sendEmptyMessage(1);
                        Movie movie = MovieCreator.build(Environment.getExternalStorageDirectory() + "/录像/" + uuid+".mp4");
                        List<Track> tracks = movie.getTracks();
                        movie.setTracks(new LinkedList<Track>());
                        Movie v = new Movie();
                        v.addTrack(tracks.get(1));
                        Mp4Builder defaultMp4Builder = new DefaultMp4Builder();
                        Container c = defaultMp4Builder.build(v);
                        c.writeContainer(new FileOutputStream(Environment.getExternalStorageDirectory() + "/录像/" + uuid+".mp3").getChannel());
                        ContentValues cv = new ContentValues();
                        cv.put("state", 1);
                        SuyDB.getSuyDB().getDb().update("video",cv,"uuid =?",new String[]{uuid});
                        handler.sendEmptyMessage(0);
                    }
                    catch (Exception e)
                    {e.printStackTrace();
                        Log.e("ee",e.getMessage());
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface SplitInterFace {
        void SplitState(boolean b);//true 开始 fasle 结束
    }
}
