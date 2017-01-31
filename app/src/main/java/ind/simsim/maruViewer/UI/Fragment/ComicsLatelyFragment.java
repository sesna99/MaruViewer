package ind.simsim.maruViewer.UI.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import ind.simsim.maruViewer.Event.LatelyEvent;
import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Model.ComicsData;
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
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        initList(v);

        isFirst = false;

        EventBus.getDefault().register(this);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
    }

    private void initList(View v){
        pm = PreferencesManager.getInstance(getActivity());

        comicsData = new ArrayList<>();

        mComicsList = (ListView)v.findViewById(R.id.listView);
        adapter = new ComicsListAdapter(getActivity(), R.layout.fragment_list_item, new ArrayList<ComicsData>());

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
            adapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onLatelyEvent(LatelyEvent event){
        if(event.isSucceed())
            refresh();
    }

}
