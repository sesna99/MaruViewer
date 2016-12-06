package ind.simsim.maruViewer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

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
    private ArrayList<String> episode;
    private ArrayList<String> link;
    private Episode task;
    private String url, imageUrl;
    private int dWidth, dHeight;
    private Intent intent;
    private Context mContext;
    private WebView webView;
    private WebSettings settings;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;
    private String path;
    private StringBuilder html;
    private ArrayList<Item> itemList;
    private String items[];
    private boolean selectedItems[];
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_episode);

        url = getIntent().getStringExtra("url");
        url = url.contains("marumaru") ? url : "http://marumaru.in" + url;

        title = getIntent().getStringExtra("title");

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);


        View mCustomView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar, null);
        actionBar.setCustomView(mCustomView);

        ImageButton back = (ImageButton) mCustomView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView titleView = (TextView) mCustomView.findViewById(R.id.title);
        titleView.setText(title);

        ImageButton save = (ImageButton) mCustomView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
                dialog.setTitle("관심분야를 선택하세요.")
                        .setMultiChoiceItems(
                                items,
                                selectedItems,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    }
                                })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(selectedItems.length == 0)
                                    Toast.makeText(ComicsEpisodeActivity.this, "선택된 화가 없습니다.", Toast.LENGTH_SHORT).show();
                                else
                                    new SaveComics().execute();
                            }
                        }).create().show();
            }
        });

        /*listView = (ListView) findViewById(R.id.listView);
        header = getLayoutInflater().inflate(R.layout.comics_episode_header, null, false);
        image = (ImageView)header.findViewById(R.id.imageView);
        adapter = new ComicsEpisodeAdapter(this, R.layout.comics_episode_item, episode);
        listView.addHeaderView(header);
        listView.setAdapter(adapter);*/
        episode = new ArrayList<>();
        link = new ArrayList<>();

        path = getCacheDir() + "/episode.html";
        file = new File(path);
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        dWidth = dm.widthPixels;
        dHeight = dm.heightPixels / 2;

        mContext = this;

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new CustomWebViewClient());
        settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        task = new Episode();
        task.execute();

        Tracker t = ((ApplicationController)getApplication()).getTracker(ApplicationController.TrackerName.APP_TRACKER);
        t.setScreenName("ComicsEpisodeActivity");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    class Episode extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            itemList = new ArrayList<>();
            try {
                Document document = Jsoup.connect(url).timeout(0).get();
                Elements image = document.select("div img[src*=quickimage]");
                Elements content = document.select("div[class=content] a");
                html = new StringBuilder();
                html.append("<html><body><div style=\"text-align: center\"><img src=\"").append(image.attr("src")).append("\" width=").append(dWidth / 3.5).append(" height=").append(dHeight / 2.5).append("></div><br>");
                html.append("<div align=\"center\" style=\"color: rgb(0, 0, 0); font-family: dotum; font-size: 12px; line-height: 19.2px; text-align: center;\"><br>");

                int size = content.size();
                for (int i = 0; i < size; i++) {
                    if (!content.get(i).attr("href").equals("")) {
                        if(!content.get(i).attr("href").contains("http"))
                            break;
                        html.append("<div align=\"center\" style=\"line-height: 19.2px;\"><a target=\"_blank\" href=\"").append(content.get(i).attr("href").replace("shencomics","yuncomics")).append("\" target=\"_self\"><font color=\"#717171\" style=\"color: rgb(113, 113, 113); text-decoration: none;\"><span style=\"font-size: 18.6667px; line-height: 19.2px;\">").append(content.get(i).text()).append("</span></font></a></div><br>");
                        itemList.add(new Item(content.get(i).text(), content.get(i).attr("href").replace("shencomics","yuncomics")));
                    }
                }

                html.append("</div></body></html>");

                size = itemList.size();
                items = new String[size];
                selectedItems = new boolean[size];
                for(int i = 0; i < size; i++) {
                    items[i] = itemList.get(i).getTitle();
                    selectedItems[i] = false;
                }
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
            webView.loadUrl("file://" + path);
        }
    }

    class Comics extends AsyncTask<String, Void, Void> {
        private Elements image, title;
        private StringBuilder html;
        private String url;
        private int size;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            url = params[0];
            try {

                Document document = Jsoup.connect(url).cookie("wp-postpass_e1ac6d6cb3b647764881f16d009c885c", "%24P%24B7TTtyw0aLlsT1XDbHUOnmABsLoItB0").timeout(0).userAgent("Mozilla/5.0").post();
                image = document.select("img[class*=alignnone]");
                title = document.select("title");

                html = new StringBuilder();
                html.append(getResources().getString(R.string.htmlStart));

                size = image.size();
                int width = 0, height = 0;
                Log.i("elementsSize", size + "");
                Log.i("html", image.outerHtml());
                for (int i = 0; i < size; i++) {
                    if (image.get(i).attr("data-src").equals("")) {
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
                    html.append("<img src=").append(image.get(i).attr("data-src")).append(" width=").append(width).append(" height=").append(height).append("/> ");
                }
                html.append(getResources().getString(R.string.htmlEnd));

                document = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            intent = new Intent(mContext, ComicsViewer.class);
            String title = this.title.get(0).text();
            intent.putExtra("title", title.substring(0, title.length() - 12));
            intent.putExtra("html", html.toString());
            startActivity(intent);
        }
    }

    class SaveComics extends AsyncTask<Void, Void, Void> {
        private Elements image;
        private int size;
        private String path;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < selectedItems.length; i++) {
                if(selectedItems[i]) {
                    try {
                        Document document = Jsoup.connect(itemList.get(i).getUrl()).cookie("wp-postpass_e1ac6d6cb3b647764881f16d009c885c", "%24P%24B7TTtyw0aLlsT1XDbHUOnmABsLoItB0").timeout(0).userAgent("Mozilla/5.0").post();
                        image = document.select("img[class*=alignnone]");

                        size = image.size();
                        int width = 0, height = 0;
                        for (int j = 0; j < size; j++) {
                            if (image.get(j).attr("data-src").equals("")) {
                                continue;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {

        }
    }

    class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            try {
                if(!view.getOriginalUrl().equals(url)){
                    if (!url.contains("maru")) {
                        view.stopLoading();
                        view.goBack();
                        Log.i("url", url);
                        new Comics().execute(url);
                    }
                    else{
                        intent = new Intent(getApplicationContext(), ComicsEpisodeActivity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                        finish();
                    }
                }
            }catch (Exception e){
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        Toast.makeText(ComicsEpisodeActivity.this, "" + itemId, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.cancel(true);
    }

    class Item {
        private String title;
        private String url;

        public Item(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }
    }
}

