package ind.simsim.maruViewer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by admin on 2016-02-18.
 */
public class ComicsViewer extends Activity {
    private ViewPager comics;
    private ComicsViewerAdapter adapter;
    private ArrayList<String> imageArray;
    private String url;
    private View mCustomView;
    private ActionBar actionBar;
    private ImageButton imageButton;
    private String title;
    private TextView textView;
    private Intent intent;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;
    private String path, html;
    private WebView webView;
    private WebSettings settings;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics);

        context = this;
        intent = getIntent();
        actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mCustomView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar, null);
        actionBar.setCustomView(mCustomView);

        imageButton = (ImageButton) mCustomView.findViewById(R.id.imageView);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        title = intent.getStringExtra("title");
        html = intent.getStringExtra("html");
        textView = (TextView) mCustomView.findViewById(R.id.title);
        textView.setText(title);
        if (title.length() > 20)
            textView.setTextSize(15);

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

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);

        webView.loadUrl("file://" + path);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            bw.close();
            fw.close();
        }catch (Exception e){
        }
    }
}
