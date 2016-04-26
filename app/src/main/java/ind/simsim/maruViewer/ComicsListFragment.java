package ind.simsim.maruViewer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by admin on 2016-02-18.
 */
public class ComicsListFragment extends Fragment{
    private ListView mComicsList;
    private String url;
    private ArrayList<String> arrayImage;
    private ArrayList<String> arrayLink;
    private ArrayList<String> arrayTitle;
    private ComicsListAdapter adapter;
    private View footer;
    private int order = 1;
    private Bundle bundle;

    public ComicsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        url = bundle.getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_fragment, container, false);
        initList(v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        order = 1;
    }

    private void initList(View v){
        arrayImage = new ArrayList<>();
        arrayLink = new ArrayList<>();
        arrayTitle = new ArrayList<>();

        mComicsList = (ListView)v.findViewById(R.id.listView);
        adapter = new ComicsListAdapter(getActivity(), R.layout.list_item, arrayTitle, arrayImage);
        footer = getActivity().getLayoutInflater().inflate(R.layout.list_footer, null, false);
        footer.findViewById(R.id.footer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ComicsList().execute();
            }
        });

        mComicsList.addFooterView(footer);
        mComicsList.setAdapter(adapter);
        mComicsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ComicsEpisodeActivity.class);
                intent.putExtra("url", arrayLink.get(position));
                startActivity(intent);
            }
        });

        new ComicsList().execute();
    }

    class ComicsList extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle("Load");
            dialog.setMessage("더 불러오는중..");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document document = Jsoup.connect(url + order++).timeout(0).get();
                Elements image = document.select("div[class=image-thumb]");
                Elements link = document.select("div div[class=list]");
                Elements title = document.select("div div[class=sbj] span[class=subject]");

                String temp;

                int size = image.size();
                Log.i("elementsSize", size + "");
                for(int i = 0; i < size; i++){
                    temp = image.get(i).attr("style");
                    if(temp.contains("http://marumaru.in"))
                        arrayImage.add(temp.substring(21, temp.length()-1));
                    else
                        arrayImage.add("http://marumaru.in" + temp.substring(21, temp.length()-1));
                    temp = link.get(i).attr("onclick");
                    arrayLink.add(temp.substring(8, temp.length()-3));
                    arrayTitle.add(title.get(i).text());
                }

                document = null;
                image = null;
                link = null;
                title = null;
                temp = null;
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            dialog.dismiss();
            adapter.addTitleArray(arrayTitle);
            adapter.addImageArray(arrayImage);
            adapter.refresh();
        }
    }
}
