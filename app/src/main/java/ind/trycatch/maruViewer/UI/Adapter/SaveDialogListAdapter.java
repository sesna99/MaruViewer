package ind.trycatch.maruViewer.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.Model.ComicsModel;

/**
 * Created by jack on 2016. 12. 8..
 */

public class SaveDialogListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<ComicsModel> comicsModels;
    private boolean[] checked;
    private ViewHolder viewHolder;

    public SaveDialogListAdapter(Context context, ArrayList<ComicsModel> comicsModels) {
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.comicsModels = comicsModels;
        comicsModels.add(0, new ComicsModel("전체 선택", "", ""));
        checked = new boolean[comicsModels.size()];
        for(int i = 0; i < checked.length; i++)
            checked[i] = false;
    }

    @Override
    public int getCount() {
        return comicsModels.size();
    }

    @Override
    public Object getItem(int i) {
        return comicsModels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = inflater.inflate(R.layout.save_dialog_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.episode.setText(comicsModels.get(i).getTitle());
        viewHolder.checkBox.setChecked(checked[i]);

        return view;
    }

    public boolean getChecked(int position){
        return checked[position];
    }

    public ArrayList<ComicsModel> getCheckedData(){
        ArrayList<ComicsModel> data = new ArrayList<>();
        for(int i = 1; i < getCount(); i++)
            if(getChecked(i))
                data.add(comicsModels.get(i));

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
        @BindView(R.id.checkbox)
        CheckBox checkBox;

        @BindView(R.id.episode)
        TextView episode;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }
}
