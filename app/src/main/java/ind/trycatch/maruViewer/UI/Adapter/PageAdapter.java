package ind.trycatch.maruViewer.UI.Adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.UI.Fragment.ComicsLatelyFragment;
import ind.trycatch.maruViewer.UI.Fragment.ComicsListFragment;
import ind.trycatch.maruViewer.UI.Fragment.ComicsSaveListFragment;
import ind.trycatch.maruViewer.UI.Fragment.FavoritesFragment;
import ind.trycatch.maruViewer.UI.Fragment.SettingFragment;

/**
 * Created by jack on 2016. 12. 14..
 */

public class PageAdapter extends FragmentStatePagerAdapter {
    private Context mContext;
    private Bundle bundle;
    private int tabSize;
    private ComicsListFragment comicsList;
    private ComicsSaveListFragment saveList;
    private FavoritesFragment favorites;
    private ComicsLatelyFragment lately;
    private SettingFragment setting;

    public PageAdapter(Context context, FragmentManager fm, int tabSize) {
        super(fm);
        mContext = context;
        this.tabSize = tabSize;
    }

    @Override
    public Fragment getItem(int position) {
        if(comicsList == null){
            String[] url = mContext.getResources().getStringArray(R.array.url);
            bundle = new Bundle();
            bundle.putString("url", url[1]);
            comicsList = new ComicsListFragment();
            comicsList.setArguments(bundle);
            saveList = new ComicsSaveListFragment();
            favorites = new FavoritesFragment();
            lately = new ComicsLatelyFragment();
            setting = new SettingFragment();
        }
        switch (position){
            case 0:
                return comicsList;
            case 1:
                return saveList;
            case 2:
                return favorites;
            case 3:
                return lately;
            case 4:
                return setting;
        }
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if(position != 0)
            super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return tabSize;
    }
}