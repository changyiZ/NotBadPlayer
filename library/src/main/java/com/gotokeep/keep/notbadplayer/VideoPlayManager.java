package com.gotokeep.keep.notbadplayer;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.gotokeep.keep.notbadplayer.scale.ScaleType;
import com.gotokeep.keep.notbadplayer.widgets.VideoView;

import java.lang.ref.WeakReference;

/**
 * Description:
 *
 * @author Changyi Zhang
 * @version 1.0
 */

public enum  VideoPlayManager {

    INSTANCE;

    private VideoView videoView;

    public Playable hold(@NonNull final VideoView videoView) {
        this.videoView = videoView;
        this.videoView.setReleaseOnDetachFromWindow(false);

        Playable playable = null;
        final ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent != null) {
            final int index = parent.indexOfChild(videoView);
            final Uri uri = videoView.getVideoUri();
            playable = new Playable() {
                @Override
                public ViewGroup parent() {
                    return parent;
                }

                @Override
                public VideoView videoView() {
                    return null;
                }

                @Override
                public int index() {
                    return index;
                }

                @Override
                public Uri uri() {
                    return uri;
                }
            };
            new BackToPlayListener(videoView.getContext(), playable);
        }
        return playable;
    }

    public VideoView smartPlaying(@NonNull ViewGroup parent, @Nullable VideoView videoViewReady, int index, @NonNull Uri uri) {
        if (videoView != null) {
            Uri videoUri = videoView.getVideoUri();
            // It is re-useful only in case of the same Uri.
            if (videoUri != null && videoUri.equals(uri)) {
                // Remove duplicated video view which may existed.
                if (videoViewReady != null) {
                    parent.removeView(videoViewReady);
                }
                // Move reUseful video view from old parent into new one.
                ViewGroup lastParent = (ViewGroup) videoView.getParent();
                if (lastParent != null) {
                    lastParent.removeView(videoView);
                }
                parent.addView(videoView, index);
                // Play it.
                videoView.start();
                return videoView;
            }
        }
        if (videoViewReady == null || videoViewReady.getParent() != parent) {
            videoViewReady = new VideoView(parent.getContext());
            videoViewReady.setScaleType(ScaleType.FIT_CENTER);
            parent.addView(videoViewReady, index);
            videoViewReady.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoViewReady.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        videoViewReady.setVideoURI(uri);
        videoViewReady.start();
        return videoViewReady;
    }

    public void release() {
        if (this.videoView != null) {
            this.videoView.setReleaseOnDetachFromWindow(true);
            this.videoView = null;
        }
    }

    static class BackToPlayListener implements LifecycleObserver {

        private LifecycleOwner lifecycleOwner;
        private WeakReference<Playable> playableWeakReference;
        private boolean ignoreFirstMatch = true;

        public BackToPlayListener(Context context, Playable playable) {
            if (context != null && context instanceof AppCompatActivity) {
                this.playableWeakReference = new WeakReference<>(playable);
                lifecycleOwner = (LifecycleOwner) context;
                lifecycleOwner.getLifecycle().addObserver(this);
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        public void onBackToPlay() {
            if (ignoreFirstMatch) {
                ignoreFirstMatch = false;
                return;
            }

            if (playableWeakReference != null) {
                Playable playable = playableWeakReference.get();
                if (playable != null) {
                    VideoPlayManager.INSTANCE.smartPlaying(playable.parent(), playable.videoView(), playable.index(), playable.uri());
                    lifecycleOwner.getLifecycle().removeObserver(this);
                }
            }
        }
    }

    public interface Playable {
        ViewGroup parent();
        VideoView videoView();
        int index();
        Uri uri();
    }
}
