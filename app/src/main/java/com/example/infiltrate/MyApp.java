package com.example.infiltrate;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class MyApp extends Application {

    public MyApp() {
        /*
        StrictMode.enableDefaults();
        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }*/
    }

}

