package ind.simsim.maruViewer.UI.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Model.ComicsData;
import ind.simsim.maruViewer.Service.PreferencesManager;

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

    public void setComicsData(ArrayList<ComicsData> comicsData){
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

        if(comicsData.get(position).getImage().contains(PreferencesManager.getInstance(mContext).getDownLoadDirectory()))
            Glide.with(mContext).load(Uri.fromFile(new File(comicsData.get(position).getImage()))).into(holder.icon);
        else if(comicsData.get(position).getImage().contains("storage"))
            Glide.with(mContext).load("http://wasabisyrup.com" + comicsData.get(position).getImage()).into(holder.icon);
        else
            Glide.with(mContext).load(comicsData.get(position).getImage()).into(holder.icon);
        holder.title.setText(comicsData.get(position).getTitle());
        holder.title.setSelected(true);

        return convertView;
    }

    class ViewHolder{
        ImageView icon;
        TextView title;
    }
}

