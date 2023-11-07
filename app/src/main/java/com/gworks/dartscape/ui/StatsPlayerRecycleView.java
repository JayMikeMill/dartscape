package com.gworks.dartscape.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.data.PlayerStats;
import static com.gworks.dartscape.data.Stats.*;

import java.util.ArrayList;

/**
 *  Player recycle view for adding,editing and deleting players.
 **/
public class StatsPlayerRecycleView extends RecyclerView {
    /** the custom adapter of this recycle view */
    Adapter mAdapter;

    public StatsPlayerRecycleView(@NonNull Context context) {
        super(context);
        init();
    }

    public StatsPlayerRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatsPlayerRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /** initialize the recycle view */
    private void init() {
        LayoutManager lay = new LinearLayoutManager(getContext());

        setLayoutManager(lay);

        // get player names from preferences
        ArrayList<ArrayList<String>>  items = new ArrayList<>();


        items.add(new ArrayList<>());
        items.get(0).add("ha");
        items.get(0).add("blha");

        // initialize adapter
        mAdapter = new Adapter(getContext(), items);

        setAdapter(mAdapter);
    }



    private static final float SZ_SM = 0.65f;
    private static final float SZ_LG = 1f;

    ArrayList<StatId> mStatIds;
    public void fillWithPlayerStats(PlayerStats playerStats, GameFlags.GameFlag game) {
        // get player names from preferences
        ArrayList<ArrayList<String>> strStats = new ArrayList<>();
        mStatIds = getGameStatIds(game);

        for(int i = 0; i < mStatIds.size(); i++) {
            strStats.add(new ArrayList<>());
            strStats.get(i).add(getStatString(mStatIds.get(i)));
            strStats.get(i).add(playerStats.getStatString(mStatIds.get(i)));
        }

        mAdapter.refill(strStats);
    }

    public void setSelectedIndex(int index) {
        mAdapter.setSelected(index);
    }

    /** @return The index of the currently selected player */
    public int getSelectedIndex() {
        return mAdapter.getSelected();
    }

    /** @return The total players in the list */
    public int getPlayerCount() {
        return mAdapter.getItemCount();
    }

    /*
        ADAPTER
     */
    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private ArrayList<ArrayList<String>> mPlayerStats;
        private final LayoutInflater mInflater;

        private int mSelectedPos;
        private int mItemHeight;

        // data is passed into the constructor
        public Adapter(Context context, ArrayList<ArrayList<String>> data) {

            // set default members
            mInflater = LayoutInflater.from(context);
            mPlayerStats = data;

            mSelectedPos = NO_POSITION;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void refill(ArrayList<ArrayList<String>> playerStats) {
            mPlayerStats = playerStats;
            notifyDataSetChanged();
        }



        // inflates the row layout from xml when needed
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_recycle_stats_player, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // if the player is selected, lighten backgtound color
            //holder.itemView.setBackgroundColor(mSelectedPos == position
            //       ? 0x88FFFFFF : Color.TRANSPARENT);
            if (position == getStatCount() - 1) holder.tvValue.setMaxLines(2);

            holder.tvStat.setText(mPlayerStats.get(position).get(0));
            holder.tvValue.setText(mPlayerStats.get(position).get(1));

            holder.tvStat.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    holder.mHeight * .7f * (getStatMajor(mStatIds.get(position)) ? SZ_LG : SZ_SM));
        }
        public void setSelected(int position) {
            if (mSelectedPos == position) return;

            notifyItemChanged(mSelectedPos);
            mSelectedPos = position;
            notifyItemChanged(mSelectedPos);
        }

        public int getSelected() {
            return mSelectedPos;
        }


        /**
         * @return The total number of rows
         */
        @Override
        public int getItemCount() {
            return mPlayerStats.size();
        }

        // stores and recycles views as they are scrolled off screen
        private class ViewHolder extends RecyclerView.ViewHolder{
            public TextView tvStat;
            public TextView tvValue;

            public float mHeight;
            @SuppressLint("ClickableViewAccessibility")
            ViewHolder(View itemView) {
                super(itemView);

                if(mItemHeight == 0) mItemHeight = getHeight() / 12;
                ViewGroup.LayoutParams lp = itemView.getLayoutParams();
                lp.height = mItemHeight;
                mHeight =  lp.height;
                itemView.setLayoutParams(lp);

                tvStat = itemView.findViewById(R.id.tvStatsPlayerRVName);
                tvValue = itemView.findViewById(R.id.tvStatsPlayerRVValue1);
            }
        }
    }
}
