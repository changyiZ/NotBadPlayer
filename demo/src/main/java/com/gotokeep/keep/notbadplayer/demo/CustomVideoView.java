package com.gotokeep.keep.notbadplayer.demo;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.gotokeep.keep.notbadplayer.widgets.VideoView;

/**
 * Description:
 *
 * @author Changyi Zhang
 * @version 1.0
 */

public class CustomVideoView extends VideoView {

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected TouchListener touchListener() {
        return new MyTouchListener(getContext());
    }

    class MyTouchListener extends TouchListener {

        public MyTouchListener(Context context) {
            super(context);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            pause();
            Context context = getContext();
            context.startActivity(new Intent(context, VideoPlayerActivity.class));
            return true;
        }
    }
}
