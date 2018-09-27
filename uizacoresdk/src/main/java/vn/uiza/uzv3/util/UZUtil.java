package vn.uiza.uzv3.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;

import loitp.core.R;
import vn.uiza.chromecast.Casty;
import vn.uiza.core.base.BaseActivity;
import vn.uiza.core.common.Constants;
import vn.uiza.core.utilities.LDeviceUtil;
import vn.uiza.core.utilities.LLog;
import vn.uiza.core.utilities.LScreenUtil;
import vn.uiza.restapi.restclient.RestClientTracking;
import vn.uiza.restapi.restclient.RestClientV2;
import vn.uiza.restapi.restclient.UZRestClient;
import vn.uiza.restapi.uiza.UZService;
import vn.uiza.restapi.uiza.model.v2.auth.Auth;
import vn.uiza.restapi.uiza.model.v2.listallentity.Subtitle;
import vn.uiza.restapi.uiza.model.v3.UizaWorkspaceInfo;
import vn.uiza.restapi.uiza.model.v3.authentication.gettoken.ResultGetToken;
import vn.uiza.restapi.uiza.model.v3.livestreaming.retrievealive.ResultRetrieveALive;
import vn.uiza.restapi.uiza.model.v3.metadata.getdetailofmetadata.Data;
import vn.uiza.restapi.uiza.model.v3.videoondeman.retrieveanentity.ResultRetrieveAnEntity;
import vn.uiza.rxandroid.ApiSubscriber;
import vn.uiza.utils.util.Utils;
import vn.uiza.uzv1.view.floatview.FUZVideoServiceV1;
import vn.uiza.uzv3.view.floatview.FUZVideoService;
import vn.uiza.uzv3.view.rl.video.UZVideo;
import vn.uiza.views.LToast;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by loitp on 4/11/2018.
 */

public class UZUtil {
    private final static String TAG = UZUtil.class.getSimpleName();

    public static void setUIFullScreenIcon(Context context, ImageButton imageButton, boolean isFullScreen) {
        if (imageButton == null) {
            return;
        }
        if (isFullScreen) {
            imageButton.setImageResource(loitp.core.R.drawable.baseline_fullscreen_exit_white_48);
        } else {
            imageButton.setImageResource(R.drawable.baseline_fullscreen_white_48);
        }
    }

    /*
     ** isDisplayPortrait true -> portrait display như YUP
     ** isDisplayPortrait false thi se bop ve 16/9 video
     */
    public static void resizeLayout(ViewGroup viewGroup, RelativeLayout llMid, ImageView ivVideoCover, boolean isDisplayPortrait) {
        //LLog.d(TAG, "resizeLayout");
        int widthScreen = 0;
        int heightScreen = 0;
        boolean isFullScreen = LScreenUtil.isFullScreen(viewGroup.getContext());
        if (isFullScreen) {
            widthScreen = LScreenUtil.getScreenHeightIncludeNavigationBar(viewGroup.getContext());
            heightScreen = LScreenUtil.getScreenHeight();
        } else {
            widthScreen = LScreenUtil.getScreenWidth();
            if (isDisplayPortrait) {
                heightScreen = LScreenUtil.getScreenHeight();
            } else {
                heightScreen = widthScreen * 9 / 16;
            }
        }
        //LLog.d(TAG, "resizeLayout isFullScreen " + isFullScreen + " -> " + widthScreen + "x" + heightScreen);
        viewGroup.getLayoutParams().width = widthScreen;
        viewGroup.getLayoutParams().height = heightScreen;
        viewGroup.requestLayout();

        //set size of parent view group of viewGroup
        RelativeLayout parentViewGroup = (RelativeLayout) viewGroup.getParent();
        parentViewGroup.getLayoutParams().width = widthScreen;
        parentViewGroup.getLayoutParams().height = heightScreen;
        parentViewGroup.requestLayout();

        if (ivVideoCover != null) {
            ivVideoCover.getLayoutParams().width = widthScreen;
            ivVideoCover.getLayoutParams().height = heightScreen;
            ivVideoCover.requestLayout();
        }

        //edit size of seekbar volume and brightness
        if (llMid != null) {
            if (isFullScreen) {
                llMid.getLayoutParams().height = (int) (heightScreen / 1.75);

            } else {
                llMid.getLayoutParams().height = (int) (heightScreen / 1.95);
            }
            llMid.requestLayout();
        }

        //edit size of imageview thumnail
        FrameLayout flImgThumnailPreviewSeekbar = viewGroup.findViewById(R.id.preview_frame_layout);
        //LLog.d(TAG, flImgThumnailPreviewSeekbar == null ? "resizeLayout imgThumnailPreviewSeekbar null" : "resizeLayout imgThumnailPreviewSeekbar !null");
        if (flImgThumnailPreviewSeekbar != null) {
            if (isFullScreen) {
                flImgThumnailPreviewSeekbar.getLayoutParams().width = widthScreen / 4;
                flImgThumnailPreviewSeekbar.getLayoutParams().height = widthScreen / 4 * 9 / 16;
            } else {
                flImgThumnailPreviewSeekbar.getLayoutParams().width = widthScreen / 5;
                flImgThumnailPreviewSeekbar.getLayoutParams().height = widthScreen / 5 * 9 / 16;
            }
            //LLog.d(TAG, "resizeLayout: " + flImgThumnailPreviewSeekbar.getWidth() + " x " + flImgThumnailPreviewSeekbar.getHeight());
            flImgThumnailPreviewSeekbar.requestLayout();
        }
    }

    //return button video in debug layout
    public static View getBtVideo(LinearLayout debugRootView) {
        if (debugRootView == null) {
            return null;
        }
        for (int i = 0; i < debugRootView.getChildCount(); i++) {
            View childView = debugRootView.getChildAt(i);
            if (childView instanceof Button) {
                if (((Button) childView).getText().toString().equalsIgnoreCase(debugRootView.getContext().getString(R.string.video))) {
                    return childView;
                }
            }
        }
        return null;
    }

    //return button audio in debug layout
    public static View getBtAudio(LinearLayout debugRootView) {
        if (debugRootView == null) {
            return null;
        }
        for (int i = 0; i < debugRootView.getChildCount(); i++) {
            View childView = debugRootView.getChildAt(i);
            if (childView instanceof Button) {
                if (((Button) childView).getText().toString().equalsIgnoreCase(debugRootView.getContext().getString(R.string.audio))) {
                    return childView;
                }
            }
        }
        return null;
    }

    //return button text in debug layout
    public static View getBtText(LinearLayout debugRootView) {
        if (debugRootView == null) {
            return null;
        }
        for (int i = 0; i < debugRootView.getChildCount(); i++) {
            View childView = debugRootView.getChildAt(i);
            if (childView instanceof Button) {
                if (((Button) childView).getText().toString().equalsIgnoreCase(debugRootView.getContext().getString(R.string.text))) {
                    return childView;
                }
            }
        }
        return null;
    }

    public static List<Subtitle> createDummySubtitle(Gson gson) {
        String json = "[\n" +
                "                {\n" +
                "                    \"id\": \"18414566-c0c8-4a51-9d60-03f825bb64a9\",\n" +
                "                    \"name\": \"\",\n" +
                "                    \"type\": \"subtitle\",\n" +
                "                    \"url\": \"//dev-static.uiza.io/subtitle_56a4f990-17e6-473c-8434-ef6c7e40bba1_en_1522812430080.vtt\",\n" +
                "                    \"mine\": \"vtt\",\n" +
                "                    \"language\": \"en\",\n" +
                "                    \"isDefault\": \"0\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\": \"271787a0-5d23-4a35-a10a-5c43fdcb71a8\",\n" +
                "                    \"name\": \"\",\n" +
                "                    \"type\": \"subtitle\",\n" +
                "                    \"url\": \"//dev-static.uiza.io/subtitle_56a4f990-17e6-473c-8434-ef6c7e40bba1_vi_1522812445904.vtt\",\n" +
                "                    \"mine\": \"vtt\",\n" +
                "                    \"language\": \"vi\",\n" +
                "                    \"isDefault\": \"0\"\n" +
                "                }\n" +
                "            ]";
        Subtitle[] subtitles = gson.fromJson(json, new TypeToken<Subtitle[]>() {
        }.getType());
        //LLog.d(TAG, "createDummySubtitle subtitles " + gson.toJson(subtitles));
        List subtitleList = Arrays.asList(subtitles);
        //LLog.d(TAG, "createDummySubtitle subtitleList " + gson.toJson(subtitleList));
        return subtitleList;
    }

    public static void showUizaDialog(Activity activity, Dialog dialog) {
        if (activity == null || dialog == null) {
            return;
        }
        boolean isFullScreen = LScreenUtil.isFullScreen(activity);
        if (isFullScreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                dialog.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            } else {
                //TODO cần làm ở sdk thấp, thanh navigation ko chịu ẩn
            }
        }
        dialog.show();
        try {
            dialog.getWindow().getAttributes().windowAnimations = R.style.uiza_dialog_animation;
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_uiza);
            //set dialog position
            WindowManager.LayoutParams wlp = dialog.getWindow().getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            //wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.dimAmount = 0.65f;
            dialog.getWindow().setAttributes(wlp);

            int width = 0;
            int height = 0;
            if (isFullScreen) {
                width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 1.0);
                height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.5);
            } else {
                width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 1.0);
                height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.3);
            }
            dialog.getWindow().setLayout(width, height);
        } catch (Exception e) {
            //do nothing
        }
        if (isFullScreen) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    public static void setTextDuration(TextView textView, String duration) {
        //LLog.d(TAG, "duration: " + duration);
        if (textView == null || duration == null || duration.isEmpty()) {
            return;
        }
        try {
            int min = (int) Double.parseDouble(duration) + 1;
            String minutes = Integer.toString(min % 60);
            minutes = minutes.length() == 1 ? "0" + minutes : minutes;
            textView.setText((min / 60) + ":" + minutes);
        } catch (Exception e) {
            LLog.e(TAG, "Error setTextDuration " + e.toString());
            textView.setText(" - ");
        }
    }

    /*
     **Hàm này set size của MediaRouteButton giống với size default của ImageButtonWithSize
     */
    /*public static void setUICastButton(MediaRouteButton uiCastButton) {
        if (uiCastButton == null) {
            return;
        }
        boolean isTablet = LDeviceUtil.isTablet(uiCastButton.getContext());
        int ratioPort;
        int ratioLand;
        if (isTablet) {
            ratioLand = Constants.RATIO_LAND_TABLET;
            ratioPort = Constants.RATIO_PORTRAIT_TABLET;
        } else {
            ratioLand = Constants.RATIO_LAND_MOBILE;
            ratioPort = Constants.RATIO_PORTRAIT_MOBILE;
        }
        int size;
        if (LScreenUtil.isFullScreen(uiCastButton.getContext())) {
            int screenWLandscape = LScreenUtil.getScreenHeightIncludeNavigationBar(uiCastButton.getContext());
            size = screenWLandscape / (ratioLand + 1);
        } else {
            int screenWPortrait = LScreenUtil.getScreenWidth();
            size = screenWPortrait / (ratioPort + 1);
        }
        LLog.d(TAG, "setUICastButton size: " + size + ", ratioPort: " + ratioPort + ", ratioLand: " + ratioLand);
        uiCastButton.getLayoutParams().width = size;
        uiCastButton.getLayoutParams().height = size;
        uiCastButton.requestLayout();
    }*/

    //return true if app is in foreground
    public static boolean isAppInForeground(Context context) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
            return foregroundTaskPackageName.toLowerCase().equals(context.getPackageName().toLowerCase());
        } else {
            ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(appProcessInfo);
            if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                return true;
            }
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            // App is foreground, but screen is locked, so show notification
            return km.inKeyguardRestrictedInputMode();
        }
    }

    public static boolean checkServiceRunning(Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            //LLog.d(TAG, "checkServiceRunning: " + FUZVideoService.class.getName());
            //LLog.d(TAG, "checkServiceRunning: " + service.service.getClassName());
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void setupRestClientV2(Activity activity) {
        if (RestClientV2.getRetrofit() == null && RestClientTracking.getRetrofit() == null) {
            String currentApi = getApiEndPoint(activity);
            if (currentApi == null || currentApi.isEmpty()) {
                LLog.e(TAG, "setupRestClientV2 trackUiza currentApi == null || currentApi.isEmpty()");
                return;
            }
            String token = getToken(activity);
            if (token == null || token.isEmpty()) {
                LLog.e(TAG, "setupRestClientV2 trackUiza token==null||token.isEmpty()");
                return;
            }
            String currentTrackApi = getApiTrackEndPoint(activity);
            if (currentTrackApi == null || currentTrackApi.isEmpty()) {
                LLog.e(TAG, "setupRestClientV2 currentTrackApi == null || currentTrackApi.isEmpty()");
                return;
            }

            RestClientV2.init(currentApi);
            RestClientV2.addAuthorization(token);
            RestClientTracking.init(currentTrackApi);

            if (Constants.IS_DEBUG) {
                LToast.show(activity, "setupRestClientV2 with currentApi: " + currentApi + "\ntoken:" + token + "\ncurrentTrackApi: " + currentTrackApi);
            }
        }
    }

    //stop service pip FUZVideoServiceV1
    public static void stopServicePiPIfRunning(Activity activity) {
        if (activity == null) {
            return;
        }
        //LLog.d(TAG, "stopServicePiPIfRunning");
        boolean isSvPipRunning = UZUtil.checkServiceRunning(activity, FUZVideoServiceV1.class.getName());
        //LLog.d(TAG, "isSvPipRunning " + isSvPipRunning);
        if (isSvPipRunning) {
            //stop service if running
            Intent intent = new Intent(activity, FUZVideoServiceV1.class);
            activity.stopService(intent);
        }
    }

    //stop service pip FUZVideoService
    public static void stopServicePiPIfRunningV3(Activity activity) {
        if (activity == null) {
            return;
        }
        //LLog.d(TAG, "stopServicePiPIfRunningV3");
        boolean isSvPipRunning = UZUtil.checkServiceRunning(activity, FUZVideoService.class.getName());
        //LLog.d(TAG, "isSvPipRunning " + isSvPipRunning);
        if (isSvPipRunning) {
            //stop service if running
            Intent intent = new Intent(activity, FUZVideoService.class);
            activity.stopService(intent);
        }
    }

    //=============================================================================START FOR UIZA V3
    /*public static void initUizaWorkspace(Context context, UizaWorkspaceInfo uizaWorkspaceInfo) {
        if (uizaWorkspaceInfo == null || uizaWorkspaceInfo.getUsername() == null || uizaWorkspaceInfo.getPassword() == null || uizaWorkspaceInfo.getUrlApi() == null) {
            throw new NullPointerException("UizaWorkspaceInfo cannot be null or empty!");
        }
        setUizaWorkspaceInfo(context, uizaWorkspaceInfo);
        UZRestClient.init(Constants.PREFIXS + uizaWorkspaceInfo.getUrlApi());
    }*/

    /*public static UizaWorkspaceInfo getUizaWorkspace(Context context) {
        if (context == null) {
            return null;
        }
        return getUizaWorkspaceInfo(context);
    }*/

    /*public static void setResultGetToken(Context context, ResultGetToken resultGetToken) {
        setResultGetToken(context, resultGetToken);
    }

    public static ResultGetToken getResultGetToken(Context context) {
        return getResultGetToken(context);
    }*/

    /*public static String getToken(Context context) {
        if (context == null) {
            return null;
        }
        ResultGetToken resultGetToken = getResultGetToken(context);
        if (resultGetToken == null) {
            return null;
        }
        return resultGetToken.getData().getToken();
    }*/

    /*public static String getAppId(Context context) {
        if (context == null) {
            return null;
        }
        ResultGetToken resultGetToken = getResultGetToken(context);
        if (resultGetToken == null) {
            return null;
        }
        return resultGetToken.getData().getAppId();
    }*/

    public interface Callback {
        public void onSuccess(Data data);

        public void onError(Throwable e);
    }

    public static void getDetailEntity(final BaseActivity activity, final String entityId, final Callback callback) {
        UZService service = UZRestClient.createService(UZService.class);
        activity.subscribe(service.retrieveAnEntity(entityId), new ApiSubscriber<ResultRetrieveAnEntity>() {
            @Override
            public void onSuccess(ResultRetrieveAnEntity result) {
                if (result == null || result.getData() == null || result.getData().getId() == null || result.getData().getId().isEmpty()) {
                    getDataFromEntityIdLIVE(activity, entityId, callback);
                } else {
                    if (callback != null) {
                        Data d = result.getData();
                        callback.onSuccess(d);
                    }
                }
            }

            @Override
            public void onFail(Throwable e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public static void getDataFromEntityIdLIVE(final BaseActivity activity, String entityId, final Callback callback) {
        UZService service = UZRestClient.createService(UZService.class);
        activity.subscribe(service.retrieveALiveEvent(entityId), new ApiSubscriber<ResultRetrieveALive>() {
            @Override
            public void onSuccess(ResultRetrieveALive result) {
                if (result == null || result.getData() == null || result.getData().getId() == null || result.getData().getId().isEmpty()) {
                    if (callback != null) {
                        callback.onError(new Exception(activity.getString(R.string.err_unknow)));
                    }
                } else {
                    if (callback != null) {
                        Data d = result.getData();
                        callback.onSuccess(d);
                    }
                }
            }

            @Override
            public void onFail(Throwable e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public static void initEntity(Activity activity, UZVideo UZVideo, String entityId) {
        if (activity == null) {
            throw new NullPointerException("Activity cannot be null");
        }
        if (UZVideo == null) {
            throw new NullPointerException("UZVideo cannot be null");
        }
        if (UZUtil.getClickedPip(activity)) {
            LLog.d(TAG, "called from pip enter fullscreen");
            UZUtil.play(UZVideo, null);
        } else {
            //check if play entity
            UZUtil.stopServicePiPIfRunningV3(activity);
            if (entityId != null) {
                LLog.d(TAG, "initEntity entityId: " + entityId);
                //setEntityId(activity, entityId);
                UZUtil.play(UZVideo, entityId);
            }
        }
    }

    public static void initPlaylistFolder(Activity activity, UZVideo UZVideo, String metadataId) {
        if (activity == null) {
            throw new NullPointerException("Activity cannot be null");
        }
        if (UZVideo == null) {
            throw new NullPointerException("UZVideo cannot be null");
        }
        if (UZUtil.getClickedPip(activity)) {
            LLog.d(TAG, "called from pip enter fullscreen");
            if (UZData.getInstance().isPlayWithPlaylistFolder()) {
                LLog.d(TAG, "called from pip enter fullscreen -> playlist folder");
                playPlaylist(UZVideo, null);
            }
        } else {
            //check if play entity
            UZUtil.stopServicePiPIfRunningV3(activity);
            //setMetadataId(activity, metadataId);
            playPlaylist(UZVideo, metadataId);
        }
    }

    private static void play(final UZVideo UZVideo, final String entityId) {
        /*if (UZData.getInstance().isSettingPlayer()) {
            LLog.d(TAG, "isSettingPlayer");
            return;
        }*/
        UZData.getInstance().setSettingPlayer(false);
        UZVideo.post(new Runnable() {
            @Override
            public void run() {
                UZVideo.init(entityId);
            }
        });
    }

    private static void playPlaylist(final UZVideo UZVideo, final String metadataId) {
        /*if (UZData.getInstance().isSettingPlayer()) {
            LLog.d(TAG, "isSettingPlayer");
            return;
        }*/
        UZData.getInstance().setSettingPlayer(false);
        UZVideo.post(new Runnable() {
            @Override
            public void run() {
                UZVideo.initPlaylistFolder(metadataId);
            }
        });
    }

    public static void initWorkspace(Context context, String domainApi, String token, String appId, int env, int currentPlayerId) {
        if (context == null) {
            throw new NullPointerException("Error: Context cannot be null");
        }
        if (domainApi == null || domainApi.isEmpty()) {
            throw new NullPointerException("Domain api cannot be null or empty");
        }
        if (token == null || token.isEmpty()) {
            throw new NullPointerException("Token cannot be null or empty");
        }
        if (appId == null || appId.isEmpty()) {
            throw new NullPointerException("App id be null or empty");
        }
        Utils.init(context.getApplicationContext());
        UZUtil.setCurrentPlayerId(currentPlayerId);
        //UZData.getInstance().setCurrentPlayerId(currentPlayerId);
        UZData.getInstance().initSDK(domainApi, token, appId, env);
    }

    public static void initWorkspace(Context context, String domainApi, String token, String appId, int currentPlayerId) {
        initWorkspace(context, domainApi, token, appId, Constants.ENVIRONMENT_PROD, currentPlayerId);
    }

    public static void initWorkspace(Context context, String domainApi, String token, String appId) {
        initWorkspace(context, domainApi, token, appId, Constants.ENVIRONMENT_PROD, R.layout.uz_player_skin_1);
    }

    public static void setCasty(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("Error: Activity cannot be null");
        }
        if (LDeviceUtil.isTV(activity)) {
            return;
        }
        UZData.getInstance().setCasty(Casty.create(activity));
    }

    public static void setCurrentPlayerId(int resLayoutMain) {
        UZData.getInstance().setCurrentPlayerId(resLayoutMain);
    }

    public static void updateUIFocusChange(View view, boolean isFocus) {
        updateUIFocusChange(view, isFocus, R.drawable.bkg_has_focus, R.drawable.bkg_no_focus);
    }

    public static void updateUIFocusChange(View view, boolean isFocus, int resHasFocus, int resNoFocus) {
        if (view == null) {
            return;
        }
        if (isFocus) {
            view.setBackgroundResource(resHasFocus);
        } else {
            view.setBackgroundResource(resNoFocus);
        }
    }

    //=============================================================================END FOR UIZA V3

    //=============================================================================START PREF
    private final static String PREFERENCES_FILE_NAME = "loitp";
    private final static String CHECK_APP_READY = "CHECK_APP_READY";
    private final static String PRE_LOAD = "PRE_LOAD";
    private final static String SLIDE_UIZA_VIDEO_ENABLED = "SLIDE_UIZA_VIDEO_ENABLED";
    private final static String INDEX = "INDEX";
    private final static String AUTH = "AUTH";
    public final static String API_END_POINT = "API_END_POINT";
    private final static String API_TRACK_END_POINT = "API_TRACK_END_POINT";
    private final static String TOKEN = "TOKEN";
    private final static String CLICKED_PIP = "CLICKED_PIP";
    private final static String ACITIVITY_CAN_SLIDE_IS_RUNNING = "ACITIVITY_CAN_SLIDE_IS_RUNNING";
    private final static String CLASS_NAME_OF_PLAYER = "CLASS_NAME_OF_PLAYER";
    private final static String PREF_CAMERA_ID = "pref_camera_id";
    private final static String PREF_STATE_FILTER = "state_filter";

    //for api v3
    private final static String V3UIZAWORKSPACEINFO = "V3UIZAWORKSPACEINFO";
    private final static String V3UIZATOKEN = "V3UIZATOKEN";
    private final static String V3DATA = "V3DATA";
    //private final static String ENTITY_ID = "ENTITY_ID";
    //private final static String METADATA_ID = "METADATA_ID";
    //end for api v3

    //object
    public static UizaWorkspaceInfo getUizaWorkspaceInfo(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return new Gson().fromJson(pref.getString(V3UIZAWORKSPACEINFO, ""), UizaWorkspaceInfo.class);
    }

    public static void setUizaWorkspaceInfo(Context context, UizaWorkspaceInfo uizaWorkspaceInfo) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(V3UIZAWORKSPACEINFO, new Gson().toJson(uizaWorkspaceInfo));
        editor.apply();
    }

    public static ResultGetToken getResultGetToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return new Gson().fromJson(pref.getString(V3UIZATOKEN, ""), ResultGetToken.class);
    }

    public static void setResultGetToken(Context context, ResultGetToken resultGetToken) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(V3UIZATOKEN, new Gson().toJson(resultGetToken));
        editor.apply();
    }

    /////////////////////////////////STRING
    public static String getApiEndPoint(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return pref.getString(API_END_POINT, null);
    }

    public static void setApiEndPoint(Context context, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(API_END_POINT, value);
        editor.apply();
    }

    public static String getApiTrackEndPoint(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return pref.getString(API_TRACK_END_POINT, null);
    }

    public static void setApiTrackEndPoint(Context context, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(API_TRACK_END_POINT, value);
        editor.apply();
    }

    public static String getToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return pref.getString(TOKEN, null);
    }

    public static void setToken(Context context, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(TOKEN, value);
        editor.apply();
    }

    public static String getClassNameOfPlayer(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return pref.getString(CLASS_NAME_OF_PLAYER, null);
    }

    public static void setClassNameOfPlayer(Context context, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(CLASS_NAME_OF_PLAYER, value);
        editor.apply();
    }

    /*public static String getEntityId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return pref.getString(ENTITY_ID, null);
    }

    public static void setEntityId(Context context, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(ENTITY_ID, value);
        editor.apply();
    }

    public static String getMetadataId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return pref.getString(METADATA_ID, null);
    }

    public static void setMetadataId(Context context, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(METADATA_ID, value);
        editor.apply();
    }*/
    /////////////////////////////////BOOLEAN

    public static Boolean getCheckAppReady(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return prefs.getBoolean(CHECK_APP_READY, false);
    }

    public static void setCheckAppReady(Context context, Boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putBoolean(CHECK_APP_READY, value);
        editor.apply();
    }

    public static Boolean getPreLoad(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return prefs.getBoolean(PRE_LOAD, false);
    }

    public static void setPreLoad(Context context, Boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putBoolean(PRE_LOAD, value);
        editor.apply();
    }

    public static Boolean getSlideUizaVideoEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return prefs.getBoolean(SLIDE_UIZA_VIDEO_ENABLED, false);
    }

    public static void setSlideUizaVideoEnabled(Context context, Boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putBoolean(SLIDE_UIZA_VIDEO_ENABLED, value);
        editor.apply();
    }

    public static Boolean getClickedPip(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return prefs.getBoolean(CLICKED_PIP, false);
    }

    public static void setClickedPip(Context context, Boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putBoolean(CLICKED_PIP, value);
        editor.apply();
    }

    public static Boolean getAcitivityCanSlideIsRunning(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return prefs.getBoolean(ACITIVITY_CAN_SLIDE_IS_RUNNING, false);
    }

    public static void setAcitivityCanSlideIsRunning(Context context, Boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putBoolean(ACITIVITY_CAN_SLIDE_IS_RUNNING, value);
        editor.apply();
    }

    /////////////////////////////////INT
    public static int getIndex(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return prefs.getInt(INDEX, Constants.NOT_FOUND);
    }

    public static void setIndex(Context context, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putInt(INDEX, value);
        editor.apply();
    }

    //Object
    public static Auth getAuth(Context context, Gson gson) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        String json = pref.getString(AUTH, null);
        return gson.fromJson(json, Auth.class);
    }

    public static void setAuth(Context context, Auth auth, Gson gson) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(AUTH, gson.toJson(auth));
        editor.apply();
    }

    /*public static Data getData(Context context, Gson gson) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        String json = pref.getString(V3DATA, null);
        return gson.fromJson(json, Data.class);
    }

    public static void setData(Context context, Data data, Gson gson) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0).edit();
        editor.putString(V3DATA, gson.toJson(data));
        editor.apply();
    }*/

    public static String getStoredCameraId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return pref.getString(PREF_CAMERA_ID, "");
    }

    public static void storeCameraId(Context context, String id) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        pref.edit().putString(PREF_CAMERA_ID, id).apply();
    }

    public static boolean getStoredFilterState(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return pref.getBoolean(PREF_STATE_FILTER, false);
    }

    public static void storeFilterState(Context context, boolean on) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        pref.edit().putBoolean(PREF_STATE_FILTER, on).apply();
    }
    //=============================================================================END PREF
}
