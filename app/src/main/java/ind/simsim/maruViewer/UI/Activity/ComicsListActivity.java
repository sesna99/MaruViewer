package ind.simsim.maruViewer.UI.Activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.UI.Fragment.ComicsListFragment;
import ind.simsim.maruViewer.UI.Fragment.SearchFragment;

/**
 * Created by jack on 2016. 12. 14..
 */

public class ComicsListActivity extends BaseActivity {
    @BindView(R.id.back_button)
    ImageView back_button;

    @BindView(R.id.title_view)
    TextView title_view;

    private Fragment fragment;
    private Bundle bundle;
    private int position;
    private String category;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_list);

        ButterKnife.bind(this);

        intent = getIntent();
        position = intent.getIntExtra("position", 0);
        category = intent.getStringExtra("category");

        title_view.setText(category);

        back_button.setVisibility(View.VISIBLE);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initFragment();

    }

    private void initFragment(){
        if(position != -1) {
            String[] url = getResources().getStringArray(R.array.url);
            fragment = new ComicsListFragment();
            bundle = new Bundle();
            bundle.putString("url", url[position]);
            Log.i("url", url[position]);
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