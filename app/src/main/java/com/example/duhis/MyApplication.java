package com.example.duhis;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dojfutnt5");
        config.put("api_key",    "986549373463368");
        config.put("api_secret", "axLbIuauyfmcySQpQgx0vg_VSQ4");
        MediaManager.init(this, config);
    }
}