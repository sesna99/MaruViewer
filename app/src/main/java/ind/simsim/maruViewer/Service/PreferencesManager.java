package ind.simsim.maruViewer.Service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;

import ind.simsim.maruViewer.Event.FavoriteEvent;
import ind.simsim.maruViewer.Event.LatelyEvent;
import ind.simsim.maruViewer.Model.ComicsData;

/**
 * Created by jack on 2016. 12. 15..
 */

public class PreferencesManager {
    public static PreferencesManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private PreferencesManager(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public void setFavorites(ComicsData data, String code){
        int position = getPosition("favorites");
        editor.putString("code" + position, code);
        editor.putString("favoritesTitle" + position, data.getTitle());
        editor.putString("favoritesComicsUrl" + position, data.getComicsUrl());
        editor.putString("favoritesEpisodeUrl" + position, data.getEpisodeUrl());
        editor.putString("favoritesImage" + position, data.getImage());
        editor.commit();
        setPosition("favorites");
        EventBus.getDefault().post(new FavoriteEvent(true));
    }

    public void deleteFavorites(int position){
        ArrayList<ComicsData> data = getFavorites();
        for(int i = position; i < data.size() - 1; i++){
            editor.putString("code" + i, getCode(i + 1));
            editor.putString("favoritesTitle" + i, data.get(i + 1).getTitle());
            editor.putString("favoritesComicsUrl" + i, data.get(i + 1).getComicsUrl());
            editor.putString("favoritesEpisodeUrl" + i, data.get(i + 1).getEpisodeUrl());
            editor.putString("favoritesImage" + i, data.get(i + 1).getImage());
        }

        int size = getPosition("favorites");
        editor.remove("code" + (size - 1));
        editor.remove("favoritesTitle" + (size - 1));
        editor.remove("favoritesComicsUrl" + (size - 1));
        editor.remove("favoritesEpisodeUrl" + (size - 1));
        editor.remove("favoritesImage" + (size - 1));

        setPosition("favorites", size - 1);

        EventBus.getDefault().post(new FavoriteEvent(true));
    }

    public ArrayList<ComicsData> getFavorites(){
        int position = getPosition("favorites");
        ArrayList<ComicsData> comicsDatas = new ArrayList<>();
        ComicsData data;
        for(int i = 0; i < position; i++){
            data = new ComicsData();
            data.setTitle(sharedPreferences.getString("favoritesTitle" + i, ""));
            data.setComicsUrl(sharedPreferences.getString("favoritesComicsUrl" + i, ""));
            data.setEpisodeUrl(sharedPreferences.getString("favoritesEpisodeUrl" + i, ""));
            data.setImage(sharedPreferences.getString("favoritesImage" + i, ""));
            comicsDatas.add(data);
        }
        return comicsDatas;
    }

    public boolean searchFavorites(String code, String url){
        ArrayList<ComicsData> data = getFavorites();
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).getComicsUrl().equals(url))
                return true;
            else if(code.equals("e") && data.get(i).getEpisodeUrl().equals(url))
                return true;
        }
        return false;
    }

    public int getFavoritesPosition(String code, String url){
        ArrayList<ComicsData> data = getFavorites();
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).getComicsUrl().equals(url))
                return i;
            else if(code.equals("e") && data.get(i).getEpisodeUrl().equals(url))
                return i;
        }
        return -1;
    }

    public void setLately(ComicsData data, int scroll){
        int position = getPosition("lately");
        editor.putString("latelyTitle" + position, data.getTitle());
        editor.putString("latelyComicsUrl" + position, data.getComicsUrl());
        editor.putString("latelyEpisodeUrl" + position, data.getEpisodeUrl());
        editor.putString("latelyImage" + position, data.getImage());
        editor.putInt("scroll" + position, scroll);
        editor.commit();
        setPosition("lately");

        EventBus.getDefault().post(new LatelyEvent(true));
    }

    public void updateLately(int position, int scroll){
        editor.putInt("scroll" + position, scroll);
        editor.commit();
    }

    public void deleteLately(int position){
        ArrayList<ComicsData> data = getLately();
        for(int i = position; i < data.size() - 1; i++){
            editor.putString("latelyTitle" + i, data.get(i + 1).getTitle());
            editor.putString("latelyComicsUrl" + i, data.get(i + 1).getComicsUrl());
            editor.putString("latelyEpisodeUrl" + i, data.get(i + 1).getEpisodeUrl());
            editor.putString("latelyImage" + i, data.get(i + 1).getImage());
            editor.putInt("scroll" + i, getScroll(i + 1));
        }

        int size = getPosition("lately");
        editor.remove("latelyTitle" + (size - 1));
        editor.remove("latelyComicsUrl" + (size - 1));
        editor.remove("latelyEpisodeUrl" + (size - 1));
        editor.remove("latelyImage" + (size - 1));
        editor.remove("scroll" + (size - 1));

        setPosition("lately", size - 1);

        EventBus.getDefault().post(new LatelyEvent(true));
    }

    public ArrayList<ComicsData> getLately(){
        int position = getPosition("lately");
        ArrayList<ComicsData> comicsDatas = new ArrayList<>();
        ComicsData data;
        for(int i = 0; i < position; i++){
            data = new ComicsData();
            data.setTitle(sharedPreferences.getString("latelyTitle" + i, ""));
            data.setComicsUrl(sharedPreferences.getString("latelyComicsUrl" + i, ""));
            data.setEpisodeUrl(sharedPreferences.getString("latelyEpisodeUrl" + i, ""));
            data.setImage(sharedPreferences.getString("latelyImage" + i, ""));
            comicsDatas.add(data);
        }
        for(int i = 0; i < comicsDatas.size() / 2; i++){
            Collections.swap(comicsDatas, i, comicsDatas.size() - i - 1);
        }
        return comicsDatas;
    }

    public boolean searchLately(String url){
        ArrayList<ComicsData> data = getLately();
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).getComicsUrl().equals(url))
                return true;
        }
        return false;
    }

    public int getLatelyPosition(String url){
        ArrayList<ComicsData> data = getLately();
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).getComicsUrl().equals(url))
                return i;
        }
        return -1;
    }

    public int getScroll(int position){
        return sharedPreferences.getInt("scroll" + position, 0);
    }

    public void setPosition(String key){
        int position = getPosition(key);
        editor.putInt(key, position + 1);
        editor.commit();
    }

    public void setPosition(String key, int position){
        if(position < 0)
            position = 0;
        editor.putInt(key, position);
        editor.commit();
    }

    public int getPosition(String key){
        return sharedPreferences.getInt(key, 0);
    }

    public String getCode(int position){
        return sharedPreferences.getString("code" + position, "");
    }

    public void setDownLoadDirectory(String directory){
        editor.putString("directory", directory);
        editor.commit();
    }

    public String getDownLoadDirectory(){
        return sharedPreferences.getString("directory", Environment.getExternalStorageDirectory().toString() + "/마루뷰어/");
    }

    public static PreferencesManager getInstance(Context context){
        if(instance == null){
            instance = new PreferencesManager(context);
        }

        return instance;
    }
}
