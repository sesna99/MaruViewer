package ind.simsim.maruViewer.Event;

import java.util.ArrayList;

import ind.simsim.maruViewer.Model.ComicsData;

/**
 * Created by jack on 2017. 2. 2..
 */

public class ComicsDownLoadEvent {
    ArrayList<ArrayList<ComicsData>> data;

    public ComicsDownLoadEvent(ArrayList<ArrayList<ComicsData>> data) {
        this.data = data;
    }

    public ArrayList<ArrayList<ComicsData>> getData() {
        return data;
    }

    public void setData(ArrayList<ArrayList<ComicsData>> data) {
        this.data = data;
    }
}
