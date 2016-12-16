package ind.simsim.maruViewer.UI.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;

import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Service.ComicsData;
import ind.simsim.maruViewer.Service.PreferencesManager;
import ind.simsim.maruViewer.UI.Activity.ComicsViewer;
import ind.simsim.maruViewer.UI.Adapter.ComicsListAdapter;

/**
 * Created by jack on 2016. 12. 16..
 */
public class ComicsLatelyFragment extends Fragment {
    private ListView mComicsList;
    private ComicsListAdapter adapter;
    private ArrayList<ComicsData> comicsData;
    private SwipyRefreshLayout refreshLayout;
    private PreferencesManager pm;
    private boolean isFirst = true;

    public ComicsLatelyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_fragment, container, false);
        initList(v);

        refreshLayout = (SwipyRefreshLayout) v.findViewById(R.id.loadlist);
        refreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                comicsData = pm.getLately();
                adapter.setComicsData(comicsData);
                refreshLayout.setRefreshing(false);
            }
        });

        isFirst = false;
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initList(View v){
        pm = PreferencesManager.getInstance(getActivity());

        comicsData = new ArrayList<>();

        mComicsList = (ListView)v.findViewById(R.id.listView);
        adapter = new ComicsListAdapter(getActivity(), R.layout.list_item, new ArrayList<ComicsData>());

        mComicsList.setAdapter(adapter);
        mComicsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                intent = new Intent(getActivity(), ComicsViewer.class);
                intent.putExtra("comicsUrl", comicsData.get(position).getComicsUrl());
                intent.putExtra("episodeUrl", comicsData.get(position).getEpisodeUrl());
                intent.putExtra("scroll", pm.getScroll(pm.getLatelyPosition(comicsData.get(position).getComicsUrl())));
                intent.putExtra("title", comicsData.get(position).getTitle());
                startActivity(intent);
            }
        });

        comicsData = pm.getLately();
        adapter.setComicsData(comicsData);
    }

    public void refresh(){
        if(!isFirst) {
            comicsData = pm.getLately();
            adapter.setComicsData(comicsData);
        }
    }

}
