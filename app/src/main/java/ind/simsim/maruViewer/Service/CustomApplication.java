package ind.simsim.maruViewer.Service;

import android.app.Application;

import com.tsengvn.typekit.Typekit;

/**
 * Created by jack on 2017. 1. 13..
 */

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Typekit.getInstance()
                .addCustom1(Typekit.createFromAsset(this, "fonts/AppleSDGothicNeo-Medium.otf"))
                .addCustom2(Typekit.createFromAsset(this, "fonts/AppleSDGothicNeo-Bold.otf"))
                .addCustom3(Typekit.createFromAsset(this, "fonts/AppleSDGothicNeo-Light.otf"))
                .addCustom4(Typekit.createFromAsset(this, "fonts/AppleSDGothicNeo-SemiBold.otf"))
                .addCustom5(Typekit.createFromAsset(this, "fonts/AppleSDGothicNeo-Regular.otf"))
                .addCustom6(Typekit.createFromAsset(this, "fonts/AppleSDGothicNeo-UltraLight.otf"));
    }
}
