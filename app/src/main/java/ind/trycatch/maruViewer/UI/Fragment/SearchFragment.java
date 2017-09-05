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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.Model.ComicsModel;
import ind.trycatch.maruViewer.UI.Adapter.ComicsListAdapter;

/**
 * Created by admin on 2016-02-18.
 */
public class SearchFragment extends Fragment {
    @BindView(R.id.comics_list)
    RecyclerView comics_list;

    private String url;
    private ArrayList<ComicsModel> comicsModels;
    private ComicsListAdapter adapter;
    private Bundle bundle;
    private boolean isFirst = true;

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
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        ButterKnife.bind(this, v);

        initList(v);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initList(View v){
        comicsModels = new ArrayList<>();

        adapter = new ComicsListAdapter(getActivity(), comicsModels);
        comics_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        comics_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        comics_list.setAdapter(adapter);

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

                ComicsModel data;
                for(int i = 0; i < size; i++){
                    data = new ComicsModel();
                    data.setImage(image.get(i).attr("src"));
                    data.setLink(link.get(i).attr("href"));
                    data.setTitle(title.get(i).text());
                    comicsModels.add(data);
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
            adapter.setComicsModel(comicsModels);
        }
    }
}
