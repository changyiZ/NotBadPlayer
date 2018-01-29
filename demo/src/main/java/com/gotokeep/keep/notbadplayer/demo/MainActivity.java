package com.gotokeep.keep.notbadplayer.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gotokeep.keep.notbadplayer.VideoPlayManager;
import com.gotokeep.keep.notbadplayer.widgets.VideoView;

/**
 * Description:
 *
 * @author Changyi Zhang
 * @version 1.0
 */

public class MainActivity extends AppCompatActivity {

    VideoView videoView;
    VideoPlayManager.Playable playable;

    // public static final String video_url = "http://v1.keepcdn.com/video/2018/01/25/14/fe3a3ca69799859804b9c00d1ace29007adf40a2.mp4";
    // public static final String video_url = "https://v1.keepcdn.com/2018/01/25/19/515052e693a686b830563b086601ef8973155cfe.mp4";
    // public static final String video_url = "http://v1.keepcdn.com/video/2018/01/26/23/520413d14dff2211df21e9b74c8a88165d73be74.mp4";
    public static final String video_url = "http://v1.keepcdn.com/video/2018/01/26/22/44f247b620af8c2efba07f3e14df156a764967f4.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.video_view);
        videoView.repeat(3);
        videoView.setVideoPath(video_url);
        // videoView.setVideoURI(Uri.fromFile(new File("/sdcard/training_video_1.mp4")));
        videoView.start();

        findViewById(R.id.go_detail_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                ActivityOptionsCompat options = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    View videoContainer = findViewById(R.id.video_container);
                    options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(MainActivity.this, videoContainer,
                                    videoContainer.getTransitionName());
                }
                playable = VideoPlayManager.INSTANCE.hold(videoView);
                startActivity(intent, options.toBundle());
            }
        });
    }
}
