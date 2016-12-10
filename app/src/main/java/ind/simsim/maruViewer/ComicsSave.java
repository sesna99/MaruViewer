package ind.simsim.maruViewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.PaintDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by jack on 2016. 12. 8..
 */

public class ComicsSave {
    private Activity activity;
    public void save(Activity activity, String url){
        this.activity = activity;
        new Episode().execute(url);
    }

    public void createDialog(ArrayList<ComicsData> comicsDatas){
        Dialog dialog;
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.save_dialog, null);
        final ListView listView = (ListView) view.findViewById(R.id.dialog_listview);
        final SaveDialogListAdapter adapter = new SaveDialogListAdapter(activity, comicsDatas);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.setChecked(i);
                if(i == 0)
                    adapter.setAllChecked(adapter.getChecked(i));
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
        ArrayList<ComicsData> comicsDatas;
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
            comicsDatas = new ArrayList<>();
            ComicsData data;
            try {
                Document document = Jsoup.connect(url).timeout(0).get();
                Elements content = document.select("div[class=content] a");
                int size = content.size();
                for(int i = 0; i < size; i++){
                    if (!content.get(i).attr("href").equals("")) {
                        if (!content.get(i).attr("href").contains("http"))
                            break;
                        data = new ComicsData();
                        data.setTitle(content.get(i).text());
                        data.setLink(content.get(i).attr("href").replace("shencomics", "yuncomics"));
                        comicsDatas.add(data);
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
            if(comicsDatas.get(comicsDatas.size() - 1).getTitle().contains("전편"))
                new Episode().execute(comicsDatas.get(comicsDatas.size() - 1).getLink());
            else
                createDialog(comicsDatas);
            dialog.dismiss();
        }
    }

    class Comics extends AsyncTask<ArrayList<ComicsData>, Void, Void> {
        private Elements image, title;
        private int size;
        private int dWidth, dHeight;
        private DisplayMetrics dm;
        private ArrayList<ComicsData> saveData;
        private ArrayList<ArrayList<ComicsData>> dataArray;
        private Document document;
        private ProgressDialog dialog;

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
        protected Void doInBackground(ArrayList<ComicsData>... params) {
            saveData = params[0];
            dm = activity.getApplicationContext().getResources().getDisplayMetrics();
            dWidth = dm.widthPixels;
            dHeight = dm.heightPixels / 2;
            dataArray = new ArrayList<>();
            ComicsData data;
            ArrayList<ComicsData> datas;

            try {
                for(int i = 0; i < saveData.size(); i++) {
                    document = Jsoup.connect(saveData.get(i).getLink()).timeout(0).post();
                    image = document.select("img");
                    title = document.select("title");

                    size = image.size();
                    int width = 0, height = 0;
                    Log.i("elementsSize", size + "");

                    datas = new ArrayList<>();
                    for (int j = 0; j < size; j++) {
                        if (image.get(j).attr("data-src").equals("")) {
                            continue;
                        }
                        try {
                            width = Integer.valueOf(image.get(i).attr("width"));
                            height = Integer.valueOf(image.get(i).attr("height"));
                        } catch (Exception e) {
                            width = dWidth;
                            height = dHeight;
                        }
                        height = (height * dWidth) / width;
                        width = dWidth;
                        data = new ComicsData();
                        data.setTitle(saveData.get(i).getTitle());
                        data.setImageName(j + ".jpg");
                        data.setImage(image.get(j).attr("data-src"));
                        datas.add(data);
                    }
                    dataArray.add(datas);
                }

                document = null;
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            dialog.dismiss();
            new Save().execute(dataArray);
        }
    }

    class Save extends AsyncTask<ArrayList<ArrayList<ComicsData>>, Void, Void>{
        private ArrayList<ArrayList<ComicsData>> data;
        private int max = 0;
        private NotificationManager nm;
        private Notification.Builder mBuilder;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder = new Notification.Builder(activity);
            mBuilder.setSmallIcon(R.drawable.ic_launcher);
            mBuilder.setTicker("다운로드중...");
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setContentTitle("마루뷰어");
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setAutoCancel(true);
            mBuilder.setOngoing(true);

        }

        @Override
        protected Void doInBackground(ArrayList<ArrayList<ComicsData>>... params) {
            data = params[0];

            for(int i = 0; i < data.size(); i++){
                max += data.get(i).size();
            }
            float increase = 100;
            increase = increase/max;

            Bitmap mBitmap = null;
            InputStream in = null;
            OutputStream outStream = null;
            int progress = 0;
            try {
                for(int i = 0; i < data.size(); i++) {
                    for(int j = 0; j < data.get(i).size(); j++) {
                        in = new java.net.URL(data.get(i).get(j).getImage()).openStream();
                        mBitmap = BitmapFactory.decodeStream(in);
                        in.close();

                        outStream = null;
                        String path = Environment.getExternalStorageDirectory().toString() + "/마루뷰어/" + data.get(i).get(j).getTitle();
                        File file = new File(path);
                        if( !file.exists() ){  // 원하는 경로에 폴더가 있는지 확인
                            file.mkdirs();
                        }

                        file = new File(path, data.get(i).get(j).getImageName());
                        outStream = new FileOutputStream(file);
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        mBuilder.setProgress(max, ++progress, false);
                        float percent = increase * progress;
                        mBuilder.setContentText("다운로드 : " + String.format("%.0f", percent) + "%");
                        nm.notify(19980313, mBuilder.build());
                    }
                }
            } catch (Exception ex) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            nm.cancel(19980313);
        }
    }
}
