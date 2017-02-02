package ind.simsim.maruViewer.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ind.simsim.maruViewer.Event.ComicsDownLoadEvent;
import ind.simsim.maruViewer.Event.DownLoadCompleteEvent;
import ind.simsim.maruViewer.Model.ComicsData;
import ind.simsim.maruViewer.R;

public class DownloadService extends Service {
    private String url;
    private int code;
    private ArrayList<ArrayList<ComicsData>> saveComics;
    private int NOTIFICATION_ID = 19980313;

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            code = intent.getIntExtra("code", 0);

            if (code == 1) {
                EventBus.getDefault().register(this);
            } else if (code == 2) {
                url = intent.getStringExtra("url");
                new UpdateDownload().execute(url);
            }
        }
        return START_STICKY;
    }

    class ComicsDownload extends AsyncTask<Void, Void, Void>{
        private int max = 0;
        private NotificationManager nm;
        private Notification.Builder mBuilder;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            mBuilder = new Notification.Builder(getApplicationContext());
            mBuilder.setSmallIcon(R.drawable.ic_launcher);
            mBuilder.setTicker("다운로드중...");
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setContentTitle("마루뷰어");
            mBuilder.setAutoCancel(true);
            mBuilder.setOngoing(true);

            startForeground(NOTIFICATION_ID, mBuilder.build());
        }

        @Override
        protected Void doInBackground(Void... params) {
            for(int i = 0; i < saveComics.size(); i++){
                max += saveComics.get(i).size();
            }
            float increase = 100;
            increase = increase/max;

            Bitmap mBitmap = null;
            InputStream in = null;
            OutputStream outStream = null;
            int progress = 0;
            String oldPercent = "";
            String path;
            File file;
            for(int i = 0; i < saveComics.size(); i++) {
                path = PreferencesManager.getInstance(getApplicationContext()).getDownLoadDirectory() + saveComics.get(i).get(0).getTitle();
                file = new File(path);
                if(!file.exists()){  // 원하는 경로에 폴더가 있는지 확인
                    file.mkdirs();
                }
                for(int j = 0; j < saveComics.get(i).size(); j++) {
                    try {
                        in = new java.net.URL(saveComics.get(i).get(j).getImage()).openStream();
                        mBitmap = BitmapFactory.decodeStream(in);
                        in.close();
                        outStream = null;

                        path = PreferencesManager.getInstance(getApplicationContext()).getDownLoadDirectory() + saveComics.get(i).get(j).getTitle();
                        file = new File(path, saveComics.get(i).get(j).getImageName());

                        outStream = new FileOutputStream(file);
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        String percent = String.format("%.0f", increase * progress);
                        if(!oldPercent.equals(percent)) {
                            Log.i("percent", percent);
                            mBuilder.setProgress(max, ++progress, false);
                            mBuilder.setContentText("다운로드 : " + percent + "%");
                            nm.notify(NOTIFICATION_ID, mBuilder.build());
                            oldPercent = percent;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            mBuilder.setProgress(max, ++progress, false);
            mBuilder.setContentText("다운로드 : " + 100 + "%");
            nm.notify(NOTIFICATION_ID, mBuilder.build());

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            stopForeground(true);
            EventBus.getDefault().post(new DownLoadCompleteEvent(true));
        }
    }

    private class UpdateDownload extends AsyncTask<String, Void, Void> {
        private NotificationManager nm;
        private Notification.Builder mBuilder;
        private Notification notification;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            mBuilder = new Notification.Builder(getApplicationContext());
            mBuilder.setSmallIcon(R.drawable.ic_launcher);
            mBuilder.setTicker("업데이트중...");
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setContentTitle("업데이트");
            mBuilder.setAutoCancel(true);
            mBuilder.setOngoing(true);

            startForeground(NOTIFICATION_ID, mBuilder.build());
        }

        @Override
        protected Void doInBackground(String...url) {
            try {
                URL downloadUrl = new URL(url[0]);

                HttpURLConnection urlConnection = (HttpURLConnection) downloadUrl.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                File SDCardRoot = Environment.getExternalStorageDirectory();
                File file = new File(SDCardRoot,"마루뷰어.apk");
                FileOutputStream fileOutput = new FileOutputStream(file);

                InputStream inputStream = urlConnection.getInputStream();
                int totalSize = urlConnection.getContentLength();
                int downloadedSize = 0;

                double increase = 100;
                increase = increase/totalSize;

                byte[] buffer = new byte[1024];
                int bufferLength = 0;

                String oldPercent = "";

                while ( (bufferLength = inputStream.read(buffer)) > 0 )
                {
                    fileOutput.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    String percent = String.format("%.0f", increase * downloadedSize);

                    if(!oldPercent.equals(percent)) {
                        Log.i("percent", percent);
                        mBuilder.setProgress(totalSize, downloadedSize, false);
                        mBuilder.setContentText("다운로드 : " + percent + "%");
                        nm.notify(NOTIFICATION_ID, mBuilder.build());
                        oldPercent = percent;
                    }
                }

                mBuilder.setProgress(totalSize, downloadedSize, false);
                mBuilder.setContentText("다운로드 : " + 100 + "%");
                nm.notify(NOTIFICATION_ID, mBuilder.build());

                fileOutput.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            Log.e("DOWNLOAD", "end");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            stopForeground(true);
            installAPK();
        }
    }

    public void installAPK() {
        File apkFile = new File(Environment.getExternalStorageDirectory() + "/마루뷰어.apk");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        else
            intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", apkFile), "application/vnd.android.package-archive");

        getApplicationContext().startActivity(intent);
    }

    @Subscribe
    public void onComicsDownLoadEvent(ComicsDownLoadEvent event){
        saveComics = event.getData();
        new ComicsDownload().execute();
        EventBus.getDefault().unregister(this);
    }
}
