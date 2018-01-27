package com.gotokeep.keep.notbadplayer.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.source.MediaSource;
import com.gotokeep.keep.notbadplayer.ListenerMux;
import com.gotokeep.keep.notbadplayer.R;
import com.gotokeep.keep.notbadplayer.animation.BottomViewHideShowAnimation;
import com.gotokeep.keep.notbadplayer.core.ExoMediaPlayer;
import com.gotokeep.keep.notbadplayer.core.VideoViewApi;
import com.gotokeep.keep.notbadplayer.listener.OnPreparedListener;
import com.gotokeep.keep.notbadplayer.listener.OnVideoSizeChangedListener;
import com.gotokeep.keep.notbadplayer.listener.VideoControlsButtonListener;
import com.gotokeep.keep.notbadplayer.listener.VideoControlsSeekListener;
import com.gotokeep.keep.notbadplayer.listener.VideoControlsVisibilityListener;
import com.gotokeep.keep.notbadplayer.scale.ScaleType;
import com.gotokeep.keep.notbadplayer.utils.Repeater;

/**
 * Description:
 *
 * @author Changyi Zhang
 * @version 1.0
 */

public class VideoView extends RelativeLayout {

    private Uri videoUri;

    private VideoViewApi videoView;
    private ImageView previewImageView;
    private View startButton;
    private View videoControlPanel;
    private ImageView playButton;
    private TextView timeLabel;
    private SeekBar seekBar;
    private TextView statusLabel;

    private VideoControls videoControls;
    private MuxNotifier muxNotifier;
    private ListenerMux listenerMux;

    private boolean releaseOnDetachFromWindow = true;

    @NonNull
    Repeater progressPollRepeater = new Repeater();

    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs);
    }

    private void setup(Context context, @Nullable AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        initView();
        postInit(new AttributeContainer(context, attrs));
    }

    private void initView() {
        inflateVideoView();

        videoView = findViewById(R.id.video_player_view);
        previewImageView = findViewById(R.id.preview_image);
        startButton = findViewById(R.id.start_button);
        videoControlPanel = findViewById(R.id.player_controller_panel);
        playButton = findViewById(R.id.play_button);
        timeLabel = findViewById(R.id.time_label);
        seekBar = findViewById(R.id.seek_bar);
        statusLabel = findViewById(R.id.status_label);

        muxNotifier = new MuxNotifier();
        listenerMux = new ListenerMux(muxNotifier);
        videoView.setListenerMux(listenerMux);
    }

    protected void inflateVideoView() {
        inflate(getContext(), R.layout.video_view_layout, this);
    }

    private void postInit(@NonNull AttributeContainer attributeContainer) {
        if (attributeContainer.scaleType != null) {
            setScaleType(attributeContainer.scaleType);
        }
        setMeasureBasedOnAspectRatioEnabled(attributeContainer.measureBasedOnAspectRatio);

        //Sets the onTouch listener to show the controls
        videoControls = new VideoControls();
        setOnTouchListener(touchListener());
    }

    protected TouchListener touchListener() {
        return new TouchListener(getContext());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //A poll used to periodically update the progress bar
        progressPollRepeater.setRepeatListener(new Repeater.RepeatListener() {
            @Override
            public void onRepeat() {
                videoControls.updateProgress();
            }
        });

        if (isPlaying()) {
            videoControls.updatePlaybackState(true);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        progressPollRepeater.stop();
        progressPollRepeater.setRepeatListener(null);

        if (!isInEditMode() && releaseOnDetachFromWindow) {
            release();
        }
    }

    /**
     * START
     * <<<<<<<<< Video APIS >>>>>>>>>
     */

    public void repeat() {
        repeat(VideoViewApi.REPEAT_INFINITE);
    }

    public void repeat(int times) {
        videoView.repeat(times);
    }

    /**
     * Retrieves the current Video URI.  If this hasn't been set with {@link #setVideoURI(android.net.Uri)}
     * or {@link #setVideoPath(String)} then null will be returned.
     *
     * @return The current video URI or null
     */
    @Nullable
    public Uri getVideoUri() {
        return videoUri;
    }

    /**
     * Sets the Uri location for the video to play
     *
     * @param uri The video's Uri
     */
    public void setVideoURI(@Nullable Uri uri) {
        this.videoUri = uri;
        videoView.setVideoUri(uri);

        if (videoControls != null) {
            videoControls.showLoading(true);
        }
    }

    /**
     * Sets the Uri location for the video to play
     *
     * @param uri         The video's Uri
     * @param mediaSource MediaSource that should be used
     */
    public void setVideoURI(@Nullable Uri uri, @Nullable MediaSource mediaSource) {
        this.videoUri = uri;
        videoView.setVideoUri(uri, mediaSource);

        if (videoControls != null) {
            videoControls.showLoading(true);
        }
    }

    /**
     * Sets the path to the video.  This path can be a web address (e.g. http://) or
     * an absolute local path (e.g. file://)
     *
     * @param path The path to the video
     */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets how the video should be scaled in the view
     *
     * @param scaleType how to scale the videos
     */
    public void setScaleType(@NonNull ScaleType scaleType) {
        videoView.setScaleType(scaleType);
    }

    /**
     * Measures the underlying {@link VideoViewApi} using the video's aspect ratio if {@code true}
     *
     * @param measureBasedOnAspectRatioEnabled whether to measure using the video's aspect ratio or not
     */
    public void setMeasureBasedOnAspectRatioEnabled(boolean measureBasedOnAspectRatioEnabled) {
        videoView.setMeasureBasedOnAspectRatioEnabled(measureBasedOnAspectRatioEnabled);
    }

    /**
     * Sets the listener to inform of VideoPlayer prepared events
     *
     * @param listener The listener
     */
    public void setOnPreparedListener(@Nullable OnPreparedListener listener) {
        listenerMux.setOnPreparedListener(listener);
    }

    /**
     * Retrieves the duration of the current audio item.  This should only be called after
     * the item is prepared (see {@link #setOnPreparedListener(OnPreparedListener)}).
     *
     * @return The millisecond duration of the video
     */
    public long getDuration() {
        return videoView.getDuration();
    }

    /**
     * Retrieves the current buffer percent of the video.  If a video is not currently
     * prepared or buffering the value will be 0.  This should only be called after the video is
     * prepared (see {@link #setOnPreparedListener(OnPreparedListener)})
     *
     * @return The integer percent that is buffered [0, 100] inclusive
     */
    public int getBufferPercentage() {
        return videoView.getBufferedPercent();
    }

    /**
     * Retrieves the current position of the audio playback.  If an audio item is not currently
     * in playback then the value will be 0.  This should only be called after the item is
     * prepared (see {@link #setOnPreparedListener(OnPreparedListener)})
     *
     * @return The millisecond value for the current position
     */
    public long getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    /**
     * Performs the functionality to stop the progress polling, and stop any other
     * procedures from running that we no longer need.
     */
    protected void onPlaybackEnded() {
        stopPlayback(false);
    }

    /**
     * Returns if a video is currently in playback
     *
     * @return True if a video is playing
     */
    public boolean isPlaying() {
        return videoView.isPlaying();
    }

    /**
     * Starts the playback for the video specified in {@link #setVideoURI(android.net.Uri)}
     * or {@link #setVideoPath(String)}.  This should be called after the VideoView is correctly
     * prepared (see {@link #setOnPreparedListener(OnPreparedListener)})
     */
    public void start() {
        videoView.start();
        setKeepScreenOn(true);

        if (videoControls != null) {
            videoControls.updatePlaybackState(true);
        }
    }

    /**
     * If a video is currently in playback, it will be paused
     */
    public void pause() {
        pause(false);
    }

    /**
     * Pauses the current video in playback, only abandoning the audio focus if
     * <code>transientFocusLoss</code> is <code>false</code>. Calling {@link #pause()} should
     * be used in most cases unless the audio focus is being handled manually
     *
     * @param transientFocusLoss <code>true</code> if the pause is temporary and the audio focus should be retained
     */
    public void pause(boolean transientFocusLoss) {
        videoView.pause();
        setKeepScreenOn(false);

        if (videoControls != null) {
            videoControls.updatePlaybackState(false);
        }
    }

    /**
     * If a video is currently in playback then the playback will be stopped
     */
    public void stopPlayback() {
        stopPlayback(true);
    }

    /**
     * Stops the video currently in playback, making sure to only clear the surface
     * when requested. This allows us to leave the last frame of a video intact when
     * it plays to completion while still clearing it when the user requests playback
     * to stop.
     *
     * @param clearSurface <code>true</code> if the surface should be cleared
     */
    protected void stopPlayback(boolean clearSurface) {
        videoView.stopPlayback(clearSurface);
        setKeepScreenOn(false);
        videoControls.updatePlaybackState(false);
    }

    /**
     * Moves the current video progress to the specified location.
     *
     * @param milliSeconds The time to move the playback to
     */
    public void seekTo(long milliSeconds) {
        if (videoControls != null) {
            videoControls.showLoading(false);
        }

        videoView.seekTo(milliSeconds);
    }

    /**
     * Stops the current video playback and resets the listener states
     * so that we receive the callbacks for events like onPrepared
     */
    public void reset() {
        stopPlayback();
        setVideoURI(null);
    }

    /**
     * <b><em>WARNING:</em></b> Use of this method may cause memory leaks.
     * <p>
     * Enables or disables the automatic release when the VideoView is detached
     * from the window.  Normally this is expected to release all resources used
     * by calling {@link #release()}.  If <code>releaseOnDetach</code> is disabled
     * then {@link #release()} will need to be manually called.
     *
     * @param releaseOnDetach False to disable the automatic release in {@link #onDetachedFromWindow()}
     */
    public void setReleaseOnDetachFromWindow(boolean releaseOnDetach) {
        this.releaseOnDetachFromWindow = releaseOnDetach;
    }

    /**
     * Stops the playback and releases all resources attached to this
     * VideoView.  This should not be called manually unless
     * {@link #setReleaseOnDetachFromWindow(boolean)} has been set.
     */
    public void release() {
        // videoControls = null;
        stopPlayback();

        videoView.release();
    }

    /**
     * If the video has completed playback, calling {@code restart} will seek to the beginning of the video, and play it.
     *
     * @return {@code true} if the video was successfully restarted, otherwise {@code false}
     */
    public boolean restart() {
        if (videoUri == null) {
            return false;
        }

        if (videoView.restart()) {
            if (videoControls != null) {
                videoControls.showLoading(true);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * If a video is currently in playback then the playback will be suspended
     */
    public void suspend() {
        // audioFocusHelper.abandonFocus();
        videoView.suspend();
        setKeepScreenOn(false);

        if (videoControls != null) {
            videoControls.updatePlaybackState(false);
        }
    }

    /**
     * Requests the {@link VideoControls} to become visible.
     */
    public void showControls() {
        videoControls.show();

        if (isPlaying()) {
            videoControls.hideDelayed();
        }
    }

    /**
     * <<<<<<<<< Video APIS >>>>>>>>>
     * END
     */

    static class AttributeContainer {

        ScaleType scaleType = ScaleType.FIT_CENTER;
        boolean measureBasedOnAspectRatio;

        AttributeContainer(Context context, AttributeSet attrs) {
            if (attrs == null) {
                return;
            }

            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VideoView);
            if (typedArray == null) {
                return;
            }

            if (typedArray.hasValue(R.styleable.VideoView_videoScaleType)) {
                scaleType = ScaleType.fromOrdinal(typedArray.getInt(R.styleable.VideoView_videoScaleType, -1));
            }

            if (typedArray.hasValue(R.styleable.VideoView_measureBasedOnAspectRatio)) {
                measureBasedOnAspectRatio = typedArray.getBoolean(R.styleable.VideoView_measureBasedOnAspectRatio, false);
            }

            typedArray.recycle();
        }
    }

    /**
     * Monitors the view click events to show and hide the video controls if they have been specified.
     */
    protected class TouchListener extends GestureDetector.SimpleOnGestureListener implements OnTouchListener {
        protected GestureDetector gestureDetector;

        public TouchListener(Context context) {
            gestureDetector = new GestureDetector(context, this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            // Toggles between hiding and showing the controls
            if (videoControls.isVisible()) {
                videoControls.hide();
            } else {
                showControls();
            }

            return true;
        }
    }

    class MuxNotifier extends ListenerMux.Notifier {

        @Nullable
        public OnVideoSizeChangedListener videoSizeChangedListener;

        @Override
        public boolean shouldNotifyCompletion(long endLeeway) {
            long position = getCurrentPosition();
            long duration = getDuration();
            return position > 0 && duration > 0 && position + endLeeway >= duration;
        }

        @Override
        public void onExoPlayerError(ExoMediaPlayer exoMediaPlayer, Exception e) {
            stopPlayback();

            if (exoMediaPlayer != null) {
                exoMediaPlayer.forcePrepare();
            }
        }

        @Override
        public void onMediaPlaybackEnded() {
            setKeepScreenOn(false);
            // onPlaybackEnded();
            pause();
            // seekTo(0);
        }

        @Override
        public void onSeekComplete() {
            videoControls.finishLoading();
        }

        @Override
        @SuppressWarnings("SuspiciousNameCombination")
        public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees, float pixelWidthHeightRatio) {
            //NOTE: Android 5.0+ will always have an unAppliedRotationDegrees of 0 (ExoPlayer already handles it)
            videoView.setVideoRotation(unAppliedRotationDegrees, false);
            videoView.onVideoSizeChanged(width, height);

            if (videoSizeChangedListener != null) {
                videoSizeChangedListener.onVideoSizeChanged(width, height);
            }
        }

        @Override
        public void onPrepared() {
            videoControls.setDuration(getDuration());
            videoControls.finishLoading();
        }

        @Override
        public void onPreviewImageStateChanged(boolean toVisible) {
            if (previewImageView != null) {
                previewImageView.setVisibility(toVisible ? View.VISIBLE : View.GONE);
            }
        }
    }

    public class VideoControls {

        private static final int DEFAULT_CONTROL_HIDE_DELAY = 2_000;
        private static final int CONTROL_VISIBILITY_ANIMATION_LENGTH = 300;

        private Handler visibilityHandler = new Handler();

        private boolean isLoading = false;
        private long hideDelay = DEFAULT_CONTROL_HIDE_DELAY;
        private boolean isVisible = true;
        private boolean canViewHide = true;
        private boolean userInteracting = false;

        private long duration;

        @Nullable
        protected VideoControlsSeekListener seekListener;
        @Nullable
        protected VideoControlsButtonListener buttonsListener;
        @Nullable
        protected VideoControlsVisibilityListener visibilityListener;

        @NonNull
        protected InternalListener internalListener = new InternalListener();

        VideoControls() {
            if (playButton != null) {
                playButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPlayPauseClick();
                    }
                });
            }
            if (seekBar != null) {
                seekBar.setOnSeekBarChangeListener(new SeekBarChanged());
            }
        }

        /**
         * Performs the functionality when the PlayPause button is clicked.  This
         * includes invoking the callback method if it is enabled, posting the bus
         * event, and toggling the video playback.
         */
        protected void onPlayPauseClick() {
            if (buttonsListener == null || !buttonsListener.onPlayPauseClicked()) {
                internalListener.onPlayPauseClicked();
            }
        }

        public void showLoading(boolean initialLoad) {
            if (isLoading) {
                return;
            }

            isLoading = true;
            if (statusLabel != null) {
                AnimationDrawable animationDrawable = (AnimationDrawable) statusLabel.getResources().getDrawable(R.drawable.tik_tok_progress_drawable);
                statusLabel.setCompoundDrawablesWithIntrinsicBounds(null, animationDrawable, null, null);
                statusLabel.setText(R.string.player_loading);
                statusLabel.setTextColor(Color.parseColor("#FFFFFF"));
                statusLabel.setVisibility(View.VISIBLE);
                animationDrawable.start();
            }
            if (initialLoad) {
                if (videoControlPanel != null) {
                    videoControlPanel.setVisibility(View.GONE);
                }
            } else {
                if (playButton != null) {
                    playButton.setEnabled(false);
                }
                // previousButton.setEnabled(false);
                // nextButton.setEnabled(false);
            }

            show();
        }

        public void finishLoading() {
            if (!isLoading) {
                return;
            }

            isLoading = false;
            if (statusLabel != null) {
                statusLabel.setVisibility(View.INVISIBLE);
            }
            if (videoControlPanel != null) {
                videoControlPanel.setVisibility(View.VISIBLE);
            }

            if (playButton != null) {
                playButton.setEnabled(true);
            }
            // previousButton.setEnabled(enabledViews.get(R.id.exomedia_controls_previous_btn, true));
            // nextButton.setEnabled(enabledViews.get(R.id.exomedia_controls_next_btn, true));

            updatePlaybackState(videoView != null && videoView.isPlaying());
        }

        /**
         * Informs the controls that the playback state has changed.  This will
         * update to display the correct views, and manage progress polling.
         *
         * @param isPlaying True if the media is currently playing
         */
        public void updatePlaybackState(boolean isPlaying) {
            updatePlayPauseImage(isPlaying);
            progressPollRepeater.start();

            if (isPlaying) {
                hideDelayed();
            } else {
                show();
            }
        }

        /**
         * Sets the callbacks to inform of progress seek events
         *
         * @param callbacks The callbacks to inform
         */
        public void setSeekListener(@Nullable VideoControlsSeekListener callbacks) {
            this.seekListener = callbacks;
        }

        /**
         * Specifies the callback to inform of button click events
         *
         * @param callback The callback
         */
        public void setButtonListener(@Nullable VideoControlsButtonListener callback) {
            this.buttonsListener = callback;
        }

        /**
         * Sets the callbacks to inform of visibility changes
         *
         * @param callbacks The callbacks to inform
         */
        public void setVisibilityListener(@Nullable VideoControlsVisibilityListener callbacks) {
            this.visibilityListener = callbacks;
        }

        private String getTimeString(int time) {
            time /= 1000;
            return String.format("%02d:%02d", time / 60, time % 60);
        }

        private void updateTimeLabel(int progress) {
            if (timeLabel != null) {
                timeLabel.setText(getTimeString(progress) + " / " + getTimeString((int) duration));
            }
        }

        public void setDuration(@IntRange(from = 0) long duration) {
            if (this.duration != duration) {
                this.duration = duration;

                if (seekBar != null) {
                    seekBar.setMax((int) duration);
                }
                updateTimeLabel(0);
            }
        }

        /**
         * Called by the {@link #progressPollRepeater} to update the progress
         * bar using the {@link #videoView} to retrieve the correct information
         */
        protected void updateProgress() {
            if (videoView != null) {
                updateProgress(getCurrentPosition(), getDuration(), getBufferPercentage());
            }
        }

        public void updateProgress(@IntRange(from = 0) long position, @IntRange(from = 0) long duration, @IntRange(from = 0, to = 100) int bufferPercent) {
            if (!userInteracting) {
                seekBar.setSecondaryProgress((int) (seekBar.getMax() * ((float) bufferPercent / 100)));
                seekBar.setProgress((int) position);
                updateTimeLabel((int) position);
            }
        }

        /**
         * Makes sure the playPause button represents the correct playback state
         *
         * @param isPlaying If the video is currently playing
         */
        public void updatePlayPauseImage(boolean isPlaying) {
            if (playButton != null) {
                playButton.setImageResource(isPlaying ? R.drawable.icon_player_pause_small : R.drawable.icon_player_play_small);
            }
        }

        /**
         * Returns <code>true</code> if the {@link VideoControls} are visible
         *
         * @return <code>true</code> if the controls are visible
         */
        public boolean isVisible() {
            return isVisible;
        }

        /**
         * Immediately starts the animation to show the controls
         */
        public void show() {
            //Makes sure we don't have a hide animation scheduled
            visibilityHandler.removeCallbacksAndMessages(null);
            clearAnimation();

            animateVisibility(true);
        }

        /**
         * Immediately starts the animation to hide the controls
         */
        public void hide() {
            if (!canViewHide || isLoading) {
                return;
            }

            //Makes sure we don't have a separate hide animation scheduled
            visibilityHandler.removeCallbacksAndMessages(null);
            clearAnimation();

            animateVisibility(false);
        }

        /**
         * After the specified delay the view will be hidden.  If the user is interacting
         * with the controls then we wait until after they are done to start the delay.
         */
        public void hideDelayed() {
            hideDelayed(hideDelay);
        }

        /**
         * After the specified delay the view will be hidden.  If the user is interacting
         * with the controls then we wait until after they are done to start the delay.
         *
         * @param delay The delay in milliseconds to wait to start the hide animation
         */
        public void hideDelayed(long delay) {
            hideDelay = delay;

            if (delay < 0 || !canViewHide || isLoading) {
                return;
            }

            //If the user is interacting with controls we don't want to start the delayed hide yet
            if (!userInteracting) {
                visibilityHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateVisibility(false);
                    }
                }, delay);
            }
        }

        /**
         * Sets the delay to use when hiding the controls via the {@link #hideDelayed()}
         * method. This value will be overridden if {@link #hideDelayed(long)} is called.
         *
         * @param delay The delay in milliseconds to wait to start the hide animation
         */
        public void setHideDelay(long delay) {
            hideDelay = delay;
        }

        /**
         * Sets weather this control can be hidden.
         *
         * @param canHide If this control can be hidden [default: true]
         */
        public void setCanHide(boolean canHide) {
            canViewHide = canHide;
        }

        private void animateVisibility(boolean toVisible) {
            if (isVisible == toVisible) {
                return;
            }

            if (!isLoading) {
                videoControlPanel.startAnimation(new BottomViewHideShowAnimation(videoControlPanel, toVisible, CONTROL_VISIBILITY_ANIMATION_LENGTH));
            }

            isVisible = toVisible;
            onVisibilityChanged();
        }

        /**
         * Performs the functionality to inform the callback
         * that the DefaultControls visibility has changed
         */
        private void onVisibilityChanged() {
            if (visibilityListener == null) {
                return;
            }

            if (isVisible) {
                visibilityListener.onControlsShown();
            } else {
                visibilityListener.onControlsHidden();
            }
        }

        /**
         * An internal class used to handle the default functionality for the
         * VideoControls
         */
        protected class InternalListener implements VideoControlsSeekListener, VideoControlsButtonListener {
            protected boolean pausedForSeek = false;

            @Override
            public boolean onPlayPauseClicked() {
                if (isPlaying()) {
                    pause();
                } else {
                    start();
                }

                return true;
            }

            @Override
            public boolean onPreviousClicked() {
                //Purposefully left blank
                return false;
            }

            @Override
            public boolean onNextClicked() {
                //Purposefully left blank
                return false;
            }

            @Override
            public boolean onRewindClicked() {
                //Purposefully left blank
                return false;
            }

            @Override
            public boolean onFastForwardClicked() {
                //Purposefully left blank
                return false;
            }

            @Override
            public boolean onSeekStarted() {
                if (isPlaying()) {
                    pausedForSeek = true;
                    pause(true);
                }

                show();
                return true;
            }

            @Override
            public boolean onSeekEnded(long seekTime) {
                seekTo(seekTime);

                if (pausedForSeek) {
                    pausedForSeek = false;
                    start();
                    hideDelayed();
                }

                return true;
            }
        }

        /**
         * Listens to the seek bar change events and correctly handles the changes
         */
        class SeekBarChanged implements SeekBar.OnSeekBarChangeListener {
            private long seekToTime;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                seekToTime = progress;
                updateTimeLabel((int) seekToTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userInteracting = true;
                if (seekListener == null || !seekListener.onSeekStarted()) {
                    internalListener.onSeekStarted();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userInteracting = false;
                if (seekListener == null || !seekListener.onSeekEnded(seekToTime)) {
                    internalListener.onSeekEnded(seekToTime);
                }
            }
        }
    }
}
