package ind.simsim.maruViewer.UI.Activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;

import java.util.ArrayList;

import ind.simsim.maruViewer.R;
import ind.simsim.maruViewer.Service.ApplicationController;
import ind.simsim.maruViewer.Service.TabEntity;
import ind.simsim.maruViewer.UI.Adapter.DrawerListAdapter;
import ind.simsim.maruViewer.UI.Adapter.PageAdapter;


public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private MenuItem searchMenu;
    private ViewPager mViewPager;
    private PageAdapter adapter;
    private ActionBar actionBar;
    private CommonTabLayout tabLayout;
    private ArrayList<CustomTabEntity> tabEntities;
    private String[] title = {"업데이트", "다운로드", "즐겨찾기", "최근본만화", "설정"};
    private int[] selectImg = {R.drawable.new1, R.drawable.save1, R.drawable.star1, R.drawable.lately1, R.drawable.settings1};
    private int[] unselectImg = {R.drawable.new2, R.drawable.save2, R.drawable.star2, R.drawable.lately2, R.drawable.settings2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        initDrawer();
        init();

        Tracker t = ((ApplicationController)getApplication()).getTracker(ApplicationController.TrackerName.APP_TRACKER);
        t.setScreenName("MainAcitivty");
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

    private void initDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);

        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                drawerArrow, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // create our list and custom adapter
        DrawerListAdapter adapter = new DrawerListAdapter(this);
        adapter.addSection("만화", new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, R.id.text1,
                new String[]{"전체보기", "업데이트 알림", "주간", "격주", "월간", "격월/비정기", "단행본", "완결", "단편", "붕탁", "와이!", "오토코노코 엔솔로지", "여장소년 엔솔로지", "오토코노코타임", "붕탁 완결"}));
        adapter.addSection("장르", new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, R.id.text1,
                new String[]{"17", "SF", "TS", "개그", "드라마", "러브코미디", "먹방", "백합", "붕탁", "순정", "스릴러", "스포츠", "시대", "액션", "일상 치유", "추리", "판타지", "학원", "호러"}));

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        mDrawerToggle.setAnimateEnabled(false);
                        drawerArrow.setProgress(1f);
                        break;
                    default:
                        selectItem(position);
                }

            }
        });
    }

    private void init(){
        initDrawer();
        adapter = new PageAdapter(getApplicationContext(), getFragmentManager(), 5);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(adapter);

        tabEntities = new ArrayList<>();
        for(int i = 0; i < selectImg.length; i++)
            tabEntities.add(new TabEntity(title[i], selectImg[i], unselectImg[i]));

        tabLayout = (CommonTabLayout) findViewById(R.id.tabs);
        tabLayout.setTabData(tabEntities);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectItem(int position) {
        if (mDrawerList != null) {
            mDrawerList.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(position > 15) {
            Intent intent = new Intent(this, ComicsListActivity.class);
            intent.putExtra("position", position - 2);
            startActivity(intent);
            //transaction.replace(R.id.fragment, fragmentArray.get(position - 1));
        }
        else {
            if(position != 2) {
                Intent intent = new Intent(this, ComicsListActivity.class);
                intent.putExtra("position", position - 1);
                startActivity(intent);
            }
            else{
                mViewPager.setCurrentItem(0);
                tabLayout.setCurrentTab(0);
            }
            //transaction.replace(R.id.fragment, fragmentArray.get(position - 2));
        }

        //transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        searchMenu = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenu.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                /*bundle = new Bundle();
                bundle.putString("url", getString(R.string.search, s));
                fragment = new SearchFragment();
                fragment.setArguments(bundle);*/
                Intent intent = new Intent(MainActivity.this, ComicsListActivity.class);
                intent.putExtra("position", -1);
                intent.putExtra("url", getString(R.string.search, s));
                startActivity(intent);
                /*
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment, fragment);
                transaction.commit();
                */
                searchMenu.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if(searchMenu.isActionViewExpanded()){
            searchMenu.collapseActionView();
        }
        else if(mDrawerLayout.isDrawerOpen(mDrawerList)){
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        else{
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
