package ind.trycatch.maruViewer.Event;

import java.util.ArrayList;

import ind.trycatch.maruViewer.Model.ComicsModel;

/**
 * Created by jack on 2017. 2. 2..
 */

public class ComicsDownLoadEvent {
    ArrayList<ArrayList<ComicsModel>> data;

    public ComicsDownLoadEvent(ArrayList<ArrayList<ComicsModel>> data) {
        this.data = data;
    }

    public ArrayList<ArrayList<ComicsModel>> getData() {
        return data;
    }

    public void setData(ArrayList<ArrayList<ComicsModel>> data) {
        this.data = data;
    }
}
