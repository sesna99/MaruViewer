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
import ind.simsim.maruViewer.Service.ComicsData;
import ind.simsim.maruViewer.UI.Activity.ComicsEpisodeActivity;
import ind.simsim.maruViewer.UI.Adapter.ComicsListAdapter;

/**
 * Created by admin on 2016-02-18.
 */
public class SearchFragment extends Fragment {
    private ListView mComicsList;
    private String url;
    private ArrayList<ComicsData> comicsDatas;
    private ComicsListAdapter adapter;
    private Bundle bundle;
    private boolean isFirst = true;
    private SwipyRefreshLayout layout;

    public SearchFragment() {
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

        layout = (SwipyRefreshLayout) v.findViewById(R.id.loadlist);
        layout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                layout.setRefreshing(false);
            }
        });
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initList(View v){
        comicsDatas = new ArrayList<>();

        mComicsList = (ListView)v.findViewById(R.id.listView);
        adapter = new ComicsListAdapter(getActivity(), R.layout.list_item, comicsDatas);
        mComicsList.setAdapter(adapter);
        mComicsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ComicsEpisodeActivity.class);
                intent.putExtra("url", comicsDatas.get(position).getLink());
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
                Document document = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(0).get();
                Elements image = document.select("div span[class=thumb] img");
                Elements link = document.select("div[class=postbox] a");
                Elements title = document.select("div[class=sbjbox] b");

                int size = image.size();

                ComicsData data;
                for(int i = 0; i < size; i++){
                    data = new ComicsData();
                    data.setImage(image.get(i).attr("src"));
                    data.setLink(link.get(i).attr("href"));
                    data.setTitle(title.get(i).text());
                    comicsDatas.add(data);
                }

                document = null;
                image = null;
                link = null;
                title = null;
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            dialog.dismiss();
            adapter.setComicsData(comicsDatas);
            adapter.refresh();
        }
    }
}