package ind.simsim.maruViewer.UI.Fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Model.ComicsData;
import ind.simsim.maruViewer.UI.Activity.ComicsEpisodeActivity;
import ind.simsim.maruViewer.UI.Adapter.ComicsListAdapter;

/**
 * Created by admin on 2016-02-18.
 */
public class ComicsListFragment extends Fragment {
    private ListView mComicsList;
    private String url;
    private ArrayList<ComicsData> comicsData;
    private ComicsListAdapter adapter;
    private int order = 1;
    private Bundle bundle;
    private SwipyRefreshLayout load;
    private boolean isFirst;
    private float oldY = 0, curY = 0;

    public ComicsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        url = bundle.getString("url");
        initListData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_refresh_list, container, false);
        initList(v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        order = 1;
    }

    private void initList(View v){
        mComicsList = (ListView)v.findViewById(R.id.listView);
        adapter = new ComicsListAdapter(getActivity(), R.layout.fragment_list_item, new ArrayList<ComicsData>());

        load = (SwipyRefreshLayout)v.findViewById(R.id.loadlist);
        load.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if(swipyRefreshLayoutDirection == SwipyRefreshLayoutDirection.TOP) {
                    order = 1;
                    comicsData = new ArrayList<>();
                }
                new ComicsList().execute();
            }
        });

        mComicsList.setAdapter(adapter);
        mComicsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ComicsEpisodeActivity.class);
                intent.putExtra("url", comicsData.get(position).getLink());
                intent.putExtra("title", comicsData.get(position).getTitle());
                startActivity(intent);
            }
        });

      /*mComicsList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        if(oldY == 0) {
                            oldY = motionEvent.getRawY();
                        }
                        curY = motionEvent.getRawY();
                        if(curY > oldY)
                            load.setDirection(SwipyRefreshLayoutDirection.TOP);
                        else
                            load.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
                        break;
                    case MotionEvent.ACTION_UP:
                        oldY = 0;
                        curY = 0;
                        break;
                }
                return false;
            }
        });*/


    }

    public void initListData(){
        comicsData = new ArrayList<>();
        isFirst = true;
        new ComicsList().execute();
    }

    class ComicsList extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(isFirst) {
                dialog = new ProgressDialog(getActivity());
                dialog.setTitle("Load");
                dialog.setMessage("리스트 생성중..");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
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
                ComicsData data;
                for(int i = 0; i < size; i++){
                    temp = image.get(i).attr("style");
                    data = new ComicsData();
                    if(temp.contains("http://marumaru.in"))
                        data.setImage(temp.substring(21, temp.length()-1));
                    else
                        data.setImage("http://marumaru.in" + temp.substring(21, temp.length()-1));
                    temp = link.get(i).attr("onclick");
                    data.setLink(temp.substring(8, temp.length()-3));
                    data.setTitle(title.get(i).text());
                    comicsData.add(data);
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
            if(isFirst) {
                dialog.dismiss();
                isFirst = false;
            }
            adapter.setComicsData(comicsData);
            adapter.refresh();
            load.setRefreshing(false);
        }
    }
}
