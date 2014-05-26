package com.itouxian.android.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.itouxian.android.R;
import com.itouxian.android.util.Utils;

import java.util.HashMap;

/**
 * Created by chenjishi on 14-5-24.
 */
public class TagGroupView extends RelativeLayout implements View.OnClickListener {
    private static final int ITEMS_PER_ROW = 4;

    private Context mContext;

    private String[] mTagArray;

    private HashMap<Integer, Integer> mIdsMap;

    private HashMap<Integer, String> mSelectedMap;

    private OnTagSelectListener mListener;

    public TagGroupView(Context context) {
        this(context, null);
    }

    public TagGroupView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagGroupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        int padding = Utils.dp2px(context, 8);
        setPadding(padding, padding, padding, padding);
        setBackgroundColor(0xFFE5E5E5);

        mSelectedMap = new HashMap<Integer, String>();

        setData(context.getResources().getStringArray(R.array.tags));
    }

    public void focusFirstTag(int[] ids) {
        for (int i = 0; i < ids.length; i++) {
            mSelectedMap.put(ids[i], "hit");
        }
        hightlightTag();
    }

    public void setOnTagSelectListener(OnTagSelectListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        int index = (Integer) v.getTag();
        if (!TextUtils.isEmpty(mSelectedMap.get(index))) {
            mSelectedMap.remove(index);
        } else {
            mSelectedMap.put(index, "hit");
        }

        hightlightTag();

        if (null != mListener) mListener.onTagSelect(mSelectedMap);
    }

    private void hightlightTag() {
        for (int i : mIdsMap.keySet()) {
            int viewId = mIdsMap.get(i);
            TagView tagView = (TagView) findViewById(viewId);
            tagView.setSelected(!TextUtils.isEmpty(mSelectedMap.get(i)));
        }
    }

    private void setData(String[] tags) {
        mTagArray = tags;

        int size = mTagArray.length;
        int column = ITEMS_PER_ROW;
        int row = size / column;
        if (size % column > 0) {
            row += 1;
        }

        mIdsMap = new HashMap<Integer, Integer>();
        int margin = Utils.dp2px(mContext, 8);
        int width = (getResources().getDisplayMetrics().widthPixels
                - 2 * Utils.dp2px(mContext, 8) - 2 * Utils.dp2px(mContext, 12) - 3 * margin) / 4;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < (i == row - 1 ? (size - column * (row - 1)) : column); j++) {
                int index = i * column + j;
                TagView tagView = new TagView(mContext);
                int id = Utils.generateViewId();
                tagView.setId(id);
                mIdsMap.put(index, id);
                RelativeLayout.LayoutParams lp = new LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i == 0) {
                    if (j > 0) lp.addRule(RIGHT_OF, mIdsMap.get(index - 1));
                } else {
                    lp.addRule(BELOW, mIdsMap.get((i - 1) * column));
                    if (j > 0) lp.addRule(RIGHT_OF, mIdsMap.get(index - 1));
                }

                if (i < row - 1 && j < column - 1) {
                    lp.setMargins(0, 0, margin, margin);
                }

                if (i == row - 1 && j < (size - column * (row - 1)) - 1) {
                    lp.setMargins(0, 0, margin, 0);
                }

                tagView.setTagText(mTagArray[index]);
                tagView.setTag(index);
                tagView.setLayoutParams(lp);
                tagView.setOnClickListener(this);
                addView(tagView);
            }
        }
    }

    public interface OnTagSelectListener {

        public void onTagSelect(HashMap<Integer, String> idsMap);

    }
}
