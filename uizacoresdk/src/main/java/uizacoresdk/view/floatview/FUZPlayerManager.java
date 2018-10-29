/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uizacoresdk.view.floatview;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.Surface;

import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.ArrayList;
import java.util.List;

import uizacoresdk.listerner.ProgressCallback;
import uizacoresdk.listerner.VideoAdPlayerListerner;
import vn.uiza.core.utilities.LUIUtil;
import vn.uiza.restapi.uiza.model.v2.listallentity.Subtitle;
import vn.uiza.utils.util.AppUtils;

/**
 * Manages the {@link ExoPlayer}, the IMA plugin and all video playback.
 */
/* package */ public final class FUZPlayerManager implements AdsMediaSource.MediaSourceFactory {
    private final String TAG = getClass().getSimpleName();
    private Context context;

    private FUZVideo FUZVideo;
    private DebugTextViewHelper debugTextViewHelper;
    private ImaAdsLoader adsLoader = null;
    private final DataSource.Factory manifestDataSourceFactory;
    private final DataSource.Factory mediaDataSourceFactory;

    private SimpleExoPlayer player;

    private String userAgent;
    private String linkPlay;
    private List<Subtitle> subtitleList;

    private VideoAdPlayerListerner videoAdPlayerListerner = new VideoAdPlayerListerner();

    private Handler handler;
    private Runnable runnable;

    private ProgressCallback progressCallback;

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public FUZPlayerManager(final FUZVideo uizaIMAVideo, String linkPlay, String urlIMAAd, String thumbnailsUrl, List<Subtitle> subtitleList) {
        this.context = uizaIMAVideo.getContext();
        this.FUZVideo = uizaIMAVideo;
        this.linkPlay = linkPlay;
        //LLog.d(TAG, "UZPlayerManagerV1 linkPlay " + linkPlay);
        this.subtitleList = subtitleList;
        if (urlIMAAd == null || urlIMAAd.isEmpty()) {
            //LLog.d(TAG, "UZPlayerManagerV1 urlIMAAd == null || urlIMAAd.isEmpty()");
        } else {
            adsLoader = new ImaAdsLoader(context, Uri.parse(urlIMAAd));
        }
        userAgent = Util.getUserAgent(context, AppUtils.getAppPackageName());
        manifestDataSourceFactory = new DefaultDataSourceFactory(context, userAgent);
        mediaDataSourceFactory = new DefaultDataSourceFactory(
                context,
                userAgent,
                new DefaultBandwidthMeter());
        //LLog.d(TAG, "UZPlayerManagerV1 thumbnailsUrl " + thumbnailsUrl);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (uizaIMAVideo.getPlayerView() != null) {
                    boolean isPlayingAd = videoAdPlayerListerner.isPlayingAd();
                    //LLog.d(TAG, "isPlayingAd " + isPlayingAd);
                    if (isPlayingAd) {
                        hideProgress();
                        if (progressCallback != null) {
                            VideoProgressUpdate videoProgressUpdate = adsLoader.getAdProgress();
                            float mls = videoProgressUpdate.getCurrentTime();
                            float duration = videoProgressUpdate.getDuration();
                            int percent = (int) (mls * 100 / duration);
                            int s = Math.round(mls / 1000);
                            //LLog.d(TAG, "runnable ad mls: " + mls + ", s: " + s + ", duration: " + duration + ", percent: " + percent + "%");
                            progressCallback.onAdProgress(mls, s, duration, percent);
                        }
                    } else {
                        if (progressCallback != null) {
                            if (player != null) {
                                float mls = player.getCurrentPosition();
                                float duration = player.getDuration();
                                int percent = (int) (mls * 100 / duration);
                                int s = Math.round(mls / 1000);
                                //LLog.d(TAG, "runnable video mls: " + mls + ", s: " + s + ", duration: " + duration + ", percent: " + percent + "%");
                                progressCallback.onVideoProgress(mls, s, duration, percent);
                            }
                        }
                    }
                    if (handler != null && runnable != null) {
                        handler.postDelayed(runnable, 1000);
                    }
                }
            }
        };
        handler.postDelayed(runnable, 0);

        //playerView.setControllerAutoShow(false);
        uizaIMAVideo.getPlayerView().setControllerShowTimeoutMs(0);
    }

    private DefaultTrackSelector trackSelector;

    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    public void init() {
        reset();

        //Exo Player Initialization
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        FUZVideo.getPlayerView().setPlayer(player);

        MediaSource mediaSourceVideo = createMediaSourceVideo();

        //merge title to media source video
        //SUBTITLE
        MediaSource mediaSourceWithSubtitle = createMediaSourceWithSubtitle(mediaSourceVideo);

        //merge ads to media source subtitle
        //IMA ADS
        // Compose the content media source into a new AdsMediaSource with both ads and content.
        MediaSource mediaSourceWithAds = createMediaSourceWithAds(mediaSourceWithSubtitle);

        // Prepare the player with the source.
        //player.seekTo(contentPosition);
        //LLog.d(TAG, "init seekTo contentPosition: " + contentPosition);
        player.addListener(new PlayerEventListener());
        player.addAudioDebugListener(new AudioEventListener());
        player.addVideoDebugListener(new VideoEventListener());
        player.addMetadataOutput(new MetadataOutputListener());
        player.addTextOutput(new TextOutputListener());

        if (adsLoader != null) {
            adsLoader.addCallback(videoAdPlayerListerner);
        }
        player.prepare(mediaSourceWithAds);
        //player.seekTo(0);
        player.setPlayWhenReady(true);
    }

    public void seekTo(long position) {
        if (player == null) {
            return;
        }
        player.seekTo(position);
    }

    private MediaSource createMediaSourceVideo() {
        //Video Source
        //MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(linkPlay));
        MediaSource mediaSourceVideo = buildMediaSource(Uri.parse(linkPlay));
        return mediaSourceVideo;
    }

    private MediaSource createMediaSourceWithSubtitle(MediaSource mediaSource) {
        if (subtitleList == null || subtitleList.isEmpty()) {
            return mediaSource;
        }
        //LLog.d(TAG, "createMediaSourceWithSubtitle " + gson.toJson(subtitleList));

        List<SingleSampleMediaSource> singleSampleMediaSourceList = new ArrayList<>();
        for (int i = 0; i < subtitleList.size(); i++) {
            Subtitle subtitle = subtitleList.get(i);
            if (subtitle == null || subtitle.getLanguage() == null || subtitle.getUrl() == null || subtitle.getUrl().isEmpty()) {
                continue;
            }
            DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context, userAgent, bandwidthMeter2);
            //Text Format Initialization
            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.TEXT_VTT, null, Format.NO_VALUE, Format.NO_VALUE, subtitle.getLanguage(), null, Format.OFFSET_SAMPLE_RELATIVE);
            SingleSampleMediaSource textMediaSource = new SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(subtitle.getUrl()), textFormat, C.TIME_UNSET);
            singleSampleMediaSourceList.add(textMediaSource);
        }
        MediaSource mediaSourceWithSubtitle = null;
        for (int i = 0; i < singleSampleMediaSourceList.size(); i++) {
            SingleSampleMediaSource singleSampleMediaSource = singleSampleMediaSourceList.get(i);
            if (i == 0) {
                mediaSourceWithSubtitle = new MergingMediaSource(mediaSource, singleSampleMediaSource);
            } else {
                mediaSourceWithSubtitle = new MergingMediaSource(mediaSourceWithSubtitle, singleSampleMediaSource);
            }
        }
        return mediaSourceWithSubtitle;
    }

    private MediaSource createMediaSourceWithAds(MediaSource mediaSource) {
        if (adsLoader == null) {
            return mediaSource;
        }
        MediaSource mediaSourceWithAds = new AdsMediaSource(
                mediaSource,
                this,
                adsLoader,
                FUZVideo.getPlayerView().getOverlayFrameLayout(),
                null,
                null);
        return mediaSourceWithAds;
    }

    //return true if toggleResume
    //return false if togglePause
    public boolean togglePauseResume() {
        if (player == null) {
            return false;
        }
        if (player.getPlayWhenReady()) {
            pauseVideo();
            return false;
        } else {
            resumeVideo();
            return true;
        }
    }

    public void resumeVideo() {
        player.setPlayWhenReady(true);
    }

    public void pauseVideo() {
        player.setPlayWhenReady(false);
    }

    public void reset() {
        if (player != null) {
            //contentPosition = player.getContentPosition();
            player.release();
            player = null;

            handler = null;
            runnable = null;

            if (debugTextViewHelper != null) {
                debugTextViewHelper.stop();
                debugTextViewHelper = null;
            }
        }
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;

            handler = null;
            runnable = null;

            if (debugTextViewHelper != null) {
                debugTextViewHelper.stop();
                debugTextViewHelper = null;
            }
        }
        if (adsLoader != null) {
            adsLoader.release();
        }
    }

    // AdsMediaSource.MediaSourceFactory implementation.

    /*@Override
    public MediaSource createMediaSource(Uri uri, @Nullable Handler handler, @Nullable MediaSourceEventListener listener) {
        return buildMediaSource(uri, handler, listener);
    }*/

    @Override
    public MediaSource createMediaSource(Uri uri) {
        return buildMediaSource(uri);
    }

    @Override
    public int[] getSupportedTypes() {
        // IMA does not support Smooth Streaming ads.
        return new int[]{C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER};
    }

    // Internal methods.
    private MediaSource buildMediaSource(Uri uri) {
        @ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        manifestDataSourceFactory)
                        .createMediaSource(uri);
            //.createMediaSource(uri, handler, listener);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), manifestDataSourceFactory)
                        .createMediaSource(uri);
            //.createMediaSource(uri, handler, listener);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(uri);
            //.createMediaSource(uri, handler, listener);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(uri);
            //.createMediaSource(uri, handler, listener);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private void hideProgress() {
        LUIUtil.hideProgressBar(FUZVideo.getProgressBar());
    }

    private void showProgress() {
        LUIUtil.showProgressBar(FUZVideo.getProgressBar());
    }

    public class PlayerEventListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            //LLog.d(TAG, "onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            //LLog.d(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            //LLog.d(TAG, "onLoadingChanged isLoading " + isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            //LLog.d(TAG, "onPlayerStateChanged playWhenReady: " + playWhenReady);
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    showProgress();
                    break;
                case Player.STATE_ENDED:
                    if (FUZVideo != null) {
                        FUZVideo.onPlayerStateEnded();
                    }
                    hideProgress();
                    break;
                case Player.STATE_IDLE:
                    showProgress();
                    break;
                case Player.STATE_READY:
                    hideProgress();
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            //LLog.d(TAG, "onRepeatModeChanged repeatMode: " + repeatMode);
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            //LLog.d(TAG, "onShuffleModeEnabledChanged shuffleModeEnabled: " + shuffleModeEnabled);
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            //LLog.d(TAG, "onPlayerError " + error.toString());
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            //LLog.d(TAG, "onPositionDiscontinuity");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            //LLog.d(TAG, "onPlaybackParametersChanged");
        }

        @Override
        public void onSeekProcessed() {
            //LLog.d(TAG, "onSeekProcessed");
        }
    }

    public class AudioEventListener implements AudioRendererEventListener {

        @Override
        public void onAudioEnabled(DecoderCounters counters) {
            //LLog.d(TAG, "onAudioEnabled");
        }

        @Override
        public void onAudioSessionId(int audioSessionId) {
            //LLog.d(TAG, "onAudioSessionId audioSessionId: " + audioSessionId);
        }

        @Override
        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
            //LLog.d(TAG, "onAudioDecoderInitialized");
        }

        @Override
        public void onAudioInputFormatChanged(Format format) {
            //LLog.d(TAG, "onAudioInputFormatChanged");
        }

        @Override
        public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
            //LLog.d(TAG, "onAudioSinkUnderrun");
        }

        @Override
        public void onAudioDisabled(DecoderCounters counters) {
            //LLog.d(TAG, "onAudioDisabled");
        }
    }

    public class VideoEventListener implements VideoRendererEventListener {

        @Override
        public void onVideoEnabled(DecoderCounters counters) {
            //LLog.d(TAG, "onVideoEnabled");
        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
            //LLog.d(TAG, "onVideoDecoderInitialized decoderName: " + decoderName + ", initializedTimestampMs " + initializedTimestampMs + ", initializationDurationMs " + initializationDurationMs);
        }

        @Override
        public void onVideoInputFormatChanged(Format format) {
            //LLog.d(TAG, "onVideoInputFormatChanged");
        }

        @Override
        public void onDroppedFrames(int count, long elapsedMs) {
            //LLog.d(TAG, "onDroppedFrames count " + count + ",elapsedMs " + elapsedMs);
        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            //LLog.d(TAG, "onVideoSizeChanged " + width + "x" + height + ", pixelWidthHeightRatio " + pixelWidthHeightRatio);
        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {
            //LLog.d(TAG, "onRenderedFirstFrame");
            FUZVideo.onStateReadyFirst();
        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {
            //LLog.d(TAG, "onVideoDisabled");
        }
    }

    public class MetadataOutputListener implements MetadataOutput {

        @Override
        public void onMetadata(Metadata metadata) {
            //LLog.d(TAG, "onMetadata " + metadata.length());
        }
    }

    public class TextOutputListener implements TextOutput {

        @Override
        public void onCues(List<Cue> cues) {
            //LLog.d(TAG, "onCues " + cues.size());
        }
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }
}
