package ind.simsim.maruViewer.UI.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.simsim.maruViewer.Event.UpdateEvent;
import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Model.TabModel;
import ind.simsim.maruViewer.Service.UpdateCheck;
import ind.simsim.maruViewer.UI.Adapter.PageAdapter;


public class MainActivity extends BaseActivity {
    @BindView(R.id.navigation_menu)
    ImageView navigation_menu;

    @BindView(R.id.title_view)
    TextView title_view;

    @BindView(R.id.search_view)
    ImageView search_view;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer_layout;

    @BindView(R.id.navigation_view)
    NavigationView navigation_view;

    @BindView(R.id.container)
    ViewPager container;

    private PageAdapter adapter;
    private CommonTabLayout tabLayout;
    private ArrayList<CustomTabEntity> tabEntities;
    private String[] title = {"업데이트", "다운로드", "즐겨찾기", "최근본만화", "설정"};
    private int[] selectImg = {R.drawable.new1, R.drawable.save1, R.drawable.star1, R.drawable.lately1, R.drawable.settings1};
    private int[] unselectImg = {R.drawable.new2, R.drawable.save2, R.drawable.star2, R.drawable.lately2, R.drawable.settings2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        MobileAds.initialize(this, getResources().getString(R.string.app_id));

        EventBus.getDefault().register(this);

        new UpdateCheck(this).check();
    }

    private void init() {
        setNavigation();

        title_view.setText(title[0]);

        search_view.setVisibility(View.VISIBLE);
        search_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View v = getLayoutInflater().inflate(R.layout.edit_dialog, null);
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setView(v)
                        .setTitle("검색")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(MainActivity.this, ComicsListActivity.class);
                                intent.putExtra("position", -1);
                                intent.putExtra("url", getString(R.string.search, ((EditText)v.findViewById(R.id.edit_text)).getText().toString()));
                                intent.putExtra("category", "검색");
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                dialog.show();
            }
        });

        adapter = new PageAdapter(getApplicationContext(), getFragmentManager(), 5);
        container = (ViewPager) findViewById(R.id.container);
        container.setAdapter(adapter);

        tabEntities = new ArrayList<>();
        for (int i = 0; i < selectImg.length; i++)
            tabEntities.add(new TabModel(title[i], selectImg[i], unselectImg[i]));

        tabLayout = (CommonTabLayout) findViewById(R.id.tabs);
        tabLayout.setTabData(tabEntities);

        container.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
                title_view.setText(title[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                container.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }

    private void checkPermission(){
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                init();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "권한을 허용해주세요.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };

        new TedPermission(getApplicationContext())
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("이 서비스를 이용할려면 권한이 필요합니다.\n\n권한을 허용해주세요. [설정] > [권한]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private void setNavigation(){
        navigation_menu.setVisibility(View.VISIBLE);
        navigation_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer_layout.openDrawer(Gravity.LEFT);
            }
        });

        View headerView = navigation_view.getHeaderView(0);
        View headerItem = getLayoutInflater().inflate(R.layout.drawer_list_item, null);
        TextView list_text = (TextView) headerItem.findViewById(R.id.list_text);
        LinearLayout category_comics = (LinearLayout)headerView.findViewById(R.id.category_comics);
        LinearLayout category_genre = (LinearLayout)headerView.findViewById(R.id.category_genre);
        final String[] comics = getResources().getStringArray(R.array.category_comics);
        final String[] genre = getResources().getStringArray(R.array.category_genre);

        for(int i = 0; i < comics.length; i++){
            final int position = i;
            headerItem = getLayoutInflater().inflate(R.layout.drawer_list_item, null);
            list_text = (TextView) headerItem.findViewById(R.id.list_text);
            list_text.setText(comics[position]);
            list_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectItem(position, comics[position], true);
                }
            });
            category_comics.addView(headerItem);
        }

        for(int i = 0; i < genre.length; i++){
            final int position = i;
            headerItem = getLayoutInflater().inflate(R.layout.drawer_list_item, null);
            list_text = (TextView) headerItem.findViewById(R.id.list_text);
            list_text.setText(genre[position]);
            list_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectItem(position, genre[position], false);
                }
            });
            category_genre.addView(headerItem);
        }
    }

    private void selectItem(int position, String category, boolean isComics) {
        drawer_layout.closeDrawers();

        if (!isComics) {
            Intent intent = new Intent(this, ComicsListActivity.class);
            intent.putExtra("category", category);
            intent.putExtra("position", position + 15);
            startActivity(intent);
        } else {
            if (position != 1) {
                Intent intent = new Intent(this, ComicsListActivity.class);
                intent.putExtra("category", category);
                intent.putExtra("position", position);
                startActivity(intent);
            } else {
                container.setCurrentItem(0);
                tabLayout.setCurrentTab(0);
            }
        }
    }

    @Subscribe
    public void onUpdateEvent(final UpdateEvent event){
        if(!event.isUpdate()){
            Toast.makeText(this, "최신 버전입니다.", Toast.LENGTH_SHORT).show();
            checkPermission();
        }

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(Gravity.LEFT)) {
            drawer_layout.closeDrawers();
        }
        else {
            new AlertDialog.Builder(this).setTitle("종료").setMessage("종료하시겠습니까?").setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setNegativeButton("아니오", null).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
