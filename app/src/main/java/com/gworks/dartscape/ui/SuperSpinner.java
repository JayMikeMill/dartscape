package com.gworks.dartscape.ui;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import com.gworks.dartscape.R;
import com.gworks.dartscape.util.Helper;

import java.util.ArrayList;
import java.util.List;

/**
 *  Player recycle view for adding,editing and deleting players.
 **/
public class SuperSpinner extends AppCompatSpinner {
    /** the custom adapter of this recycle view */
    private SuperSpinAdapter mAdapter;
    private SpinIconAdapter  mIconAdapter;
    private int mPreviousSelection;
    private boolean mIsIconSpinner;
    private OnItemSelectedListener mTempListener;

    public SuperSpinner(@NonNull Context context) {
        super(context);
    }

    public SuperSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SuperSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /** initialize the recycle view */
    public void init(ArrayList<String> items, float textSize) {

        // get player names from preferences
        mAdapter = new SuperSpinAdapter(getContext(),
                R.layout.item_spinner,
                R.id.tvCustomSpinText, textSize, 0, items);

        setAdapter(mAdapter);

        mIsIconSpinner = false;
    }

    /** initialize as an icon spinner */
    public void init(List<Drawable> icons) {
        mIconAdapter = new SpinIconAdapter(getContext(), icons);
        mIconAdapter.setDropDownViewResource(R.layout.item_spinner_icon);

        setAdapter(mIconAdapter);

        mIsIconSpinner = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setDropDownWidth(w);

        if(mAdapter != null) {
            mAdapter.onSizeChange(w, h);
            //setDropDownVerticalOffset(-(h * (getCount() / 2) - (h/2)));
        }

        if(mIconAdapter != null) {
            mIconAdapter.notifyDataSetChanged();
            //setDropDownVerticalOffset(-(h * (getCount() / 2) - (h/2)));
        }
    }

    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    public void clear() {
        mAdapter.clear();
    }

    public void addAll(ArrayList<String> items) {
        mAdapter.addAll(items);
    }

    public void addAllKeepSel(ArrayList<String> items) {
        String stat = getSelectedItem() != null ? getSelectedItem().toString() : "";
        mAdapter.addAll(items);
        int statpos = getFirstItemPosition(stat);
        setSelection(statpos == -1 ? 0 : statpos);
    }
    public void setLeftAlign(boolean align) {
        mAdapter.setLeftAlign(align);
    }

    public int getFirstItemPosition(String text) {
        return mAdapter.getFirstTextItemPosition(text);
    }

    public String getPreviousSelectedItem() {
        if(!mIsIconSpinner) return mAdapter.getTextItem(mPreviousSelection);
        else return "";
    }

    public int getPreviousSelectedPosition() {
        return mPreviousSelection;
    }

    @Override
    public int getCount() {
        return mAdapter != null ? mAdapter.getCount() : 0;
    }

    public void setSelection(int position, boolean animate, boolean noTrigger) {
        mPreviousSelection = getSelectedItemPosition();

        if(noTrigger) super.setOnItemSelectedListener(null);

        super.setSelection(position, true);

        if(noTrigger) {
            this.post(() -> super.setOnItemSelectedListener(mTempListener));
        }
    }

    @Override
    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
        super.setOnItemSelectedListener(listener);
        mTempListener = listener;
    }

    @Override
    public void setSelection(int position) {
        mPreviousSelection = getSelectedItemPosition();
        super.setSelection(position);
    }


    public static class SuperSpinAdapter extends ArrayAdapter<String> {
        public static final int PLAYER_SPINNER_ICONS = 0x400;

        private List<String> mTextItems;

        private int mTextIconId;
        private Drawable mTextIcon;
        private Boolean mLeftAlign = false;
        private float mTextSizeRatio = -1f;
        private List<Drawable> mIcons;
        private boolean mIsIconAdapter;

        private Rect mParentBounds = new Rect();
        private float mTextWidthLimit;
        private final Context mContext;

        public SuperSpinAdapter(Context context, int resourceId, int textviewId, float textSize, int iconId, List<String> list) {
            super(context, resourceId, list);

            mContext = context;
            mTextIconId = iconId;
            mTextItems = list;

            if(mTextIconId != 0 && mTextIconId != PLAYER_SPINNER_ICONS)
                mTextIcon = AppCompatResources.getDrawable(context, iconId);

            setTextSizeRatio(textSize);

            mIsIconAdapter = false;
        }

        public SuperSpinAdapter(Context context, int resourceId, int textviewId, List<Drawable> icons) {
            super(context, resourceId, textviewId);
            mContext = context;
            mIcons = icons;
            mIsIconAdapter = true;
        }

        public boolean isIconAdapter() {
            return mIsIconAdapter;
        }

        public Drawable getIconItem(int index) {
            if(isIconAdapter()) return mIcons.get(index);
            else return null;
        }

        public String getTextItem(int index) {
            if(!isIconAdapter()) return mTextItems.get(index);
            else return "";
        }

        public int getFirstTextItemPosition(String text) {
            if(!isIconAdapter()) return mTextItems.indexOf(text);
            else return -1;
        }

        public void addAll(List<String> items) {
            if(isIconAdapter()) return;
            mTextItems = new ArrayList<>(items);
            notifyDataSetChanged();
        }

        public void setTextSizeRatio(float size) {
            mTextSizeRatio = size;
        }

        public void setLeftAlign(boolean on) {
            mLeftAlign = on;
        }

        @Override
        public void clear() {
            super.clear();
            if(isIconAdapter()) mIcons = new ArrayList<>();
            mTextItems = new ArrayList<>();
        }

        @Override
        public int getCount() {
            if(isIconAdapter()) return mIcons.size();
            return mTextItems.size();
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return mTextItems.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView,  parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return rowView(convertView, position);
        }

        private View rowView(View convertView, int position) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spinner, null);

            TextView tv = new ViewHolder(convertView, position).txtTitle;

            convertView.setLayoutParams(new AbsListView.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, mParentBounds.height()));

            Helper.setShadowColor(tv);
            if(mLeftAlign) tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            else tv.setGravity(Gravity.CENTER);

            tv.setText(getTextItem(position));
            autoSizeText(tv);

            Drawable icon = null;
            if(mTextIconId != 0 && mTextIconId != PLAYER_SPINNER_ICONS) icon = mTextIcon.getCurrent();
            else if(mTextIconId == PLAYER_SPINNER_ICONS)
                icon = AppCompatResources.getDrawable(mContext,
                        getTextItem(position).contains("-") ? R.drawable.ic_users_trio : R.drawable.ic_user);

            tv.setCompoundDrawablesRelativeWithIntrinsicBounds
                        (null, null, null, null);


            return convertView;
        }

        public void onSizeChange(int w, int h) {
            mParentBounds = new Rect(0, 0, w, h);
            mTextWidthLimit = mParentBounds.width() - Helper.dpToPx(25);
            notifyDataSetChanged();
        }

        private void autoSizeText(TextView tv) {
            // auto size text
            float textSize = mParentBounds.height() * mTextSizeRatio;
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            String text = tv.getText().toString();

            Rect textRect = new Rect();
            int textSizeStep = 10; boolean autoSize;
            while (true) {
                tv.getPaint().getTextBounds(text, 0, text.length(), textRect);
                autoSize = textRect.width() >= mTextWidthLimit || mTextSizeRatio == -1f;
                if (!autoSize || textSize < 20) break;
                tv.getPaint().setTextSize(textSize -= textSizeStep);
            }

            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }


        static class ViewHolder extends RecyclerView.ViewHolder {
            //Views
            public TextView txtTitle;

            public ViewHolder(@NonNull View itemView, int position) {
                super(itemView);

                //Init Views by itemView.findViewById
                txtTitle = itemView.findViewById(R.id.tvCustomSpinText);
            }
        }
    }
}
