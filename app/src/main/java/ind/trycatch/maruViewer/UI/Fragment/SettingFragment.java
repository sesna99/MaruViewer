package ind.trycatch.maruViewer.UI.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.Service.PreferencesManager;

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

    @BindView(R.id.kakao)
    TextView kakao;

    @BindView(R.id.developer)
    LinearLayout developer;

    @BindView(R.id.sponsor)
    LinearLayout sponsor;

    @BindView(R.id.sponsor_text)
    TextView sponsor_text;

    @BindView(R.id.update)
    LinearLayout update;

    @BindView(R.id.update_text)
    TextView update_text;

    private String BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/마루뷰어/";


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
        download_directory_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View v = getActivity().getLayoutInflater().inflate(R.layout.edit_dialog, null);
                final EditText editText = (EditText)v.findViewById(R.id.edit_text);
                editText.setText(PreferencesManager.getInstance(getActivity()).getDownLoadDirectory());
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setView(v)
                        .setTitle("검색")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PreferencesManager.getInstance(getActivity()).setDownLoadDirectory(editText.getText().toString());
                                download_directory_text.setText(editText.getText().toString());
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

        kakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://open.kakao.com/o/g3SsVZw"));
                startActivity(intent);
            }
        });

        developer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://trycatch98.tistory.com/"));
                startActivity(intent);
            }
        });

        sponsor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", "3CYUn9C5e9W8YWnpLYYSVo2gtqtPJPF9QT");
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getActivity(), "복사되었습니다. 후원금은 개발자에게 커피 한잔과 새로운 기능을 추가하는 원동력이 됩니다.", Toast.LENGTH_SHORT).show();
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