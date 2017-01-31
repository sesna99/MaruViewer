package ind.simsim.maruViewer.UI.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Service.PreferencesManager;

/**
 * Created by jack on 2017. 1. 24..
 */

public class SettingFragment extends Fragment {
    @BindView(R.id.download_directory)
    LinearLayout download_directory;

    @BindView(R.id.download_directory_text)
    TextView download_directory_text;

    @BindView(R.id.clear_cache)
    TextView clear_cache;

    @BindView(R.id.clear_lately)
    TextView clear_lately;

    @BindView(R.id.update)
    LinearLayout update;

    @BindView(R.id.update_text)
    TextView update_text;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, null);

        ButterKnife.bind(this, v);

        init();

        return v;
    }

    public void init(){
        download_directory_text.setText(PreferencesManager.getInstance(getActivity()).getDownLoadDirectory());
        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        String versionName = pInfo.versionName;
        update_text.setText("현재 버전 : " + versionName);

        clear_cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearApplicationCache(null);
            }
        });

        clear_lately.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = PreferencesManager.getInstance(getActivity()).getPosition("lately") - 1;
                for(int i = 0; i <= size;){
                    PreferencesManager.getInstance(getActivity()).deleteLately(i);
                    size = PreferencesManager.getInstance(getActivity()).getPosition("lately") - 1;
                }
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://trycatch98.tistory.com/category/Project/MaruViewer"));
                startActivity(intent);
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