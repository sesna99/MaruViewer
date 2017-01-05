package ind.simsim.maruViewer.UI.Activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Service.ComicsData;
import ind.simsim.maruViewer.Service.ComicsSave;
import ind.simsim.maruViewer.Service.PreferencesManager;

/**
 * Created by admin on 2016-02-18.
 */
public class ComicsViewer extends Activity {
    private View mCustomView;
    private ActionBar actionBar;
    private ImageButton back, favorite, save;
    private String comicsUrl, episodeUrl;
    private TextView titleView;
    private Intent intent;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;
    private String path;
    private WebView webView;
    private WebSettings settings;
    private Context context;
    private int dWidth, dHeight;
    private ArrayList<ComicsData> comicsDatas;
    private SwipyRefreshLayout nextComics;
    private boolean isFirst = true;
    private PreferencesManager pm;
    private String imageUrl;
    private int scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics);

        context = this;
        intent = getIntent();
        comicsUrl = intent.getStringExtra("comicsUrl");
        episodeUrl = intent.getStringExtra("episodeUrl");
        scroll = intent.getIntExtra("scroll", 0);
        actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mCustomView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar, null);
        actionBar.setCustomView(mCustomView);

        pm = PreferencesManager.getInstance(getApplicationContext());

        back = (ImageButton) mCustomView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        titleView = (TextView) mCustomView.findViewById(R.id.title);
        titleView.setSelected(true);

        favorite = (ImageButton) mCustomView.findViewById(R.id.favorite);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!comicsUrl.equals("")) {
                    if (pm.searchFavorites("c", comicsUrl)) {
                        pm.deleteFavorites(pm.getFavoritesPosition("c", comicsUrl));
                        favorite.setBackgroundResource(R.drawable.star3);
                    } else {
                        pm.setFavorites(new ComicsData(titleView.getText().toString(), imageUrl, comicsUrl, episodeUrl), "c");
                        favorite.setBackgroundResource(R.drawable.star4);
                    }
                } else
                    Toast.makeText(context, "저장된 만화는 즐겨찾기가 불가능합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        if (!comicsUrl.equals(""))
            if (pm.searchFavorites("c", comicsUrl))
                favorite.setBackgroundResource(R.drawable.star4);


        comicsDatas = new ArrayList<>();

        nextComics = (SwipyRefreshLayout) findViewById(R.id.nextComics);
        nextComics.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        nextComics.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (!episodeUrl.equals("")) {
                    if (comicsDatas.size() > 0)
                        loadNextComics();
                    else
                        new Episode().execute();
                } else {
                    nextComics.setRefreshing(false);
                }
            }
        });

        final Activity activity = this;
        save = (ImageButton) mCustomView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!comicsUrl.equals(""))
                    new ComicsSave().save(activity, episodeUrl);
                else
                    Toast.makeText(activity, "저장된 만화는 저장이 불가능합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        dWidth = dm.widthPixels;
        dHeight = dm.heightPixels - 100;

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());

        settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);

        if (comicsUrl.equals(""))
            createHtml();
        else
            new Comics().execute();
    }

    class Comics extends AsyncTask<Void, Void, Void> {
        private Elements image, title;
        private StringBuilder html;
        private int size;
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isFirst) {
                dialog = new ProgressDialog(context);
                dialog.setTitle("Load");
                dialog.setMessage("로딩중..");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.i("url", comicsUrl);
                String url = Jsoup.connect(comicsUrl).timeout(0).followRedirects(true).execute().url().toExternalForm();
                Connection conn = Jsoup.connect(url).cookie("wp-postpass_e1ac6d6cb3b647764881f16d009c885c", "%24P%24B7TTtyw0aLlsT1XDbHUOnmABsLoItB0").timeout(0).userAgent("Firefox/2.0.0.6").referrer("http://marumaru.in").method(Connection.Method.POST);
                Connection.Response res = conn.execute();
                Document document = res.parse();

                image = document.select("img");
                title = document.select("title");

                html = new StringBuilder();
                html.append(getResources().getString(R.string.htmlStart));

                imageUrl = image.get(0).attr("data-src");

                size = image.size();
                Log.i("size", size + "");
                int width = 0, height = 0;
                for (int i = 0; i < size; i++) {
                    if (image.get(i).attr("data-src").equals("")) {
                        continue;
                    }
                    width = dWidth;
                    height = dHeight;
                    html.append("<img src=").append("http://wasabisyrup.com").append(image.get(i).attr("data-src")).append(" width=").append(width).append(" height=").append(height).append("/> ");
                    Log.i("image", image.get(i).attr("data-src"));
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
            if (isFirst) {
                dialog.dismiss();
                isFirst = false;
            }
            path = getCacheDir() + "/maru.html";
            file = new File(path);
            try {
                fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                bw.write(html.toString());
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String temp = title.get(0).text();
            titleView.setText(temp.substring(0, temp.length() - 13));
            loadComics();
        }
    }

    class Episode extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document document = Jsoup.connect(episodeUrl).timeout(0).get();
                Elements content = document.select("div[class=content] a");

                int size = content.size();
                for (int i = 0; i < size; i++) {
                    if (!content.get(i).attr("href").equals("")) {
                        if (!content.get(i).attr("href").contains("http"))
                            break;
                        comicsDatas.add(new ComicsData(content.get(i).text(), content.get(i).attr("href")));
                    }
                }

                document = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            loadNextComics();
        }
    }

    public void createHtml() {
        StringBuilder html = new StringBuilder();
        html.append(getResources().getString(R.string.htmlStart));

        ArrayList<String> image = intent.getStringArrayListExtra("image");
        int size = image.size();
        for (int i = 0; i < size; i++)
            html.append("<img src=").append("\"file://" + image.get(i)).append("\" width=").append(dWidth).append(" height=").append(dHeight * 2).append("/> ");
        html.append(getResources().getString(R.string.htmlEnd));

        path = getCacheDir() + "/maru.html";
        file = new File(path);
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(html.toString());
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        titleView.setText(intent.getStringExtra("title"));
        loadComics();
    }

    public void loadComics() {
        webView.loadUrl("file://" + path);
        nextComics.setRefreshing(false);
        if (!comicsUrl.equals("")) {
            if (pm.searchLately(comicsUrl))
                pm.deleteLately(pm.getLatelyPosition(comicsUrl));
            pm.setLately(new ComicsData(titleView.getText().toString(), imageUrl, comicsUrl, episodeUrl), webView.getScrollY());
        }
        webView.setScrollY(scroll);
    }

    public void loadNextComics() {
        for (int i = 0; i < comicsDatas.size(); i++) {
            if (titleView.getText().toString().equals(comicsDatas.get(i).getTitle())) {
                if (i + 1 < comicsDatas.size()) {
                    if (!comicsDatas.get(i + 1).getTitle().contains("전편")) {
                        comicsUrl = comicsDatas.get(i + 1).getLink();
                        new Comics().execute();
                    } else {
                        break;
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!comicsUrl.equals(""))
            pm.updateLately(pm.getLatelyPosition(comicsUrl), webView.getScrollY());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            bw.close();
            fw.close();
        } catch (Exception e) {
        }
    }
}
