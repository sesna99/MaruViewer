package ind.trycatch.maruViewer.UI.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.Event.DivEvent;
import ind.trycatch.maruViewer.Event.DownLoadRemoveEvent;
import ind.trycatch.maruViewer.Event.FavoriteEvent;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.Model.ComicsModel;
import ind.trycatch.maruViewer.Service.ComicsSave;
import ind.trycatch.maruViewer.Service.PreferencesManager;
import ind.trycatch.maruViewer.UI.JavascriptInterface;

/**
 * Created by admin on 2016-02-18.
 */
public class ComicsViewer extends BaseActivity {
    @BindView(R.id.back_button)
    ImageView back_button;

    @BindView(R.id.title_view)
    TextView title_view;

    @BindView(R.id.save_button)
    ImageView save_button;

    @BindView(R.id.remove_view)
    ImageView remove_view;

    @BindView(R.id.favorite_button)
    ImageView favorite_button;

    @BindView(R.id.webView)
    WebView webView;

    private String comicsUrl, episodeUrl, imageUrl, title, path;
    private Intent intent;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;
    private WebSettings settings;
    private Context context;
    private int dWidth, dHeight;
    private ArrayList<ComicsModel> comicsModels;
    private ArrayList<String> image;
    private SwipyRefreshLayout nextComics;
    private boolean isFirst = true;
    private boolean isDiv;
    private PreferencesManager pm;
    private int scroll;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics);

        ButterKnife.bind(this);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        EventBus.getDefault().register(this);

        context = this;
        intent = getIntent();
        comicsUrl = intent.getStringExtra("comicsUrl");
        episodeUrl = intent.getStringExtra("episodeUrl");
        title = intent.getStringExtra("title");
        scroll = intent.getIntExtra("scroll", 0);
        isDiv = intent.getBooleanExtra("div", false);
        Log.i("Scroll", scroll+"");

        pm = PreferencesManager.getInstance(getApplicationContext());

        title_view.setText(title);
        title_view.setSelected(true);

        back_button.setVisibility(View.VISIBLE);

        if (!comicsUrl.equals(""))
            if (pm.searchFavorites("c", comicsUrl))
                favorite_button.setBackgroundResource(R.drawable.star1);


        comicsModels = new ArrayList<>();

        nextComics = (SwipyRefreshLayout) findViewById(R.id.nextComics);
        nextComics.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        nextComics.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (!episodeUrl.equals("")) {
                    if (comicsModels.size() > 0)
                        loadNextComics();
                    else
                        new Episode().execute();
                } else {
                    nextComics.setRefreshing(false);
                }
            }
        });

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        dWidth = dm.widthPixels;
        dHeight = dm.heightPixels - 200;

        initWebview();

        if (comicsUrl.equals("")){
            createHtml();

            remove_view.setVisibility(View.VISIBLE);
            remove_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(ComicsViewer.this)
                            .setTitle("삭제")
                            .setMessage("정말로 삭제하시겠습니까.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    removeComics();
                                    finish();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create();
                    dialog.show();
                }
            });
        }
        else{
            new Comics().execute();

            save_button.setVisibility(View.VISIBLE);
            favorite_button.setVisibility(View.VISIBLE);

            back_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            favorite_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (pm.searchFavorites("c", comicsUrl)) {
                        pm.deleteFavorites(pm.getFavoritesPosition("c", comicsUrl));
                        favorite_button.setBackgroundResource(R.drawable.star3);
                    } else {
                        pm.setFavorites(new ComicsModel(title_view.getText().toString(), imageUrl, comicsUrl, episodeUrl), "c");
                        favorite_button.setBackgroundResource(R.drawable.star1);
                    }
                }
            });

            save_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!comicsUrl.equals(""))
                        new ComicsSave().save(ComicsViewer.this, episodeUrl);
                }
            });
        }
    }

    class Comics extends AsyncTask<Void, Void, Void> {
        private Elements image;
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
                Connection conn = Jsoup.connect(url).data("pass", "qndxkr").timeout(0).userAgent("Firefox/2.0.0.6").referrer("http://marumaru.in").method(Connection.Method.POST);
                Connection.Response res = conn.execute();
                Document document = res.parse();

                image = document.select("img");

                html = new StringBuilder();
                html.append("<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "  <head>\n" +
                        "    <meta charset=\"utf-8\">\n" +
                        "    <style media=\"screen\">\n" +
                        "      img{\n" +
                        "        min-width: 100%;\n" +
                        "        min-height: 100%;\n" +
                        "      }\n" +
                        "      .crop img{\n" +
                        "        min-width: 200%;\n" +
                        "        min-height: 100%;\n" +
                        "      }\n" +
                        "      .crop{\n" +
                        "        overflow: hidden;\n" +
                        "        height: 100%;\n" +
                        "        width: 100%;\n" +
                        "      }\n" +
                        "    </style>\n" +
                        "  </head>\n" +
                        "  <body>\n" +
                        "    <center>\n");

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
                    String imageUrl = "http://wasabisyrup.com" + image.get(i).attr("data-src").replaceAll(" ", "%20");
                    html.append("<div>").append("<img src=\"").append(imageUrl).append("\"/></div>");
                }

                html.append(
                        "    </center>\n" +
                        "  </body>\n" +
                        "\n" +
                        "<script type=\"text/javascript\">\n" +
                        "  var contentDiv = document.getElementsByTagName(\"center\")[0];\n" +
                        "  var divs = document.getElementsByTagName(\"div\");\n" +
                        "  var imgs = document.getElementsByTagName(\"img\");\n" +
                        "  var count = 0;\n" +
                        "  var isDiv = false;\n" +
                        "  Android.checkImage();\n" +
                        "\n" +
                        "  for (var i = 0; i < imgs.length; i++) {\n" +
                        "    var img = imgs.item(i);\n" +
                        "    var div = divs.item(i);\n" +
                        "\n" +
                        "    img.position = i;\n" +
                        "    img.lastPosition = imgs.length - 1;\n" +
                        "    img.div = div;\n" +
                        "\n" +
                        "    img.onload = function() {\n" +
                        "      if(this.naturalWidth > this.naturalHeight){\n" +
                        "        Android.divImage();\n" +
                        "        this.div.setAttribute(\"class\", \"crop\");\n" +
                        "        var newDiv = document.createElement(\"div\");\n" +
                        "        newDiv.setAttribute(\"class\", \"crop\");\n" +
                        "        newDiv.innerHTML = this.div.innerHTML;\n" +
                        "        newDiv.getElementsByTagName(\"img\")[0].align = \"right\";\n" +
                        "        contentDiv.insertBefore(newDiv, this.div);\n" +
                        "        isDiv = true;\n" +
                        "      }\n" +
                        "      count++\n" +
                        "      if(count == this.lastPosition){\n" +
                        "        Android.finishDivImage();\n" +
                        "        if(isDiv){\n" +
                        "           Android.divEvent();\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "\n" +
                        "    Android.finishCheckImage();\n" +
                        "</script>\n" +
                        "\n" +
                        "</html>\n");

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
            } catch (Exception e) {
                e.printStackTrace();
            }

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
                        comicsModels.add(new ComicsModel(content.get(i).text(), content.get(i).attr("href")));
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

    public void initWebview(){
        webView.setWebViewClient(new WebViewClient());

        settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webView.addJavascriptInterface(new JavascriptInterface(this), "Android");
    }

    public void createHtml() {
        StringBuilder html = new StringBuilder();
        html.append(getResources().getString(R.string.htmlStart));

        image = intent.getStringArrayListExtra("image");
        int size = image.size();
        for (int i = 0; i < size; i++)
            html.append("<img src=").append("\"file://" + image.get(i)).append("\" width=").append(dWidth).append(" height=").append(dHeight).append("/> ");
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

        title_view.setText(title);
        loadComics();
    }

    public void loadComics() {
        webView.loadUrl("file://" + path);
        webView.setScrollY(scroll);
        scroll = 0;
        nextComics.setRefreshing(false);
        if (!comicsUrl.equals(""))
            if (!pm.searchLately(comicsUrl))
                pm.setLately(new ComicsModel(title_view.getText().toString(), imageUrl, comicsUrl, episodeUrl), webView.getScrollY());
    }

    public void loadNextComics() {
        for (int i = 0; i < comicsModels.size(); i++) {
            if (title_view.getText().toString().equals(comicsModels.get(i).getTitle())) {
                if (i + 1 < comicsModels.size()) {
                    if (!comicsModels.get(i + 1).getTitle().contains("전편")) {
                        comicsUrl = comicsModels.get(i + 1).getLink();
                        title = comicsModels.get(i + 1).getTitle();
                        title_view.setText(title);
                        new Comics().execute();
                        break;
                    } else {
                        Toast.makeText(context, "마지막 화입니다", Toast.LENGTH_SHORT).show();
                        nextComics.setRefreshing(false);
                        break;
                    }
                }
                Toast.makeText(context, "마지막 화입니다", Toast.LENGTH_SHORT).show();
                nextComics.setRefreshing(false);
                break;
            }
        }
    }

    public void removeComics() {
        String root = "";
        String temp[] = image.get(0).split("/");
        for(int i = 0; i < temp.length - 1; i++)
            root += temp[i] + "/";
        File file = new File(root.substring(0, root.length() - 1));
        Log.i("file", file.getAbsolutePath());
        for(File childFile : file.listFiles()) {
            Log.i("child", childFile.getAbsolutePath());
            childFile.delete();
        }
        file.delete();    //root 삭제
        EventBus.getDefault().post(new DownLoadRemoveEvent(true));
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
        if (!comicsUrl.equals(""))
            pm.updateLately(pm.getLatelyPosition(comicsUrl), webView.getScrollY());
        webView = null;
        try {
            bw.close();
            fw.close();
        } catch (Exception e) {
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onDivEvent(DivEvent event){
        Log.i("event", event.isSucceed()+"");
        if(!isDiv){
            Intent intent = new Intent(ComicsViewer.this, ComicsViewer.class);
            intent.putExtra("comicsUrl", comicsUrl);
            intent.putExtra("episodeUrl", episodeUrl);
            intent.putExtra("title", title);
            intent.putExtra("scroll", scroll);
            intent.putExtra("div", !isDiv);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }
}
