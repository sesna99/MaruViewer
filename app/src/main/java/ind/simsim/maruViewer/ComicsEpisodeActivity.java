package ind.simsim.maruViewer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_episode);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);


        View mCustomView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar, null);
        actionBar.setCustomView(mCustomView);

        ImageButton imageButton = (ImageButton) mCustomView.findViewById(R.id.imageView);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        url = getIntent().getStringExtra("url");
        url = url.contains("marumaru") ? url : "http://marumaru.in" + url;
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
                Elements episode = document.select("a[href*=shencomics]");
                Elements episode2 = document.select("a[href*=yuncomics]");
                Elements allEpisode = document.select("a[href*=maru][target*=blank]");
                Elements script = document.select("script");
                Elements image = document.select("div img[src*=quickimage]");
                html = new StringBuilder();
                html.append("<html><body><div style=\"text-align: center\"><img src=\"").append(image.attr("src")).append("\" width=").append(dWidth / 3.5).append(" height=").append(dHeight / 2.5).append("></div><br>");
                html.append("<div align=\"center\" style=\"color: rgb(0, 0, 0); font-family: dotum; font-size: 12px; line-height: 19.2px; text-align: center;\"><br>");
                int size = episode.size();
                Log.i("elementsSize", size + "");
                for (int i = 0; i < size; i++) {
                    if(!episode.get(i).attr("href").equals(""))
                        html.append("<div align=\"center\" style=\"line-height: 19.2px;\"><a target=\"_blank\" href=\"").append(episode.get(i).attr("href").replace("www.shencomics", "blog.yuncomics")).append("\" target=\"_self\"><font color=\"#717171\" style=\"color: rgb(113, 113, 113); text-decoration: none;\"><span style=\"font-size: 18.6667px; line-height: 19.2px;\">").append(episode.get(i).text()).append("</span></font></a></div><br>");
                }
                size = episode2.size();
                Log.i("elementsSize", size + "");
                for (int i = 0; i < size; i++) {
                    if(!episode2.get(i).attr("href").equals(""))
                        html.append("<div align=\"center\" style=\"line-height: 19.2px;\"><a target=\"_blank\" href=\"").append(episode2.get(i).attr("href")).append("\" target=\"_self\"><font color=\"#717171\" style=\"color: rgb(113, 113, 113); text-decoration: none;\"><span style=\"font-size: 18.6667px; line-height: 19.2px;\">").append(episode2.get(i).text()).append("</span></font></a></div><br>");
                }
                if(!allEpisode.text().equals(""))
                    html.append("<div align=\"center\" style=\"line-height: 19.2px;\"><a target=\"_blank\" href=\"").append(allEpisode.get(0).attr("href")).append("\" target=\"_self\"><font color=\"#717171\" style=\"color: rgb(113, 113, 113); text-decoration: none;\"><span style=\"font-size: 18.6667px; line-height: 19.2px;\">").append(allEpisode.get(0).text()).append("</span></font></a></div><br>");

                html.append("</div></body></html>");
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
                long startTime = System.currentTimeMillis();

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
            intent = new Intent(mContext, ComicsViewer.class);
            String title = this.title.get(0).text();
            intent.putExtra("title", title.substring(0, title.length() - 12));
            intent.putExtra("html", html.toString());
            startActivity(intent);
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
                e.printStackTrace();
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
}

