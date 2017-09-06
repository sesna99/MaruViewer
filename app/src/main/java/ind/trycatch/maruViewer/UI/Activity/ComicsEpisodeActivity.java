package ind.trycatch.maruViewer.UI.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.Model.ComicsModel;
import ind.trycatch.maruViewer.Service.ComicsSave;
import ind.trycatch.maruViewer.Service.PreferencesManager;

/**
 * Created by admin on 2016-02-19.
 */
public class ComicsEpisodeActivity extends BaseActivity {
    @BindView(R.id.back_button)
    ImageView back_button;

    @BindView(R.id.title_view)
    TextView title_view;

    @BindView(R.id.save_button)
    ImageView save_button;

    @BindView(R.id.favorite_button)
    ImageView favorite_button;

    @BindView(R.id.webView)
    WebView webView;

    private Episode task;
    private int dWidth, dHeight;
    private Intent intent;
    private Context mContext;
    private WebSettings settings;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;
    private StringBuilder html;
    private String title, imageUrl, episodeUrl, path;
    private PreferencesManager pm;
    private ArrayList<String> episode;
    private Map<String, String> comicsInfo;
    private FirebaseAnalytics firebaseAnalytics;
    private AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_episode);

        ButterKnife.bind(this);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        episodeUrl = getIntent().getStringExtra("url");
        episodeUrl = episodeUrl.contains("marumaru") ? episodeUrl : "http://marumaru.in" + episodeUrl;

        title = getIntent().getStringExtra("title");
        title_view.setSelected(true);

        pm = PreferencesManager.getInstance(getApplicationContext());

        back_button.setVisibility(View.VISIBLE);
        save_button.setVisibility(View.VISIBLE);
        favorite_button.setVisibility(View.VISIBLE);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ComicsSave().save(ComicsEpisodeActivity.this, episodeUrl);
            }
        });

        favorite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pm.searchFavorites("e", episodeUrl)){
                    pm.deleteFavorites(pm.getFavoritesPosition("e", episodeUrl));
                    favorite_button.setBackgroundResource(R.drawable.star3);
                }
                else {
                    pm.setFavorites(new ComicsModel(title_view.getText().toString(), imageUrl, "", episodeUrl), "e");
                    favorite_button.setBackgroundResource(R.drawable.star1);
                }
            }
        });

        if(pm.searchFavorites("e", episodeUrl)){
            favorite_button.setBackgroundResource(R.drawable.star1);
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

        webView.setWebViewClient(new CustomWebViewClient());
        settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        task = new ComicsEpisodeActivity.Episode();
        task.execute();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                Elements episodeTitle = document.select("div[class=subject] h1");
                html = new StringBuilder();
                html.append("<html><head><meta charset=\"utf-8\"/></head><body><div style=\"text-align: center\"><img src=\"").append(image.attr("src")).append("\" width=").append(dWidth / 3.5).append(" height=").append(dHeight / 2.5).append("></div><br>");
                html.append("<div align=\"center\" style=\"color: rgb(0, 0, 0); font-family: dotum; font-size: 12px; line-height: 19.2px; text-align: center;\"><br>");
                imageUrl = image.attr("src");
                episode = new ArrayList<>();
                comicsInfo = new HashMap<>();

                int size = content.size();
                for (int i = 0; i < size; i++) {
                    if (!content.get(i).attr("href").equals("")) {
                        if(!content.get(i).attr("href").contains("http"))
                            break;
                        html.append("<div align=\"center\" style=\"line-height: 19.2px;\"><a target=\"_blank\" href=\"").append(content.get(i).attr("href")).append("\" target=\"_self\"><font color=\"#717171\" style=\"color: rgb(113, 113, 113); text-decoration: none;\"><span style=\"font-size: 18.6667px; line-height: 19.2px;\">").append(content.get(i).text()).append("</span></font></a></div><br>");
                        episode.add(content.get(i).text());
                        comicsInfo.put(content.get(i).attr("href"), content.get(i).text());
                    }
                }

                html.append("</div></body></html>");

                if(title.equals(""))
                    title = episodeTitle.text();
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
            title_view.setText(title);
        }
    }

    class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            try {
                if(!view.getOriginalUrl().equals(url)){
                    Log.i("url", url);
                    if (!url.contains("maru")) {
                        view.loadUrl(view.getOriginalUrl());
                        intent  = new Intent(getApplicationContext(), ComicsViewer.class);
                        intent.putExtra("comicsUrl", url);
                        intent.putExtra("episodeUrl", episodeUrl);
                        intent.putExtra("title", comicsInfo.get(url));
                        startActivity(intent);
                    }
                    else{
                        intent = new Intent(getApplicationContext(), ComicsEpisodeActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("title", "");
                        startActivity(intent);
                        finish();
                    }
                }
            }catch (Exception e){
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.cancel(true);
    }

}

