package com.itouxian.android.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.itouxian.android.FileCache;
import com.itouxian.android.PrefsUtil;
import com.itouxian.android.R;
import com.itouxian.android.util.FileUploadCallback;
import com.itouxian.android.util.HttpUtils;
import com.itouxian.android.util.Utils;
import com.itouxian.android.view.TagGroupView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenjishi on 14-3-30.
 */
public class FeedPostActivity extends BaseActivity implements FileUploadCallback,
        LoaderManager.LoaderCallbacks<Cursor>, TagGroupView.OnTagSelectListener {
    private static final int MAX_UPLOAD_IMAGE_WIDTH = 510;
    private static final int LOADER_ID = 10010;
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_CAMERA = 2;

    private Uri mImageUri;
    private String mImagePath;

    private HashMap<Integer, String> mTagIdsMap;

    private TagGroupView mTagGroup;
    private EditText mEditText;
    private Rect mRect = new Rect();

    private String[] mTagArray;

    private InputMethodManager mInputManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post, R.layout.title_bar_layout);

        mTagArray = getResources().getStringArray(R.array.tags);

        mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mEditText = (EditText) findViewById(R.id.edit_content);

        int height = getTextHeight(16.f);

        mEditText.setMaxHeight(5 * height);

        mTagGroup = (TagGroupView) findViewById(R.id.tag_group);
        mTagGroup.setOnTagSelectListener(this);

        String[] drafts = PrefsUtil.getDraft();
        if (null != drafts && drafts.length > 0) {
            recoverDraft(drafts);
        }
    }

    private void recoverDraft(String[] drafs) {
        String content = drafs[0];
        mEditText.setText(content);

        String[] array = drafs[1].split(",");
        HashMap<String, Integer> tagsMap = new HashMap<String, Integer>();
        for (int i = 0; i < mTagArray.length; i++) {
            tagsMap.put(mTagArray[i], i);
        }

        int len = array.length;
        int[] tagIds = new int[len];
        for (int i = 0; i < len; i++) {
            String key = array[i];
            int value = tagsMap.get(key);
            tagIds[i] = value;
            mTagIdsMap.put(value, "hit");
        }
        mTagGroup.focusFirstTag(tagIds);

        setPreview(drafs[2]);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        int[] location = new int[2];
        mEditText.getLocationOnScreen(location);
        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + mEditText.getWidth();
        mRect.bottom = location[1] + mEditText.getHeight();

        int x = (int) ev.getX();
        int y = (int) ev.getY();

        if (action == MotionEvent.ACTION_DOWN && !mRect.contains(x, y)) {
            mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onTagSelect(HashMap<Integer, String> idsMap) {
        mTagIdsMap = idsMap;
    }

    public void onCameraButtonClicked(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(FileCache.getDataCacheDir(this), "Pic.jpg");
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
        }

        mImageUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    public void onGalleryButtonClicked(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    public void onRightButtonClicked(View view) {
        final String content = mEditText.getText().toString();
        if (TextUtils.isEmpty(content)) {
            Utils.showToast(R.string.post_content_empty);
            return;
        }

        if (null == mTagIdsMap || mTagIdsMap.size() == 0) {
            mTagGroup.focusFirstTag(new int[]{0});
            Utils.showToast(R.string.post_tags_empty);
            return;
        }

        Map<String, String> params = new HashMap<String, String>();

        String token = PrefsUtil.getUser().token;

        StringBuilder sb = new StringBuilder();
        for (int i : mTagIdsMap.keySet()) {
            sb.append(mTagArray[i] + ",");
        }

        String tags = sb.toString();
        tags = tags.substring(0, tags.length() - 1);

        File file = null;
        if (!TextUtils.isEmpty(mImagePath)) {
            adjustPictureSize(mImagePath);

            file = new File(mImagePath);
            if (!file.exists()) file = null;
        }

        final String url = "http://www.itouxian.com/json/upload";

        params.put("token", token);
        params.put("content", content);
        params.put("tags", tags);

        HttpUtils.postImage(url, file, params, this);

        /** save the draft, when error happens, we can repost it */
        PrefsUtil.saveDraft(content, tags, mImagePath);

        finish();
    }

    private void adjustPictureSize(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);
        int width = options.outWidth;
        int height = options.outHeight;

        if (width > MAX_UPLOAD_IMAGE_WIDTH) {
            int reqWidth = MAX_UPLOAD_IMAGE_WIDTH;
            int reqHeight = reqWidth * height / width;

            options.inSampleSize = Utils.calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            if (null != bitmap) {
                File file = new File(filePath);
                if (file.exists()) file.delete();

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void onSuccess(int statusCode, String response) {
        int code = -1;
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObj = new JSONObject(response);
                code = jsonObj.optInt("code", -1);
            } catch (JSONException e) {
                code = -1;
            }
        }

        if (code == 0) {
            /** post success, delete the draft */
            PrefsUtil.saveDraft("", "", "");
        }

        Utils.showToast(code == 0 ? R.string.post_success : R.string.post_error);
    }

    @Override
    public void onFailure(int statusCode, String response, Throwable e) {
        Utils.showToast(R.string.post_error);
    }

    private int getTextHeight(float f) {
        TextView tv = new TextView(this);
        tv.setText(R.string.app_name);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, f);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        tv.measure(widthMeasureSpec, heightMeasureSpec);
        return tv.getMeasuredHeight();
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {MediaStore.Images.Media.DATA};
        return new CursorLoader(this, mImageUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        if (null != cursor) {
            int imageIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            mImagePath = cursor.getString(imageIndex);
            setPreview(mImagePath);
        }
        getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {

    }

    private void setPreview(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;

        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        int imageWidth = imageView.getWidth();
        int imageHeight = imageView.getHeight();
        options.inSampleSize = Utils.calculateInSampleSize(options, imageWidth, imageHeight);
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) return;

        if (requestCode == REQUEST_CODE_CAMERA) {
            mImagePath = mImageUri.getPath();
            setPreview(mImagePath);
        } else {
            mImageUri = data.getData();
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }
}
