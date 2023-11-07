package com.gworks.dartscape.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gworks.dartscape.R;
import com.gworks.dartscape.util.MyColors;
import com.gworks.dartscape.data.PlayerStats;

import static com.gworks.dartscape.data.Stats.*;

/**
 *  Player recycle view for adding,editing and deleting players.
 **/
public class StatsAllRecycleView extends RecyclerView {
    /** the custom adapter of this recycle view */
    Adapter mAdapter;
    ItemListener mClientItemListener = null;

    public StatsAllRecycleView(@NonNull Context context) {
        super(context);
        init();
    }

    public StatsAllRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatsAllRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        items.get(0).add("smha");
        items.get(0).add("smha");
        items.get(0).add("glha");


        // initialize adapter
        mAdapter = new Adapter(getContext(), items, mItemListener);

        setAdapter(mAdapter);


    }

    public void fillWithAllPlayerStats(ArrayList<PlayerStats> playerStats, StatId statId) {

        PlayerStats[] psa = playerStats.toArray(new PlayerStats[0]);
        Arrays.sort(psa, new PlayerStatsComparator().setSortBy(statId, true));
        playerStats = new ArrayList<>(Arrays.asList(psa));


        // get player names from preferences
        ArrayList<ArrayList<String>> strStats = new ArrayList<>();

        for(int i = 0; i < playerStats.size(); i++) {
            strStats.add(new ArrayList<>());
            strStats.get(i).add(playerStats.get(i).getName());
            strStats.get(i).add(playerStats.get(i).getStatString(statId));
        }

        mAdapter.refill(strStats);
    }


    public static class PlayerStatsComparator implements Comparator<PlayerStats> {
        StatId mSortBy = null;
        boolean mDescending = false;

        public PlayerStatsComparator setSortBy(StatId sortBy, boolean descending) {
            mSortBy = sortBy;
            mDescending = descending;
            return this;
        }

        @Override
        public int compare(PlayerStats ps1, PlayerStats ps2) {
            int compare = 0;
            if (getStatType(mSortBy) == STAT_TYPE_INT)
                compare = Integer.valueOf(ps1.getStatString(mSortBy))
                        .compareTo(Integer.valueOf(ps2.getStatString(mSortBy)));
            else if (getStatType(mSortBy) == STAT_TYPE_FLOAT)
                compare = Float.valueOf(ps1.getStatString(mSortBy))
                        .compareTo(Float.valueOf(ps2.getStatString(mSortBy)));
            else if (getStatType(mSortBy) == STAT_TYPE_STRING)
                compare = ps1.getStatString(mSortBy).compareTo(ps2.getStatString(mSortBy));

            if (mDescending) compare = -compare;
            return compare;
        }
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

    public String getSelectedName() {
        return mAdapter.getSelectedName();
    }

    public void setClickListener(ItemListener itemListener) {
        mClientItemListener = itemListener;
    }

    public interface ItemListener {
        void onItemClick(View view,int position);
    }

    ItemListener mItemListener = new ItemListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (mClientItemListener != null)
                mClientItemListener.onItemClick(view, position);
        }
    };

    /*
        ADAPTER
     */
    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private ArrayList<ArrayList<String>> mPlayerStats;
        private final LayoutInflater mInflater;

        private int mSelectedPos;

        // data is passed into the constructor
        public Adapter(Context context, ArrayList<ArrayList<String>> data, ItemListener itemListener) {

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
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_recycle_stats, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // if the player is selected, lighten backgtound color
            holder.itemView.setBackgroundColor(mSelectedPos == position
                    ? MyColors.themeHighlight() : Color.TRANSPARENT);

            holder.tvName.setText(mPlayerStats.get(position).get(0));
            holder.tvStatValue1.setText(mPlayerStats.get(position).get(1));
        }

        public String getSelectedName() {
            return mPlayerStats.get(mSelectedPos).get(0);
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


        /** @return The total number of rows */
        @Override
        public int getItemCount() {
            return mPlayerStats.size();
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener  {
            TextView tvName;
            TextView tvStatValue1;

            @SuppressLint("ClickableViewAccessibility")
            ViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                ViewGroup.LayoutParams lp = itemView.getLayoutParams();
                lp.height = getHeight() / 10;
                itemView.setLayoutParams(lp);

                tvName = itemView.findViewById(R.id.tvStatsRVName);
                tvStatValue1 = itemView.findViewById(R.id.tvStatsRVStatValue1);
            }

            @Override
            public void onClick(View view) {
                setSelected(getLayoutPosition());

                if (mItemListener != null)
                    mItemListener.onItemClick(view, getAdapterPosition());
            }
        }
    }
}
