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

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.Event.DownLoadCompleteEvent;
import ind.trycatch.maruViewer.Event.DownLoadRemoveEvent;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.Model.ComicsModel;
import ind.trycatch.maruViewer.Service.PreferencesManager;
import ind.trycatch.maruViewer.UI.Adapter.ComicsSaveListAdapter;

/**
 * Created by jack on 2016. 12. 15..
 */

public class ComicsSaveListFragment extends Fragment {
    @BindView(R.id.comics_list)
    RecyclerView comics_list;

    private ComicsSaveListAdapter adapter;
    private ArrayList<ArrayList<String>> comicsData;
    private ArrayList<ComicsModel> folderList;
    private boolean isFirst = true;

    public ComicsSaveListFragment() {
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

    private void initList(View v){
        folderList = getFolderList();
        setComicsData(folderList);
        adapter = new ComicsSaveListAdapter(getActivity(), setComicsThum(folderList), folderList, comicsData);
        comics_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        comics_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        comics_list.setAdapter(adapter);
    }

    private ArrayList<ComicsModel> getFolderList() {
        try {
            ArrayList<ComicsModel> folderList = new ArrayList<>();
            String path = PreferencesManager.getInstance(getActivity()).getDownLoadDirectory();
            File file = new File(path);
            if(file.exists()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if(files[i].listFiles().length == 0)
                        continue;
                    folderList.add(0, new ComicsModel(files[i].getName(), files[i].getAbsolutePath()));
                }
            }
            return folderList;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<ComicsModel> setComicsThum(ArrayList<ComicsModel> folderList){
        try {
            String path;
            File file;
            File[] files;
            for(int i = 0;i < folderList.size();i++) {
                path = folderList.get(i).getLink();
                file = new File(path);
                files = file.listFiles();
                folderList.get(i).setImage(files.length > 0 ? files[0].getAbsolutePath() : "");
            }
            return folderList;
        } catch(Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    private void setComicsData(ArrayList<ComicsModel> folderList){
        comicsData = new ArrayList<>();
        try {
            String path;
            File file;
            File[] files;
            ArrayList<String> image;
            for(int i = 0;i < folderList.size();i++) {
                path = folderList.get(i).getLink();
                file = new File(path);
                files = file.listFiles();
                image = new ArrayList<>();
                for(int j = 0; j < files.length; j++)
                    image.add(files[j].getAbsolutePath());
                comicsData.add(image);
            }
        } catch(Exception e ) {
            e.printStackTrace();
        }
    }

    public void refresh(){
        if(!isFirst) {
            folderList = getFolderList();
            setComicsData(folderList);
            setComicsThum(folderList);
            adapter.setComicsModel(folderList);
        }
    }

    @Subscribe
    public void onDownLoadCompleteEvent(DownLoadCompleteEvent event){
        if(event.isSucceed())
            refresh();
    }

    @Subscribe
    public void onDownLoadRemoveEvent(DownLoadRemoveEvent event){
        if(event.isSucceed())
            refresh();
    }
}