package ind.simsim.maruViewer.UI.Activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import ind.simsim.maruViewer.R;
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

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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