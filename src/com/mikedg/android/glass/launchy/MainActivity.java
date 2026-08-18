/*
Copyright 2013 Michael DiGiovanni glass@mikedg.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.mikedg.android.glass.launchy;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends Activity {
    private static final float SWIPE_PX = 60f;
    private static final float TAP_SLOP_PX = 24f;

    private AppHelper appHelper;
    private ListView list;
    private float downX;
    private float downY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appHelper = new AppHelper(this);
        appHelper.loadApplications(false);
        appHelper.bindApplications();
        appHelper.registerIntentReceivers();

        list = findViewById(android.R.id.list);
        list.setSelection(0);
        list.requestFocus();
        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                list.smoothScrollToPositionFromTop(position, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return handleTouch(event) || super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return handleTouch(event) || super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean handleTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                if (Math.abs(dx) > SWIPE_PX && Math.abs(dx) > Math.abs(dy)) {
                    moveSelection(dx > 0 ? 1 : -1);
                } else if (dy > SWIPE_PX && Math.abs(dy) > Math.abs(dx)) {
                    finish();
                } else if (Math.hypot(dx, dy) <= TAP_SLOP_PX) {
                    launchSelection();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
            default:
                return false;
        }
    }

    private void moveSelection(int delta) {
        int count = list.getAdapter().getCount() - 1;
        if (count <= 0) return;
        int current = Math.max(0, list.getSelectedItemPosition());
        list.setSelection(Math.max(0, Math.min(count - 1, current + delta)));
    }

    private void launchSelection() {
        int position = Math.max(0, list.getSelectedItemPosition());
        list.performItemClick(list.getSelectedView(), position, list.getItemIdAtPosition(position));
    }

    @Override
    protected void onDestroy() {
        if (appHelper != null) appHelper.onDestroy();
        super.onDestroy();
    }
}
