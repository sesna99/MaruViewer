package ind.trycatch.maruViewer.Event;

/**
 * Created by jack on 2017. 1. 25..
 */

public class UpdateEvent {
    private boolean isUpdate;

    public UpdateEvent(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }
}
