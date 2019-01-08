package uizacoresdk.util;

import vn.uiza.core.utilities.LLog;
import vn.uiza.restapi.uiza.model.v3.metadata.getdetailofmetadata.Data;

/**
 * Created by loitp on 5/18/2018.
 */

public class UZInput {
    private final String TAG = getClass().getSimpleName();
    private String urlThumnailsPreviewSeekbar = "";
    private Data data;

    public String getUrlThumnailsPreviewSeekbar() {
        return urlThumnailsPreviewSeekbar;
    }

    public void setUrlThumnailsPreviewSeekbar(String urlThumnailsPreviewSeekbar) {
        this.urlThumnailsPreviewSeekbar = urlThumnailsPreviewSeekbar;
    }

    public Data getData() {
        if (data == null) {
            LLog.d(TAG, "getData data == null");
        }
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public boolean isLivestream() {
        if (data == null || data.getLastFeedId() == null || data.getLastFeedId().isEmpty()) {
            return false;
        }
        return true;
    }
}
