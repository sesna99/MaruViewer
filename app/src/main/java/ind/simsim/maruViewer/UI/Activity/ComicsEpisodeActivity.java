package ind.simsim.maruViewer.UI.Activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
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

import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Service.ApplicationController;
import ind.simsim.maruViewer.Service.ComicsData;
import ind.simsim.maruViewer.Service.ComicsSave;
import ind.simsim.maruViewer.Service.PreferencesManager;

/**
 * Created by admin on 2016-02-19.
 */
public class ComicsEpisodeActivity extends Activity {
    private Episode task;
    private String episodeUrl;
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
    private String title;
    private PreferencesManager pm;
    private ImageButton save, favorite;
    private TextView titleView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_episode);

        episodeUrl = getIntent().getStringExtra("url");
        episodeUrl = episodeUrl.contains("marumaru") ? episodeUrl : "http://marumaru.in" + episodeUrl;

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

        titleView = (TextView) mCustomView.findViewById(R.id.title);
        titleView.setText(title);
        titleView.setSelected(true);

        pm = PreferencesManager.getInstance(getApplicationContext());

        final Activity activity = this;

        save = (ImageButton) mCustomView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ComicsSave().save(activity, episodeUrl);
            }
        });

        favorite = (ImageButton) mCustomView.findViewById(R.id.favorite);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pm.searchFavorites("e", episodeUrl)){
                    pm.deleteFavorites(pm.getFavoritesPosition("e", episodeUrl));
                    favorite.setBackgroundResource(R.drawable.star3);
                }
                else {
                    pm.setFavorites(new ComicsData(title, imageUrl, "", episodeUrl), "e");
                    favorite.setBackgroundResource(R.drawable.star4);
                }
            }
        });

        if(pm.searchFavorites("e", episodeUrl)){
            favorite.setBackgroundResource(R.drawable.star4);
        }

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

        task = new ComicsEpisodeActivity.Episode();
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
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(mContext);
            dialog.setTitle("Load");
            dialog.setMessage("로딩중..");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document document = Jsoup.connect(episodeUrl).timeout(0).get();
                Elements image = document.select("div img[src*=quickimage]");
                Elements content = document.select("div[class=content] a");
                html = new StringBuilder();
                html.append("<html><body><div style=\"text-align: center\"><img src=\"").append(image.attr("src")).append("\" width=").append(dWidth / 3.5).append(" height=").append(dHeight / 2.5).append("></div><br>");
                html.append("<div align=\"center\" style=\"color: rgb(0, 0, 0); font-family: dotum; font-size: 12px; line-height: 19.2px; text-align: center;\"><br>");
                imageUrl = image.attr("src");

                int size = content.size();
                for (int i = 0; i < size; i++) {
                    if (!content.get(i).attr("href").equals("")) {
                        if(!content.get(i).attr("href").contains("http"))
                            break;
                        html.append("<div align=\"center\" style=\"line-height: 19.2px;\"><a target=\"_blank\" href=\"").append(content.get(i).attr("href").replace("shencomics","yuncomics")).append("\" target=\"_self\"><font color=\"#717171\" style=\"color: rgb(113, 113, 113); text-decoration: none;\"><span style=\"font-size: 18.6667px; line-height: 19.2px;\">").append(content.get(i).text()).append("</span></font></a></div><br>");
                    }
                }


                html.append("</div></body></html>");

                document = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            dialog.dismiss();
            try {
                bw.write(html.toString());
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            webView.loadUrl("file://" + path);
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
                        intent  = new Intent(getApplicationContext(), ComicsViewer.class);
                        intent.putExtra("comicsUrl", url);
                        intent.putExtra("episodeUrl", episodeUrl);
                        startActivity(intent);
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

}

