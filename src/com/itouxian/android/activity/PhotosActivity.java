package com.itouxian.android.activity;

import android.os.Bundle;
import android.widget.GridView;
import com.itouxian.android.R;

/**
 * Created by chenjishi on 14-5-6.
 */
public class PhotosActivity extends BaseActivity {
    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mGridView = (GridView) findViewById(R.id.grid_view);
    }
}
