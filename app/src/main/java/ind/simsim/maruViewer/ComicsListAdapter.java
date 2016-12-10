package ind.simsim.maruViewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by admin on 2016-02-15.
 */
public class ComicsListAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    int layout;
    ArrayList<ComicsData> comicsData;

    public ComicsListAdapter(Context context, int layout, ArrayList<ComicsData> comicsData) {
        this.mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
        this.comicsData = comicsData;
    }

    @Override
    public int getCount() {
        return comicsData.size();
    }

    @Override
    public Object getItem(int position) {
        return comicsData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addComicsData(ArrayList<ComicsData> comicsData){
        this.comicsData = comicsData;
    }

    public void refresh(){
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
            holder = new ViewHolder();
            holder.icon = (ImageView)convertView.findViewById(R.id.icon);
            holder.title = (TextView)convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        Glide.with(mContext).load(comicsData.get(position).getImage()).into(holder.icon);
        holder.title.setText(comicsData.get(position).getTitle());

        return convertView;
    }

    class ViewHolder{
        ImageView icon;
        TextView title;
    }
}

