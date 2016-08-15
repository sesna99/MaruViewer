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

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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
                Elements episode = document.select("div[style*=text-align]");
                Elements script = document.select("script");
                Elements image = document.select("div img[src*=quickimage]");
                html = new StringBuilder();
                html.append("<html><body><div style=\"text-align: center\"><img src=\"").append(image.attr("src")).append("\" width=").append(dWidth / 3.5).append(" height=").append(dHeight / 2.5).append("></div><br>");
                html.append("<div style=\"text-align: center;\" align=\"center\">");
                html.append(episode.html());
                html.append("</div>");

                /*
                Elements episode = document.select("div[align*=center] span");
                Elements allEpisode = document.select("div[align*=center] a[href*=maru]");
                Elements script = document.select("script");
                Elements image = document.select("div img[src*=quickimage]");
                html = new StringBuilder();
                html.append("<html><body><div style=\"text-align: center\"><img src=\"").append(image.attr("src")).append(" width=").append(dWidth / 3.5).append(" height=").append(dHeight / 2.5).append("></div><br>");

                int size = episode.size();
                Log.i("elementsSize", size + "");
                for (int i = 0; i < size; i++) {
                    if (episode.get(i).attr("cf-token").equals(""))
                        continue;
                    html.append("<span style=\"font-size: 14pt;\"><span target=\"_self\" cf-token=\"").append(episode.get(i).attr("cf-token")).append("\"><font color=\"#717171\">").append(episode.get(i).text()).append("</font></span></span><br>");
                }
                //http://marumaru.in/?c=1/30&amp;cat=%EC%9B%94%EA%B0%84&amp;mod=view&amp;uid=41705" 전편 보러가기
                if(!allEpisode.text().equals(""))
                    html.append("<div align=\"center\" style=\"color: rgb(0, 0, 0); font-family: dotum; font-size: 12px; line-height: 19.2px;\">").append(" <a target=\"_blank\" href=\"").append(allEpisode.attr("href")).append("\" style=\"color: rgb(113, 113, 113); text-decoration: none; outline: 0px;\">").append(allEpisode.text()).append("</a></div></div> </div><br>");
                html.append(script.get(16)).append("</body></html>");
                 */

                html.append("<script type=\"text/javascript\">\n" +
                        "!function(){\"use strict\";var a,b,c,d,e,f=(function(){var t=\"YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo\",o={a:\"Z2V0QXR0\",b:\"Y3Vyc29y\",d:\"aHR0cDovL3d3dy5zaGVuY29taWNzLmNvbS9hcmNoaXZlcy8=\",e:\"\",f:[\"fr\",({}+\"\")[1],\"mC\",t[0xa],t[0xC],\"rC\",({}+\"\")[1],t[0x1e],({}+\"\")[4]].join(\"\"),_a:function(e,x){return e[this._b(this._t(this.a,\"cmlidXRl\"))](x)},_b:function(e){var k=\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\",t=\"\",n,r,i,s,o,u,a,f=0;e=e.replace(/[^A-Za-z0-9+/=]/g,\"\");e=this._t(e,\"====\".substr(this._d(this._e(e.length,4),4)));while(f<e.length){s=k.indexOf(e.charAt(f++));o=k.indexOf(e.charAt(f++));u=k.indexOf(e.charAt(f++));a=k.indexOf(e.charAt(f++));n=this._g(2,s)|this._k(4,o);r=this._g(4,(o&0xf))|this._k(2,u);i=this._g(6,(u&3))|a;t=this._t(t,this._f(n));if(u!=0x40)t=this._t(t,this._f(r));if(a!=64)t=this._t(t,this._f(i));}return t},_c:function(x){var t=this._b(this.t);return t[(this._r(0x3f2f,x))-97]},_d:function(s,d){return d-s},_e:function(s,d){return s%d},_f:function(c){return String[this.f](c)},_g:function(s,d){return d<<s},_h:function(x){return document.createElement(x)},_i:parseInt,_k:function(s,d){return d>>s},_m:function(a,s){return a.join(s)},_n:function(a,s){return a.split(s)},_p:function(){return this._b(\"cG9pbnRlcg\")},_r:function(s,d){return s^d},_s:function(e){return document[this._b(\"cXVlcnlTZWxlY3RvckFsbA\")](e)},_t:function(s,d){return s+d},_u:function(u){return function(){open(u);return false}},_x:function(x){return this._t(x,~[])},_y:function(e,s,v){e.style[s]=v},_z:function(t,s,l){return t.substr(s,l)}};o.t=t;return o})();d=f._h(f._b(\"Y2FudmFz\"));a=\"c\"+f._c(0x3f49)+\"-t\";a+=({}+243)[1]+\"k\"+({}+73)[4]+\"n\";b=f._s(f._m([\"[\",a,\"]\"],\"\"));e=f._b(\"b25jbGljaw\");for(c in b){if(isNaN(c))continue;var g,h,i,j,k,m=[],n=[];g=f._n(f._a(b[c],a),\"-\");h=g[0],j=f._x(f._i(g[2])),i=f._r(f._g(8,j),f._i(g[1]));i=f._i(f._t(f._z(i+[],~~[],4),f._z(i+[],5)));for(d in h){k=f._i(h[f._t(h.length-d,~[])],16);m.push(k&1,(f._k(-~[],k))&1,(f._k(2,k))&1,f._k(3,k));}k=j,d=0;while(i>0)n.push(f._e(i,2)),i=f._k(-~[],i);while(k--)m.splice(0,0,0);for(k in n)m[k]=f._r(n[k],m[k]);m.splice(0,j);for(k in n)m[k]=f._r(m[k],n[k]);for(k in m)d|=f._g(k,m[k]);f._y(b[c],f._b(f.b),f._p());b[c][e]=f._u(f._t(f._b(f.d),d));}}()\n" +
                        "</script>").append("</body></html>");
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
                image = document.select("img");
                title = document.select("h1[class*=entry-title]");

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
            intent.putExtra("title", title.text());
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

