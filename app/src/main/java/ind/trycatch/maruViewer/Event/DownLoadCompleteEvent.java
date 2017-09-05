package ind.trycatch.maruViewer.Event;

/**
 * Created by jack on 2017. 1. 25..
 */

public class DownLoadCompleteEvent {
    private boolean succeed;

    public DownLoadCompleteEvent(boolean succeed) {
        this.succeed = succeed;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }
}
