package ind.simsim.maruViewer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by admin on 2016-02-19.
 */
public class ComicsEpisodeActivity extends Activity {
    private ListView listView;
    private View header;
    private ImageView image;
    private ComicsEpisodeAdapter adapter;
    private ArrayList<String> episode;
    private ArrayList<String> link;
    private Episode task;
    private String url, imageUrl;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;
    private String path;
    private int dWidth, dHeight;
    private Intent intent;
    private Context mContext;
    private ComicsEpisodeAdapter.ViewHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_episode);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        View mCustomView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar, null);
        actionBar.setCustomView(mCustomView);

        path = getCacheDir() + "/maru.html";
        file = new File(path);
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageButton imageButton = (ImageButton)mCustomView.findViewById(R.id.imageView);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        listView = (ListView) findViewById(R.id.listView);
        header = getLayoutInflater().inflate(R.layout.comics_episode_header, null, false);
        image = (ImageView)header.findViewById(R.id.imageView);
        url = getIntent().getStringExtra("url");
        url = url.contains("marumaru") ? url : "http://marumaru.in" + url;
        episode = new ArrayList<>();
        link = new ArrayList<>();
        adapter = new ComicsEpisodeAdapter(this, R.layout.comics_episode_item, episode);
        listView.addHeaderView(header);
        listView.setAdapter(adapter);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        dWidth = dm.widthPixels;
        dHeight = dm.heightPixels / 2;

        mContext = this;

        task = new Episode();
        task.execute();
    }

    class Episode extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                long startTime = System.currentTimeMillis();
                Document document = Jsoup.connect(url).timeout(0).get();
                Elements elements = document.select("div a[href*=shencomics]");
                Elements elements2 = document.select("div img[src*=quickimage]");
                Elements elements3 = document.select("a");

                int size = elements.size();
                Log.i("elementsSize", size + "");
                for(int i = 0; i < size; i++) {
                    if(elements.get(i).text().equals("")){
                        continue;
                    }
                    episode.add(elements.get(i).text());
                    link.add(elements.get(i).attr("href"));
                }
                imageUrl = elements2.attr("src");
                size = elements3.size();
                for(int i = 0; i < size; i++) {
                    if(!elements3.get(i).text().contains("전편")){
                        continue;
                    }
                    episode.add(elements3.get(i).text());
                    link.add(elements3.get(i).attr("href"));
                    break;
                }

                long endTime = System.currentTimeMillis();
                Log.i("time", (endTime - startTime) / 1000.f + "초");

                document = null;
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            Glide.with(ComicsEpisodeActivity.this).load(imageUrl).into(image);
            adapter.addEpisodeArray(episode);
            adapter.refresh();
        }
    }

    class Comics extends AsyncTask<ArrayList<Object>, Void, Void> {
        private Elements image;
        private StringBuilder html;
        private int size, position;
        private ComicsEpisodeAdapter.ViewHolder holder;
        private ArrayList<Object> objects;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(ArrayList<Object>... params) {
            objects = params[0];
            holder = (ComicsEpisodeAdapter.ViewHolder) objects.get(0);
            position = (int) objects.get(1);
            try {
                long startTime = System.currentTimeMillis();

                Document document = Jsoup.connect(link.get(position)).cookie("wp-postpass_e1ac6d6cb3b647764881f16d009c885c", "%24P%24B7TTtyw0aLlsT1XDbHUOnmABsLoItB0").timeout(0).userAgent("Mozilla/5.0").post();
                image = document.select("img");

                html = new StringBuilder();
                html.append(getResources().getString(R.string.htmlStart));

                size = image.size();
                int width = 0, height = 0;
                Log.i("elementsSize", size + "");
                long median = System.currentTimeMillis();
                Log.i("time", (median - startTime) / 1000.f + "초");
                for (int i = 0; i < size; i++) {
                    if (image.get(i).attr("data-lazy-src").equals("")) {
                        continue;
                    }
                    try {
                        width = Integer.valueOf(image.get(i).attr("width"));
                        height = Integer.valueOf(image.get(i).attr("height"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        width = dWidth;
                        height = dHeight;
                    }
                    height = (height * dWidth) / width;
                    width = dWidth;
                    Log.i("width", width + "");
                    Log.i("height", height + "");
                    html.append("<img src=").append(image.get(i).attr("data-lazy-src")).append(" width=").append(width).append(" height=").append(height).append("/> ");
                }
                html.append(getResources().getString(R.string.htmlEnd));
                Log.i("html", html.toString());

                long endTime = System.currentTimeMillis();
                Log.i("time", (endTime - startTime) / 1000.f + "초");

                document = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            try {
                bw.write(html.toString());
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            intent = new Intent(mContext, ComicsViewer.class);
            intent.putExtra("title", episode.get(position));
            startActivity(intent);
        }
    }

    class ComicsEpisodeAdapter extends BaseAdapter {
        Context mContext;
        LayoutInflater inflater;
        int layout;
        ArrayList<String> episode;

        public ComicsEpisodeAdapter(Context context, int layout, ArrayList<String> episode) {
            this.mContext = context;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.layout = layout;
            this.episode = episode;
        }

        @Override
        public int getCount() {
            return episode.size();
        }

        @Override
        public Object getItem(int position) {
            return episode.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addEpisodeArray(ArrayList<String> episode){
            this.episode = episode;
        }

        public void refresh(){
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final ArrayList<Object> objects = new ArrayList<>();;
            if(convertView == null){
                convertView = inflater.inflate(layout, parent, false);
                holder = new ViewHolder();
                holder.episode = (ActionProcessButton)convertView.findViewById(R.id.button);
                holder.episode.setMode(ActionProcessButton.Mode.ENDLESS);
                convertView.setTag(holder);
            }
            else{
                holder = (ViewHolder)convertView.getTag();
            }

            objects.add((Object) holder);
            objects.add((Object) position);
            holder.episode.setText(episode.get(position));
            holder.episode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    if(position == getCount() - 1){
                        if(link.get(position).contains("marumaru")) {
                            intent = new Intent(mContext, ComicsEpisodeActivity.class);
                            intent.putExtra("url", link.get(position));
                            startActivity(intent);
                            finish();
                        }
                        else{
                            holder.episode.setProgress(75);
                            new Comics().execute(objects);
                        }
                    }
                    else{
                        holder.episode.setProgress(75);
                        new Comics().execute(objects);
                    }
                }
            });

            return convertView;
        }

        class ViewHolder{
            private ActionProcessButton episode;
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        Toast.makeText(ComicsEpisodeActivity.this, ""+itemId, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.cancel(true);
        try {
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

