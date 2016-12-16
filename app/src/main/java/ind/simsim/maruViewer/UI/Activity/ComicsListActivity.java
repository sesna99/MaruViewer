package ind.simsim.maruViewer.UI.Activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Service.ApplicationController;
import ind.simsim.maruViewer.UI.Fragment.ComicsListFragment;
import ind.simsim.maruViewer.UI.Fragment.SearchFragment;

/**
 * Created by jack on 2016. 12. 14..
 */

public class ComicsListActivity extends Activity {
    private Fragment fragment;
    private Bundle bundle;
    private int position;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_list);
        ActionBar ab = getActionBar();
        ab.setDisplayShowTitleEnabled(false);

        intent = getIntent();
        position = intent.getIntExtra("position", 0);

        initFragment();

        Tracker t = ((ApplicationController)getApplication()).getTracker(ApplicationController.TrackerName.APP_TRACKER);
        t.setScreenName("ComicsListActivity");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    private void initFragment(){
        if(position != -1) {
            String[] url = getResources().getStringArray(R.array.url);
            fragment = new ComicsListFragment();
            bundle = new Bundle();
            bundle.putString("url", url[position]);
            fragment.setArguments(bundle);
        }
        else{
            fragment = new SearchFragment();
            bundle = new Bundle();
            bundle.putString("url", intent.getStringExtra("url"));
            fragment.setArguments(bundle);
        }


        fragment.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}