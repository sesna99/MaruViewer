package ind.trycatch.maruViewer.Event;

/**
 * Created by trycatch on 2017. 9. 17..
 */

public class DivEvent {
    private boolean succeed;

    public DivEvent(boolean succeed) {
        this.succeed = succeed;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }
}
