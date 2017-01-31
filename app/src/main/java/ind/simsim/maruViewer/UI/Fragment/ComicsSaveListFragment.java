package ind.simsim.maruViewer.UI.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;

import ind.simsim.maruViewer.Event.DownLoadEvent;
import ind.simsim.maruViewer.Event.DownLoadRemoveEvent;
import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Model.ComicsData;
import ind.simsim.maruViewer.Service.PreferencesManager;
import ind.simsim.maruViewer.UI.Activity.ComicsViewer;
import ind.simsim.maruViewer.UI.Adapter.ComicsListAdapter;

/**
 * Created by jack on 2016. 12. 15..
 */

public class ComicsSaveListFragment extends Fragment {
    private ListView mComicsList;
    private ComicsListAdapter adapter;
    private ArrayList<ArrayList<String>> comicsData;
    private ArrayList<ComicsData> folderList;
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
        mComicsList = (ListView)v.findViewById(R.id.listView);
        adapter = new ComicsListAdapter(getActivity(), R.layout.fragment_list_item, new ArrayList<ComicsData>());


        mComicsList.setAdapter(adapter);
        mComicsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ComicsViewer.class);
                intent.putExtra("image", comicsData.get(position));
                intent.putExtra("title", folderList.get(position).getTitle());
                intent.putExtra("comicsUrl", "");
                intent.putExtra("episodeUrl", "");
                startActivity(intent);
                Log.i("path", comicsData.get(position).get(0));
            }
        });

        folderList = getFolderList();
        setComicsData(folderList);
        adapter.setComicsData(setComicsThum(folderList));
    }

    private ArrayList<ComicsData> getFolderList() {
        try {
            ArrayList<ComicsData> folderList = new ArrayList<>();
            String path = PreferencesManager.getInstance(getActivity()).getDownLoadDirectory();
            File file = new File(path);
            if(file.exists()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    folderList.add(0, new ComicsData(files[i].getName(), files[i].getAbsolutePath()));
                }
            }
            return folderList;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<ComicsData> setComicsThum(ArrayList<ComicsData> folderList){
        try {
            String path;
            File file;
            File[] files;
            for(int i = 0;i < folderList.size();i++) {
                path = folderList.get(i).getLink();
                file = new File(path);
                files = file.listFiles();
                folderList.get(i).setImage(files[0].getAbsolutePath());
            }
            return folderList;
        } catch(Exception e ) {
            return null;
        }
    }

    private void setComicsData(ArrayList<ComicsData> folderList){
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
        }
    }

    public void refresh(){
        if(!isFirst) {
            folderList = getFolderList();
            setComicsData(folderList);
            setComicsThum(folderList);
            adapter.setComicsData(folderList);
            adapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onDownLoadEvent(DownLoadEvent event){
        if(event.isSucceed())
            refresh();
    }

    @Subscribe
    public void onDownLoadRemoveEvent(DownLoadRemoveEvent event){
        if(event.isSucceed())
            refresh();
    }
}