package ind.trycatch.maruViewer.Service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import ind.trycatch.maruViewer.Event.DownLoadCompleteEvent;
import ind.trycatch.maruViewer.Model.ComicsModel;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.UI.Adapter.SaveDialogListAdapter;

/**
 * Created by jack on 2016. 12. 8..
 */

public class ComicsSave {
    private Activity activity;
    private Context context;
    private ArrayList<ArrayList<ComicsModel>> saveComics;

    public void save(Activity activity, String url){
        this.activity = activity;
        this.context = activity.getApplicationContext();
        new Episode().execute(url);
    }

    public void createDialog(ArrayList<ComicsModel> comicsModels){
        Dialog dialog;
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.save_dialog, null);
        final ListView listView = (ListView) view.findViewById(R.id.dialog_listview);
        final SaveDialogListAdapter adapter = new SaveDialogListAdapter(activity, comicsModels);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.setChecked(i);
                if(i == 0)
                    adapter.setAllChecked(adapter.getChecked(0));
                adapter.notifyDataSetChanged();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("다운로드");
        builder.setView(view);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                new Comics().execute(adapter.getCheckedData());
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    class Episode extends AsyncTask<String, Void, Void> {
        ArrayList<ComicsModel> comicsModels;
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(activity);
            dialog.setTitle("Load");
            dialog.setMessage("리스트 생성중..");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            comicsModels = new ArrayList<>();
            ComicsModel data;
            try {
                Document document = Jsoup.connect(url).timeout(0).get();
                Elements content = document.select("div[class=content] a");
                int size = content.size();
                for(int i = 0; i < size; i++){
                    if (!content.get(i).attr("href").equals("")) {
                        if (!content.get(i).attr("href").contains("http"))
                            break;
                        if(content.get(i).text().equals(""))
                            continue;
                        data = new ComicsModel();
                        data.setTitle(content.get(i).text());
                        data.setLink(content.get(i).attr("href").replace("shencomics", "yuncomics"));
                        comicsModels.add(data);
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
            if(comicsModels.get(comicsModels.size() - 1).getTitle().contains("전편"))
                new Episode().execute(comicsModels.get(comicsModels.size() - 1).getLink());
            else
                createDialog(comicsModels);
            dialog.dismiss();
        }
    }

    class Comics extends AsyncTask<ArrayList<ComicsModel>, Void, Void> {
        private Elements image, title;
        private int size;
        private ArrayList<ComicsModel> saveData;
        private Document document;
        private ProgressDialog dialog;
        private String url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(activity);
            dialog.setTitle("Load");
            dialog.setMessage("파일 수 계산중..");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(ArrayList<ComicsModel>... params) {
            saveData = params[0];
            saveComics = new ArrayList<>();
            ComicsModel data;
            ArrayList<ComicsModel> datas;

            try {
                for(int i = 0; i < saveData.size(); i++) {
                    url = Jsoup.connect(saveData.get(i).getLink()).timeout(0).followRedirects(true).execute().url().toExternalForm();
                    document = Jsoup.connect(url).data("pass", "qndxkr").timeout(0).userAgent("Firefox/2.0.0.6").referrer("http://marumaru.in").method(Connection.Method.POST).execute().parse();
                    image = document.select("img");
                    title = document.select("title");

                    size = image.size();

                    datas = new ArrayList<>();
                    for (int j = 0; j < size; j++) {
                        if (image.get(j).attr("data-src").equals("")) {
                            continue;
                        }
                        data = new ComicsModel();
                        data.setTitle(saveData.get(i).getTitle());
                        data.setImageName(j + ".jpg");
                        data.setImage("http://wasabisyrup.com" + image.get(j).attr("data-src"));
                        datas.add(data);
                    }
                    saveComics.add(datas);
                }

                document = null;
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            dialog.dismiss();

            new ComicsDownload().execute();

            //EventBus.getDefault().post(new ComicsDownLoadEvent(saveComics));
        }
    }

    class ComicsDownload extends AsyncTask<Void, Void, Void> {
        private int NOTIFICATION_ID = 19980313;
        private int max = 0;
        private NotificationManager nm;
        private Notification.Builder mBuilder;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            mBuilder = new Notification.Builder(context);
            mBuilder.setSmallIcon(R.drawable.ic_launcher);
            mBuilder.setTicker("다운로드중...");
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setContentTitle("마루뷰어");
            mBuilder.setAutoCancel(true);
            mBuilder.setOngoing(true);

            nm.notify(NOTIFICATION_ID, mBuilder.build());
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
                path = PreferencesManager.getInstance(context).getDownLoadDirectory() + saveComics.get(i).get(0).getTitle();
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

                        path = PreferencesManager.getInstance(context).getDownLoadDirectory() + saveComics.get(i).get(j).getTitle();
                        file = new File(path, saveComics.get(i).get(j).getImageName());

                        outStream = new FileOutputStream(file);
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        String percent = String.format("%.0f", increase * progress);
                        if(!oldPercent.equals(percent)) {
                            Log.i("percent", percent);
                            mBuilder.setProgress(max, ++progress, false);
                            mBuilder.setContentText("다운로드 : " + percent + "%  " + saveComics.get(i).get(j).getTitle() + "  " + saveComics.get(i).get(j).getImageName());
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            nm.cancel(NOTIFICATION_ID);

            EventBus.getDefault().post(new DownLoadCompleteEvent(true));
        }
    }
}
