package ind.trycatch.maruViewer.Service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import ind.trycatch.maruViewer.Event.UpdateEvent;

/**
 * Created by jack on 2017. 1. 25..
 */

public class UpdateCheck {
    private Activity activity;
    private Context context;
    private long downloadID;
    private DownloadManager downloadManager;
    private DownloadManager.Request request;

    public UpdateCheck(Activity activity) {
        this.activity = activity;
        context = activity.getApplicationContext();
    }

    public void check(){
        new Version().execute();
    }

    class Version extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private String version;
        private String downLink;
        private Document document;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(activity);
            dialog.setTitle("Check");
            dialog.setMessage("업데이트 확인중..");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                document = Jsoup.connect("http://trycatch98.tistory.com/entry/%EB%A7%88%EB%A3%A8%EB%B7%B0%EC%96%B4-%EB%B2%84%EC%A0%84").timeout(0).post();
                Elements versionElements = document.select("div p");
                Elements downLinkElements = document.select("div p a");

                version = versionElements.get(0).text().split(":")[1];
                downLink = downLinkElements.attr("href");

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            dialog.cancel();

            PackageInfo pInfo = null;
            try {
                pInfo = activity.getPackageManager().getPackageInfo(
                        activity.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
            }

            if (pInfo.versionName.equals(version)) {
                EventBus.getDefault().post(new UpdateEvent(false));
            } else {
                //EventBus.getDefault().post(new UpdateEvent(true, downLink));
                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setTitle("업데이트")
                        .setMessage("최신 버전이 존재합니다.\n확인을 클릭 시 자동으로 다운로드 됩니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downLink));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                activity.finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activity.finish();
                            }
                        })
                        .create();
                dialog.show();
            }
        }
    }
}
