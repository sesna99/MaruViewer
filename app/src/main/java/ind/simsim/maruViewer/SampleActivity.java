package ind.simsim.maruViewer;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


public class SampleActivity extends FragmentActivity {
    private MarketVersionChecker checker;
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private View header;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private Fragment fragment;
    private ArrayList<Fragment> fragmentArray;
    private Bundle bundle;
    private MenuItem searchMenu;
    private String store_version, device_version, rtn, verSion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        ActionBar ab = getActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        new Version().execute();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        edit = preferences.edit();
        if(preferences.getString("password", "").equals("")){
            createDialog();
        }

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        initDrawer();
        initFragment();
    }

    private void createDialog(){
        final EditText passWord = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("초기설정")
                .setMessage("qndxkr을 입력하세요")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (passWord.getText().toString().equals("qndxkr")) {
                            edit.putString("password", "qndxkr");
                            edit.commit();
                        } else {
                            createDialog();
                        }
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setView(passWord)
                .show();
    }

    private void initDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);
        header = getLayoutInflater().inflate(R.layout.drawer_list_header, null, false);

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

        //mDrawerList.addHeaderView(header);
        header.findViewById(R.id.bookMark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SampleActivity.this, "mark", Toast.LENGTH_SHORT).show();
            }
        });

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

    private void initFragment(){
        fragmentArray = new ArrayList<>();
        String[] url = getResources().getStringArray(R.array.url);
        int position = 0;
        for(String temp : url){
            fragment = new ComicsListFragment();
            bundle = new Bundle();
            bundle.putString("url", temp);
            Log.i("url", temp);
            fragment.setArguments(bundle);
            fragmentArray.add(fragment);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, fragmentArray.get(1));
        transaction.commit();
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(position > 15)
            transaction.replace(R.id.fragment, fragmentArray.get(position - 2));
        else
            transaction.replace(R.id.fragment, fragmentArray.get(position - 1));
        transaction.commit();
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
                bundle = new Bundle();
                bundle.putString("url", getString(R.string.search, s));
                fragment = new SearchFragment();
                fragment.setArguments(bundle);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment, fragment);
                transaction.commit();
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
        clearApplicationCache(null);
    }

    private void clearApplicationCache(java.io.File dir){
        if(dir==null)
            dir = getCacheDir();
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

    class Version extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

            // Confirmation of market information in the Google Play Store
            try {
                Document doc = Jsoup
                        .connect("https://play.google.com/store/apps/details?id=" + getPackageName())
                        .get();
                Elements Version = doc.select(".content");

                for (Element v : Version) {
                    if (v.attr("itemprop").equals("softwareVersion")) {
                        rtn = v.text();
                    }
                }
                return rtn;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Version check the execution application.
            PackageInfo pi = null;
            try {
                pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            verSion = pi.versionName;
            rtn = result;

            if (!verSion.equals(rtn)) {
                AlertDialog updateDialog = new AlertDialog.Builder(SampleActivity.this)
                        .setTitle("업데이트가 발견되었습니다.")
                        .setMessage("업데이트가 있습니다.\n 업데이트 후 이용이 가능합니다.")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id="+getPackageName()));
                                startActivity(intent);
                                finish();
                            }
                        })
                        .show();
            }

            super.onPostExecute(result);
        }
    }
}
