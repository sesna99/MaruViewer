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
    ArrayList<String> title, image;

    public ComicsListAdapter(Context context, int layout, ArrayList<String> title, ArrayList<String> image) {
        this.mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
        this.title = title;
        this.image = image;
    }

    @Override
    public int getCount() {
        return title.size();
    }

    @Override
    public Object getItem(int position) {
        return title.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addTitleArray(ArrayList<String> title){
        this.title = title;
    }

    public void addImageArray(ArrayList<String> image){
        this.image = image;
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

        Glide.with(mContext).load(image.get(position)).into(holder.icon);
        holder.title.setText(title.get(position));

        return convertView;
    }

    class ViewHolder{
        public ImageView icon;
        public TextView title;
    }
}

