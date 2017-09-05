package ind.trycatch.maruViewer.Event;

/**
 * Created by jack on 2017. 1. 26..
 */

public class LatelyEvent {
    private boolean succeed;

    public LatelyEvent(boolean succeed) {
        this.succeed = succeed;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }
}
