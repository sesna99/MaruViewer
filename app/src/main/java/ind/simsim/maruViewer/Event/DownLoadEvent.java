package ind.simsim.maruViewer.Event;

/**
 * Created by jack on 2017. 1. 25..
 */

public class DownLoadEvent {
    private boolean succeed;

    public DownLoadEvent(boolean succeed) {
        this.succeed = succeed;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }
}
