package io.freefair.example;

import android.app.Activity;
import android.os.Bundle;

public class DummyActivity extends Activity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        System.out.println("in Method");
        super.onPostCreate(savedInstanceState);
    }
}
