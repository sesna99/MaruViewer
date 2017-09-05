package ind.trycatch.maruViewer.UI.Activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.UI.Fragment.ComicsListFragment;
import ind.trycatch.maruViewer.UI.Fragment.SearchFragment;

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
            String url;
            if(position < 15) {
                url = getResources().getStringArray(R.array.url)[position];
            }
            else{
                url = getResources().getString(R.string.genre_url) + category + "&p=";
            }
            fragment = new ComicsListFragment();
            bundle = new Bundle();
            bundle.putString("url", url);
            Log.i("url", url);
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