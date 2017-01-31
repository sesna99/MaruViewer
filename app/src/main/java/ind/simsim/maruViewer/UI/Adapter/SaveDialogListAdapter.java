package ind.simsim.maruViewer.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Model.ComicsData;

/**
 * Created by jack on 2016. 12. 8..
 */

public class SaveDialogListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<ComicsData> comicsDatas;
    private boolean[] checked;
    private ViewHolder viewHolder;

    public SaveDialogListAdapter(Context context, ArrayList<ComicsData> comicsDatas) {
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.comicsDatas = comicsDatas;
        comicsDatas.add(0, new ComicsData("전체 선택", "", ""));
        checked = new boolean[comicsDatas.size()];
        for(int i = 0; i < checked.length; i++)
            checked[i] = false;
    }

    @Override
    public int getCount() {
        return comicsDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return comicsDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = inflater.inflate(R.layout.save_dialog_item, null);
            viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            viewHolder.episode = (TextView) view.findViewById(R.id.episode);
            view.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.episode.setText(comicsDatas.get(i).getTitle());
        viewHolder.checkBox.setChecked(checked[i]);

        return view;
    }

    public boolean getChecked(int position){
        return checked[position];
    }

    public ArrayList<ComicsData> getCheckedData(){
        ArrayList<ComicsData> data = new ArrayList<>();
        for(int i = 1; i < getCount(); i++)
            if(getChecked(i))
                data.add(comicsDatas.get(i));

        return data;
    }

    public void setChecked(int position){
        if(position != 0)
            checked[0] = false;
        checked[position] = !checked[position];
    }

    public void setAllChecked(boolean isChecked){
        for(int i = 1; i < getCount(); i++)
            checked[i] = isChecked;
    }

    class ViewHolder {
        CheckBox checkBox;
        TextView episode;
    }
}
