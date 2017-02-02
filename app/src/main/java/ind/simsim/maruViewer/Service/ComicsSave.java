package ind.simsim.maruViewer.Service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import ind.simsim.maruViewer.Event.ComicsDownLoadEvent;
import ind.simsim.maruViewer.Model.ComicsData;
import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.UI.Adapter.SaveDialogListAdapter;

/**
 * Created by jack on 2016. 12. 8..
 */

public class ComicsSave {
    private Activity activity;
    private ArrayList<ArrayList<ComicsData>> saveComics;

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
                Intent intent = new Intent(activity, DownloadService.class);
                intent.putExtra("code", 1);
                activity.startService(intent);
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
                        if(content.get(i).text().equals(""))
                            continue;
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
        private ArrayList<ComicsData> saveData;
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
            saveComics = new ArrayList<>();
            ComicsData data;
            ArrayList<ComicsData> datas;

            try {
                for(int i = 0; i < saveData.size(); i++) {
                    document = Jsoup.connect(saveData.get(i).getLink()).timeout(0).post();
                    image = document.select("img");
                    title = document.select("title");

                    size = image.size();

                    datas = new ArrayList<>();
                    for (int j = 0; j < size; j++) {
                        if (image.get(j).attr("data-src").equals("")) {
                            continue;
                        }
                        data = new ComicsData();
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

            EventBus.getDefault().post(new ComicsDownLoadEvent(saveComics));
        }
    }
}
