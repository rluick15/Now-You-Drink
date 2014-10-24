package com.richluick.nowyoudrink;

import android.app.Application;
import android.view.ViewConfiguration;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;
import com.richluick.nowyoudrink.ui.activities.MainActivity;
import com.richluick.nowyoudrink.utils.ParseConstants;

import java.lang.reflect.Field;

/**
 * Created by Rich on 9/18/2014.
 */
public class NowYouDrinkApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "k1fpMe0Hns2JX8TmgJ8GHqdeGft7Z6incqE1Bych", "ymYKO08ET8D3GiCZ7p6h8IeORhC7bQ8GdBK4ACYD");

        ParseInstallation.getCurrentInstallation().saveInBackground();
        PushService.setDefaultPushCallback(this, MainActivity.class);

        alwaysShowOverflow();
    }

    public static void updateParseInstallation(ParseUser user) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER, user);
        installation.saveInBackground();
    }

    //this method is called to always show the overflow menu on all devices for consistancy
    private void alwaysShowOverflow() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e) {
            // Ignore
        }
    }
}
