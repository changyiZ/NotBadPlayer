package com.gotokeep.keep.notbadplayer.demo;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.gotokeep.keep.notbadplayer.VideoPlayManager;
import com.gotokeep.keep.notbadplayer.widgets.VideoView;

/**
 * Description:
 *
 * @author Changyi Zhang
 * @version 1.0
 */

public class VideoPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_player);
        VideoView videoView = findViewById(R.id.video_view);
        videoView = VideoPlayManager.INSTANCE.smartPlaying((ViewGroup) findViewById(R.id.video_container2), videoView, 0, Uri.parse(MainActivity.video_url));
    }
}
