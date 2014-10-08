package com.richluick.nowyoudrink;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by Rich on 9/18/2014.
 */
public class NowYouDrinkApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "k1fpMe0Hns2JX8TmgJ8GHqdeGft7Z6incqE1Bych", "ymYKO08ET8D3GiCZ7p6h8IeORhC7bQ8GdBK4ACYD");

        ParseInstallation.getCurrentInstallation().saveInBackground();

        //PushService.setDefaultPushCallback(this, MainActivity.class, R.drawable.ic_stat_ic_launcher);
    }
}
