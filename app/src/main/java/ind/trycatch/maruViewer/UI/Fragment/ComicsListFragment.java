package ind.trycatch.maruViewer.UI.Fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.Model.ComicsModel;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.UI.Adapter.ComicsListAdapter;

/**
 * Created by admin on 2016-02-18.
 */
public class ComicsListFragment extends Fragment {
    @BindView(R.id.comics_list)
    RecyclerView comics_list;

    @BindView(R.id.load_list)
    SwipyRefreshLayout load_list;

    private String url;
    private ArrayList<ComicsModel> comicsModel;
    private ComicsListAdapter adapter;
    private int order = 1;
    private Bundle bundle;
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

        ButterKnife.bind(this, v);

        initList(v);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        order = 1;
    }

    private void initList(View v) {
        adapter = new ComicsListAdapter(getActivity(), new ArrayList<ComicsModel>());

        load_list.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (swipyRefreshLayoutDirection == SwipyRefreshLayoutDirection.TOP) {
                    order = 1;
                    comicsModel = new ArrayList<>();
                }
                new ComicsList().execute();
            }
        });

        comics_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        comics_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        comics_list.setAdapter(adapter);
    }

    public void initListData() {
        comicsModel = new ArrayList<>();
        isFirst = true;
        new ComicsList().execute();
    }

    class ComicsList extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isFirst) {
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
                String reUrl = Jsoup.connect(url + order++).timeout(0).followRedirects(true).execute().url().toExternalForm();
                Document document = Jsoup.connect(reUrl).timeout(0).get();
                Elements image = document.select("div[class=image-thumb]");
                Elements link = document.select("div div[class=list]");
                Elements title = document.select("div div[class=sbj] span[class=subject]");

                String temp;

                int size = image.size();
                ComicsModel data;
                for (int i = 0; i < size; i++) {
                    temp = image.get(i).attr("style");
                    data = new ComicsModel();
                    if (temp.contains("http://marumaru.in"))
                        data.setImage(temp.substring(21, temp.length() - 1));
                    else
                        data.setImage("http://marumaru.in" + temp.substring(21, temp.length() - 1));
                    temp = link.get(i).attr("onclick");
                    data.setLink(temp.substring(8, temp.length() - 3));
                    data.setTitle(title.get(i).text());
                    comicsModel.add(data);
                }

                document = null;
                image = null;
                link = null;
                title = null;
                temp = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            if (isFirst) {
                dialog.dismiss();
                isFirst = false;
            }
            adapter.setComicsModel(comicsModel);
            load_list.setRefreshing(false);
        }
    }
}
