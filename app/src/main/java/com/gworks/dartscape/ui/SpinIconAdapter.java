package com.gworks.dartscape.ui;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gworks.dartscape.R;
import com.gworks.dartscape.util.Helper;

import java.util.List;

public class SpinIconAdapter extends ArrayAdapter<Drawable> {
    Spinner mParent;
    List<Drawable> mIcon;
    public SpinIconAdapter(Context context, List<Drawable> icons) {
        super(context, R.layout.item_spinner_icon, icons);
        mIcon = icons;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mParent = (Spinner) parent;
        mParent.setDropDownVerticalOffset(-mParent.getHeight());
        return rowView(convertView, position);
    }

    @Override
    public View getDropDownView
            (int position, View convertView, @NonNull ViewGroup parent) {
        return rowView(convertView, position);
    }

    private View rowView(View convertView, int position) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_spinner_icon, null);

        ViewHolder holder = new ViewHolder(convertView);

        Drawable icon = getItem(position);
        icon = DrawableCompat.wrap(icon);
        holder.mLayIcon.setBackground(icon);

        convertView.setLayoutParams
                (new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mParent.getHeight()));

        return convertView;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        //Views
        public LinearLayout mLayIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mLayIcon = itemView.findViewById(R.id.layCustomSpinIcon);
        }
    }
}