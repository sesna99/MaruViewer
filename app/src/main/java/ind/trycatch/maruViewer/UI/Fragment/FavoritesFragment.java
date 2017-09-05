package ind.trycatch.maruViewer.UI.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.Event.FavoriteEvent;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.Model.ComicsModel;
import ind.trycatch.maruViewer.Service.PreferencesManager;
import ind.trycatch.maruViewer.UI.Adapter.FavoritesListAdapter;

/**
 * Created by jack on 2016. 12. 16..
 */

public class FavoritesFragment extends Fragment {
    @BindView(R.id.comics_list)
    RecyclerView comics_list;

    private FavoritesListAdapter adapter;
    private ArrayList<ComicsModel> comicsModel;
    private PreferencesManager pm;
    private boolean isFirst = true;

    public FavoritesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        ButterKnife.bind(this, v);

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

    private void initList(View v) {
        pm = PreferencesManager.getInstance(getActivity());

        comicsModel = new ArrayList<>();

        adapter = new FavoritesListAdapter(getActivity(), new ArrayList<ComicsModel>());

        comics_list.setAdapter(adapter);
        comics_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        comics_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        comicsModel = pm.getFavorites();
        adapter.setComicsModel(comicsModel);
    }

    public void refresh(){
        if(!isFirst) {
            comicsModel = pm.getFavorites();
            adapter.setComicsModel(comicsModel);
        }
    }

    @Subscribe
    public void onFavoriteEvent(FavoriteEvent event){
        Log.i("event", event.isSucceed()+"");
        if(event.isSucceed())
            refresh();
    }

}