package uizacoresdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

import uizacoresdk.util.UZData;
import vn.uiza.core.utilities.LLog;
import vn.uiza.core.utilities.LUIUtil;

/**
 * Created by loitp on 6/8/2018.
 */

//https://github.com/google/ExoPlayer/issues/4031
//I want to to show playback controls only when onTouch event is fired. How to prevent control buttons being showed up when on long pressing, dragging etc.?

public final class UZPlayerView extends PlayerView implements PlayerControlView.VisibilityListener {
    private final String TAG = getClass().getSimpleName();
    private static final float DRAG_THRESHOLD = 10;//original 10
    private static final long LONG_PRESS_THRESHOLD_MS = 500;//original 500

    private boolean controllerVisible;
    private long tapStartTimeMs;
    private float tapPositionX;
    private float tapPositionY;

    public UZPlayerView(Context context) {
        this(context, null);
    }

    public UZPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private GestureDetector mDetector;

    private OnTouchEvent onTouchEvent;

    public UZPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setControllerVisibilityListener(this);
        mDetector = new GestureDetector(context, new UizaGestureListener());
    }

    public boolean isControllerVisible() {
        return controllerVisible;
    }

    public interface ControllerStateCallback {
        public void onVisibilityChange(boolean isShow);
    }

    private ControllerStateCallback controllerStateCallback;

    public void setControllerStateCallback(ControllerStateCallback controllerStateCallback) {
        this.controllerStateCallback = controllerStateCallback;
    }

    @Override
    public void onVisibilityChange(int visibility) {
        controllerVisible = visibility == View.VISIBLE;
        //LLog.d(TAG, "onVisibilityChange visibility controllerVisible " + controllerVisible);
        if (controllerStateCallback != null) {
            controllerStateCallback.onVisibilityChange(controllerVisible);
        }
    }

    public void toggleShowHideController() {
        if (controllerVisible) {
            hideController();
        } else {
            showController();
        }
    }

    @Override
    public void showController() {
        LLog.d(TAG, "showController");
        super.showController();
    }

    @Override
    public void hideController() {
        LLog.d(TAG, "hideController");
        super.hideController();
    }

    public void setOnTouchEvent(OnTouchEvent onTouchEvent) {
        this.onTouchEvent = onTouchEvent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /*switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                tapStartTimeMs = SystemClock.elapsedRealtime();
                tapPositionX = ev.getX();
                tapPositionY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (tapStartTimeMs != 0
                        && (Math.abs(ev.getX() - tapPositionX) > DRAG_THRESHOLD
                        || Math.abs(ev.getY() - tapPositionY) > DRAG_THRESHOLD)) {
                    tapStartTimeMs = 0;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (tapStartTimeMs != 0) {
                    if (SystemClock.elapsedRealtime() - tapStartTimeMs < LONG_PRESS_THRESHOLD_MS) {
                        if (!controllerVisible) {
                            //LLog.d(TAG, "showController");
                            showController();
                            if (controllerStateCallback != null) {
                                controllerStateCallback.onVisibilityChange(true);
                            }
                        } else if (getControllerHideOnTouch()) {
                            //LLog.d(TAG, "hideController");
                            hideController();
                            if (controllerStateCallback != null) {
                                controllerStateCallback.onVisibilityChange(false);
                            }
                        }
                    }
                    tapStartTimeMs = 0;
                    if (onTouchEvent != null) {
                        LLog.d(TAG, "onTouchEvent");
                        onTouchEvent.onClick();
                    }
                }
        }*/

        if (UZData.getInstance().isUseWithVDHView()) {
            return false;
        } else {
            mDetector.onTouchEvent(ev);
            return true;
        }

        //mDetector.onTouchEvent(ev);
        //return true;
    }

    public interface OnTouchEvent {
        public void onSingleTapConfirmed(float x, float y);

        public void onLongPress(float x, float y);

        public void onDoubleTap(float x, float y);

        public void onSwipeRight();

        public void onSwipeLeft();

        public void onSwipeBottom();

        public void onSwipeTop();
    }

    private class UizaGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent event) {
            //LLog.d(TAG, "onDown");
            // don't return false here or else none of the other
            // gestures will work
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            LLog.d(TAG, "onSingleTapConfirmed " + e.getX() + " - " + e.getY());
            if (!controllerVisible) {
                showController();
            } else if (getControllerHideOnTouch()) {
                hideController();
            }
            if (onTouchEvent != null) {
                onTouchEvent.onSingleTapConfirmed(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            LLog.d(TAG, "onLongPress " + e.getX() + " - " + e.getY());
            if (onTouchEvent != null) {
                onTouchEvent.onLongPress(e.getX(), e.getY());
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            LLog.d(TAG, "onDoubleTap " + e.getX() + " - " + e.getY());
            if (onTouchEvent != null) {
                onTouchEvent.onDoubleTap(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            LLog.d(TAG, "onScroll");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            LLog.d(TAG, "onFling");
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            //LLog.d(TAG, "onSwipeRight");
                            if (onTouchEvent != null) {
                                onTouchEvent.onSwipeRight();
                            }
                        } else {
                            //LLog.d(TAG, "onSwipeLeft");
                            if (onTouchEvent != null) {
                                onTouchEvent.onSwipeLeft();
                            }
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            //LLog.d(TAG, "onSwipeBottom");
                            if (onTouchEvent != null) {
                                onTouchEvent.onSwipeBottom();
                            }
                        } else {
                            //LLog.d(TAG, "onSwipeTop");
                            if (onTouchEvent != null) {
                                onTouchEvent.onSwipeTop();
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return true;
        }
    }

    public PlayerControlView getPlayerControlView() {
        //PlayerControlView playerControlView = (PlayerControlView) findViewById(R.id.exo_controller);
        //return playerControlView;
        for (int i = 0; i < this.getChildCount(); i++) {
            //LLog.d(TAG, "getPlayerControlView " + i);
            if (this.getChildAt(i) instanceof PlayerControlView) {
                return (PlayerControlView) getChildAt(i);
            }
        }
        return null;
    }

    public View[] getAllChild() {
        PlayerControlView playerControlView = getPlayerControlView();
        if (playerControlView == null) {
            //LLog.e(TAG, "playerControlView == null");
            return null;
        }
        //LLog.d(TAG, "playerControlView != null");
        //LLog.d(TAG, "playerControlView.getChildCount() " + playerControlView.getChildCount());
        List<View> viewList = LUIUtil.getAllChildren(playerControlView);
        return viewList.toArray(new View[viewList.size()]);
    }
}