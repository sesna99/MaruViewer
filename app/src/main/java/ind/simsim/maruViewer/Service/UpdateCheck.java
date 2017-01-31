package ind.simsim.maruViewer.Service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

import ind.simsim.maruViewer.Event.UpdateEvent;
import ind.simsim.maruViewer.UI.Activity.MainActivity;

/**
 * Created by jack on 2017. 1. 25..
 */

public class UpdateCheck {
    private Activity activity;
    private long downloadID;
    private DownloadManager downloadManager;
    private DownloadManager.Request request;

    public UpdateCheck(Activity activity) {
        this.activity = activity;
    }

    public void check(){
        new Version().execute();
    }

    public void download(String url) {
        Uri downloadUri = Uri.parse(url);
        downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        request = new DownloadManager.Request(downloadUri);
        String cookie = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("Cookie", cookie);
        request.setTitle("마루뷰어");
        request.setDescription("업데이트");

        downloadID = downloadManager.enqueue(request);

        activity.registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        activity.finish();
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadID);

            Cursor cursor = downloadManager.query(query);
            if(cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                if(status == DownloadManager.STATUS_SUCCESSFUL){
                    int localFileNameId = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                    Intent openApk = new Intent(Intent.ACTION_VIEW);
                    openApk.setDataAndType(Uri.fromFile(new File(cursor.getString(localFileNameId))),"application/vnd.android.package-archive");
                    openApk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(openApk);
                }
            }
        }
    };

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
                                download(downLink);
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
