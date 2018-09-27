package uiza.v2.home.canslide;

/**
 * Created by www.muathu@gmail.com on 12/24/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.View;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.List;

import uiza.R;
import vn.uiza.core.base.BaseFragment;
import vn.uiza.core.common.Constants;
import vn.uiza.core.utilities.LDialogUtil;
import vn.uiza.core.utilities.LLog;
import vn.uiza.core.utilities.LUIUtil;
import vn.uiza.restapi.uiza.model.v2.getdetailentity.GetDetailEntity;
import vn.uiza.restapi.uiza.model.v2.getlinkplay.GetLinkPlay;
import vn.uiza.restapi.uiza.model.v2.listallentity.Item;
import vn.uiza.uzv1.listerner.ProgressCallback;
import vn.uiza.uzv1.view.rl.video.UZVideoV1;
import vn.uiza.uzv1.view.util.UizaDataV1;
import vn.uiza.uzv1.view.util.UizaInputV1;
import vn.uiza.views.LToast;

public class FrmVideoTop extends BaseFragment implements UZVideoV1.Callback {
    private UZVideoV1 UZVideoV1;

    public UZVideoV1 getUZVideoV1() {
        return UZVideoV1;
    }

    public interface FrmTopCallback {
        public void initDone(boolean isInitSuccess, GetLinkPlay getLinkPlay, GetDetailEntity getDetailEntity);

        public void onClickListEntityRelation(Item item, int position);
    }

    private FrmTopCallback frmTopCallback;

    public void setFrmTopCallback(FrmTopCallback frmTopCallback) {
        this.frmTopCallback = frmTopCallback;
    }

    @Override
    protected String setTag() {
        return getClass().getSimpleName();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        UZVideoV1 = (UZVideoV1) view.findViewById(R.id.uiza_video);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.frm_top;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UZVideoV1.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().isClosedAtLeft() || ((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().isClosedAtRight()) {
            return;
        }
        UZVideoV1.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        UZVideoV1.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        UZVideoV1.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        UZVideoV1.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                UZVideoV1.initializePiP();
            } else {
                LToast.show(getActivity(), "Draw over other app permission not available");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setListener() {
        if (UZVideoV1 == null || UZVideoV1.getPlayer() == null) {
            return;
        }
        UZVideoV1.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                //LLog.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                //LLog.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                //LLog.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                //LLog.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                //LLog.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                //LLog.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                //LLog.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                //LLog.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                //LLog.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onSeekProcessed() {
                //LLog.d(TAG, "onTimelineChanged");
            }
        });
        UZVideoV1.getPlayer().addAudioDebugListener(new AudioRendererEventListener() {
            @Override
            public void onAudioEnabled(DecoderCounters counters) {
                //LLog.d(TAG, "onAudioEnabled");
            }

            @Override
            public void onAudioSessionId(int audioSessionId) {
                //LLog.d(TAG, "onAudioSessionId");
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
        });
        UZVideoV1.setProgressCallback(new ProgressCallback() {
            @Override
            public void onAdProgress(float currentMls, int s, float duration, int percent) {
                //LLog.d(TAG, TAG + " ad progress: " + currentMls + "/" + duration + " -> " + percent + "%");
            }

            @Override
            public void onVideoProgress(float currentMls, int s, float duration, int percent) {
                //LLog.d(TAG, TAG + " video progress: " + currentMls + "/" + duration + " -> " + percent + "%");
            }
        });
        UZVideoV1.getPlayer().addVideoDebugListener(new VideoRendererEventListener() {
            @Override
            public void onVideoEnabled(DecoderCounters counters) {
                //LLog.d(TAG, "onVideoEnabled");
            }

            @Override
            public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
                //LLog.d(TAG, "onVideoDecoderInitialized");
            }

            @Override
            public void onVideoInputFormatChanged(Format format) {
                //LLog.d(TAG, "onVideoInputFormatChanged");
            }

            @Override
            public void onDroppedFrames(int count, long elapsedMs) {
                //LLog.d(TAG, "onDroppedFrames");
            }

            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                //LLog.d(TAG, "onAudioDisabled");
            }

            @Override
            public void onRenderedFirstFrame(Surface surface) {
                //LLog.d(TAG, "onRenderedFirstFrame");
            }

            @Override
            public void onVideoDisabled(DecoderCounters counters) {
                //LLog.d(TAG, "onVideoDisabled");
            }
        });
        UZVideoV1.getPlayer().addMetadataOutput(new MetadataOutput() {
            @Override
            public void onMetadata(Metadata metadata) {
                //LLog.d(TAG, "onMetadata");
            }
        });
        UZVideoV1.getPlayer().addTextOutput(new TextOutput() {
            @Override
            public void onCues(List<Cue> cues) {
                //LLog.d(TAG, "onCues");
            }
        });
    }

    @Override
    public void isInitResult(boolean isInitSuccess, GetLinkPlay getLinkPlay, GetDetailEntity getDetailEntity) {
        if (isInitSuccess) {
            setListener();
            if (frmTopCallback != null) {
                frmTopCallback.initDone(isInitSuccess, getLinkPlay, getDetailEntity);
            }
        } else {
            UizaInputV1 prevUizaInputV1 = UizaDataV1.getInstance().getUizaInputPrev();
            if (prevUizaInputV1 == null) {
                ((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().minimize();
                LUIUtil.setDelay(250, new LUIUtil.DelayCallback() {
                    @Override
                    public void doAfter(int mls) {
                        ((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().closeToRight();
                    }
                });
            } else {
                boolean isPlayPrev = UizaDataV1.getInstance().isTryToPlayPreviousUizaInputIfPlayCurrentUizaInputFailed();
                if (isPlayPrev) {
                    setupVideo(prevUizaInputV1.getEntityId(), prevUizaInputV1.getEntityName(), prevUizaInputV1.getUrlThumnailsPreviewSeekbar(), prevUizaInputV1.getUrlIMAAd(), prevUizaInputV1.getUrlThumnailsPreviewSeekbar(), false);
                } else {
                    ((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().minimize();
                    LUIUtil.setDelay(250, new LUIUtil.DelayCallback() {
                        @Override
                        public void doAfter(int mls) {
                            ((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().closeToRight();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onClickListEntityRelation(Item item, int position) {
        if (frmTopCallback != null) {
            frmTopCallback.onClickListEntityRelation(item, position);
        }
    }

    @Override
    public void onClickBack() {
        ((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().minimize();
    }

    @Override
    public void onClickPip(Intent intent) {
        //LLog.d(TAG, "onClickPip");
        ((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClickPipVideoInitSuccess(boolean isInitSuccess) {
        ((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().closeToRight();
    }

    @Override
    public void onError(Exception e) {
        if (e != null) {
            LLog.e(TAG, "onError " + e.toString());
        }
        ((HomeV2CanSlideActivity) getActivity()).getDraggablePanel().closeToRight();
    }

    public void setupVideo(String entityId, String entityTitle, String entityCover, String urlIMAAd, String urlThumnailsPreviewSeekbar, boolean isTryToPlayPreviousUizaInputIfPlayCurrentUizaInputFailed) {
        if (UizaDataV1.getInstance().isSettingPlayer()) {
            return;
        }
        if (entityId == null || entityId.isEmpty()) {
            LDialogUtil.showDialog1(getActivity(), getActivity().getString(R.string.entity_cannot_be_null_or_empty), new LDialogUtil.Callback1() {
                @Override
                public void onClick1() {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }

                @Override
                public void onCancel() {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            });
            return;
        }
        UizaInputV1 uizaInputV1 = new UizaInputV1();
        uizaInputV1.setEntityId(entityId);
        uizaInputV1.setEntityName(entityTitle);
        uizaInputV1.setEntityCover(entityCover);
        uizaInputV1.setUrlIMAAd(urlIMAAd);
        uizaInputV1.setUrlThumnailsPreviewSeekbar(urlThumnailsPreviewSeekbar);
        UizaDataV1.getInstance().setUizaInput(uizaInputV1, isTryToPlayPreviousUizaInputIfPlayCurrentUizaInputFailed);

        UZVideoV1.init(this);
    }
}
