package ind.trycatch.maruViewer.UI;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;

import ind.trycatch.maruViewer.Event.DivEvent;

/**
 * Created by trycatch on 2017. 9. 8..
 */

public class JavascriptInterface {
    private Context mContext;
    private ProgressDialog checkImageDialog, divImageDialog;
    private final Handler handler = new Handler();
    private boolean isFirst = false;

    public JavascriptInterface(Context context) {
        mContext = context;
    }

    @android.webkit.JavascriptInterface
    public void checkImage(){
        checkImageDialog = new ProgressDialog(mContext);
        checkImageDialog.setTitle("Load");
        checkImageDialog.setMessage("이미지 검사중..");
        checkImageDialog.setCanceledOnTouchOutside(false);
        checkImageDialog.show();
    }

    @android.webkit.JavascriptInterface
    public void finishCheckImage(){
        checkImageDialog.dismiss();
    }

    @android.webkit.JavascriptInterface
    public void divImage(){
        if(divImageDialog == null) {
            divImageDialog = new ProgressDialog(mContext);
            divImageDialog.setTitle("Load");
            divImageDialog.setMessage("이미지 분할중..");
            divImageDialog.setCanceledOnTouchOutside(false);
            divImageDialog.show();
        }
    }

    @android.webkit.JavascriptInterface
    public void finishDivImage(){
        if(divImageDialog != null)
            divImageDialog.dismiss();
    }

    @android.webkit.JavascriptInterface
    public void divEvent(){
        if(!isFirst) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new DivEvent(true));
                }
            });
            isFirst = true;
        }
    }

}
