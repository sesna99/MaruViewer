package ind.simsim.maruViewer.UI.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Service.PreferencesManager;

/**
 * Created by jack on 2016. 12. 16..
 */

public class SettingFragment extends PreferenceFragment {
    private Preference path, cache, lately, update;
    private PreferencesManager pm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        path = findPreference("path");
        cache = findPreference("cache");
        lately = findPreference("lately");
        update = findPreference("update");

        pm = PreferencesManager.getInstance(getActivity());

        path.setSummary(Environment.getExternalStorageDirectory().toString() + "/마루뷰어");
        cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clearApplicationCache(null);
                return false;
            }
        });

        lately.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int size = pm.getPosition("lately") - 1;
                for(int i = 0; i <= size;){
                    pm.deleteLately(i);
                    size = pm.getPosition("lately") - 1;
                }
                return false;
            }
        });

        update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("http://trycatch98.tistory.com/category/Project/MaruViewer");
                intent.setData(uri);
                startActivity(intent);
                return false;
            }
        });
    }

    private void clearApplicationCache(java.io.File dir){
        if(dir==null)
            dir = getActivity().getCacheDir();
        else;
        if(dir==null)
            return;
        else ;
        java.io.File[] children = dir.listFiles();
        try {
            for (int i = 0; i < children.length; i++)
                if (children[i].isDirectory())
                    clearApplicationCache(children[i]);
                else children[i].delete();
        } catch(Exception e){}
    }
}