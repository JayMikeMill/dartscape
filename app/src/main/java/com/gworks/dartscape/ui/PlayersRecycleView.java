package com.gworks.dartscape.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gworks.dartscape.database.PlayerDatabase;
import com.gworks.dartscape.R;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.MyColors;

import java.util.ArrayList;
import java.util.Objects;

/**
 *  Player recycle view for adding,editing and deleting players.
 **/
public class PlayersRecycleView extends RecyclerView {
    private static final int ITEMS_SHOWN = 8;

    /** the custom adapter of this recycle view */
    Adapter mAdapter;

    /** lets client know if item was changed */
    ItemListener mItemListener;

    public PlayersRecycleView(@NonNull Context context) {
        super(context);
        init();
    }

    public PlayersRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayersRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /** initialize the recycle view */
    private void init() {
        if (isInEditMode()) return;

        CustomGridLayoutManager layoutManager =
                new CustomGridLayoutManager(getContext());

        setLayoutManager(layoutManager);

        // initialize adapter
        mAdapter = new Adapter(getContext());

        setAdapter(mAdapter);
        mAdapter.setItemListener(onItem);

        //addItemDecoration(new Divider(getContext(), R.drawable.players_rv_divider, Color.BLACK));
    }

    public void refill() {
        mAdapter.fillWithNames();
    }

    private void setScrollEnabled(boolean enabled) {
        ((CustomGridLayoutManager) Objects.requireNonNull
                (getLayoutManager())).setScrollEnabled(enabled);
        setNestedScrollingEnabled(enabled);
    }

    public void setSelectedIndex(int index) {
        mAdapter.setSelected(index);
    }

    /** @return The index of the currently selected player */
    public int getSelectedIndex() {
        return mAdapter.getSelected();
    }

    public boolean isAddingPlayer() {
        return mAdapter.isAdding();
    }

    /** @return The total players in the list */
    public int getPlayerCount() {
        return mAdapter.getItemCount();
    }

    /** Adds a new player to the list and database */
    public void addPlayer() {
        if (mAdapter.isEditing()) return;

        mAdapter.addPlayer(mAdapter.getItemCount());
        mAdapter.setSelected(mAdapter.getItemCount() - 1);
        scrollToPosition(mAdapter.getItemCount() - 1);

        setScrollEnabled(false);

        mAdapter.startEditing();
    }

    /** Saves a new or edited player to the list and database */
    public void savePlayers() {
        mAdapter.savePlayer();
        setScrollEnabled(true);
    }

    /** Starts the editing process */
    public void editSelected() {
        scrollToPosition(mAdapter.getSelected());
        setScrollEnabled(false);
        mAdapter.startEditing();
    }

    /** Deletes the selected player from list and database */
    public void deleteSelected() {
        setScrollEnabled(true);
        mAdapter.removePlayer(mAdapter.getSelected());
    }

    public void stopEditing() {
        setScrollEnabled(true);
        mAdapter.stopEditing();
    }

    public boolean isEditing() {
        return mAdapter.isEditing();
    }

    public void setItemListener(ItemListener itemListener) {
        mItemListener = itemListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemListener {
        void onItemClick(View view, int position);
        void onItemEdited();
    }

    ItemListener onItem = new ItemListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (mItemListener != null)
                mItemListener.onItemClick(view, position);
        }

        @Override
        public void onItemEdited() {
            if (mItemListener != null)
                mItemListener.onItemEdited();
        }
    };

    /** grab the global game data */
    private PlayerDatabase db() {
        return ((DartScapeActivity)getContext()).playerDB();
    }
    /*
        ADAPTER
     */
    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private ArrayList<String> mData;
        private final LayoutInflater mInflater;
        private ItemListener mItemListener;

        private int mItemHeight = 0;

        private int mSelectedPos;

        private boolean mEditing;
        private boolean mAdding;
        private int mEditingPos;
        private EditText mEditingText;
        private String mBeginEditText;


        // data is passed into the constructor
        public Adapter(Context context) {

            // set default members
            mInflater = LayoutInflater.from(context);

            fillWithNames();

            mSelectedPos = NO_POSITION;

            mEditing = false;
            mAdding = false;
            mEditingPos = -1;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void fillWithNames() {
            PlayerDatabase.DBHelper.runAsyncResult(() -> {
                // Load stats in background thread
                return db().getAllPlayerNames();
            }, stats -> {
                mData = stats;
                notifyDataSetChanged();
            });
        }


        // inflates the row layout from xml when needed
        @NonNull @Override
        public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_recycle_player, parent, false);
            return new Adapter.ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {

            // if the player is selected, lighten background color
            holder.itemView.setBackgroundColor(mSelectedPos == position
                    ? MyColors.themeHighlight() : Color.TRANSPARENT);

            Drawable player =AppCompatResources.getDrawable(getContext(), R.drawable.ic_user);
            Drawable team = AppCompatResources.getDrawable(getContext(), R.drawable.ic_users);

            holder.mIvIcon.setImageDrawable(mData.get(position).contains("-") ? team : player);

            holder.mEvItem.setText(mData.get(position));

            boolean editing = mEditing && mEditingPos == position;
            int inputType = editing ?
                    InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS |
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS :
                    InputType.TYPE_NULL |
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

            holder.mEvItem.setInputType(inputType);
            holder.mEvItem.setFocusable(editing);
            holder.mEvItem.setFocusableInTouchMode(editing);

            if (editing) {
                mEditingText = holder.mEvItem;
                mBeginEditText = mEditingText.getText().toString();
                mEditingText.requestFocus();
                Helper.showKeyboard(mEditingText);
            } else {
                holder.mEvItem.clearFocus();
            }
        }

        public boolean isAdding() { return mAdding;}
        public void setSelected(int position) {
            if (mEditing) return;
            if(mSelectedPos == position) return;

            notifyItemChanged(mSelectedPos);
            mSelectedPos = position;
            notifyItemChanged(mSelectedPos);
        }

        public int getSelected() { return mSelectedPos; }

        @SuppressLint("NotifyDataSetChanged")

        public void startEditing() {
            if(mSelectedPos == NO_POSITION) return;

            mEditing = true;
            mEditingPos = mSelectedPos;
            notifyItemChanged(mSelectedPos);
        }

        // Finished editing
        public void stopEditing() {
            mEditing = false;
            Helper.closeKeyboard(mEditingText);
            notifyItemChanged(mSelectedPos);
        }

        /** Start adding a new player */
        public void addPlayer(int position) {
            mAdding = true;
            mData.add(position, "PLAYER");
            notifyItemInserted(position);
        }

        /** Save a new player to database or update existing player */
        public void savePlayer() {
            if(!mEditing) return;
            if(mSelectedPos == NO_POSITION || mSelectedPos >= mData.size()) {
                mAdding = false;
                return;
            }

            String newName =  mEditingText.getText().toString();

            if(newName.isEmpty()) {

                Helper.dialogs().showConfirm(getContext().getString(R.string.no_name), R.drawable.ic_cancel,
                        getContext().getString(R.string.dialog_no_name),
                        getContext().getString(R.string.ok), true, dialog -> Helper.showKeyboard(mEditingText));

                return;
            }

            if(isAddingPlayer()) {
                // try to add player to the database
                int code = db().createPlayer(newName);int i = 0;

                while(code != PlayerDatabase.CODE_SUCCESS)
                    code = db().createPlayer(newName + ++i);
                if(i > 0) newName = newName + i;
            }
            else {
                // just editing, update player's name in database
                db().updatePlayerName(mBeginEditText, newName);
            }

            // update the list view data
            mData.set(mSelectedPos, newName);
            mEditingText.setText(newName);

            // notify client a player was edited
            mItemListener.onItemEdited();

            // no longer adding or editing
            mAdding = false;
            stopEditing();
        }


        public void removePlayer(int position) {
            if(position == NO_POSITION || position >= mData.size()) {
                // stop editing and adding
                mEditing = false;
                mAdding = false;
                return;
            }

            // close keyboard if it is open in adding mode
            if(isAddingPlayer()) Helper.closeKeyboard(mEditingText);

            // let client know an item was edited
            mItemListener.onItemEdited();

            // remove player from database if necessary
            if(!isAddingPlayer())
                db().deletePlayer(mData.get(position));

            // remove player from list
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mData.size());

            // stop editing and adding
            mEditing = false;
            mAdding = false;

            // select next item if available, if not, previous item
            setSelected(position < getItemCount() ? position : getItemCount() - 1);
        }

        public boolean isEditing() {
            return mEditing;
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
            ImageView mIvIcon;
            EditText mEvItem;

            @SuppressLint("ClickableViewAccessibility")
            ViewHolder(View itemView) {
                super(itemView);

                // set the height of the edit text to 1/6th the height of
                // the recycle view, also auto size the text
                if(mItemHeight == 0) mItemHeight = getHeight() / ITEMS_SHOWN;
                ViewGroup.LayoutParams lp = itemView.getLayoutParams();
                lp.height = mItemHeight;
                itemView.setLayoutParams(lp);

                mIvIcon = itemView.findViewById(R.id.ivPlayerRVIcon);
                mEvItem = itemView.findViewById(R.id.etPlayerRVLayout);
                mEvItem.setTextSize(TypedValue.COMPLEX_UNIT_PX, lp.height * .7f);

                mEvItem.setOnClickListener(this);
                mEvItem.setOnEditorActionListener(mOnEnterListener);

                // select all text on focus
                mEvItem.setOnFocusChangeListener((v, hasFocus) -> {
                    if(hasFocus) {
                        ((EditText) v).selectAll();
                    }
                });

                // make sure text remains uppercase
                mEvItem.addTextChangedListener(new TextWatcher() {
                    @Override public void onTextChanged
                            (CharSequence arg0, int arg1, int arg2, int arg3) {}
                    @Override public void beforeTextChanged
                            (CharSequence arg0, int arg1, int arg2, int arg3) {}

                    @Override public void afterTextChanged(Editable et) {
                        String s = et.toString();
                        String os = s;
                        if (!s.equals(s.toUpperCase())) s = s.toUpperCase();

                        if(!os.equals(s)) {
                            int sel = mEvItem.getSelectionStart();
                            mEvItem.setText(s);
                            mEvItem.setSelection(sel);
                        }
                    }
                });
            }


            /* Send click back to client */
            @Override
            public void onClick(View view) {
                setSelected(getLayoutPosition());

                if (mItemListener != null)
                    mItemListener.onItemClick(view, getAdapterPosition());
            }

            /** on enter key press, finished editing */
            EditText.OnEditorActionListener mOnEnterListener = (v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    savePlayer();
                    return true;
                }

                return false;
            };
        }

        // allows clicks events to be caught
        public void setItemListener(ItemListener itemListener) {
            this.mItemListener = itemListener;
        }
    }

    /**
     * Required override to disable/enable scrolling
     */
    static class CustomGridLayoutManager extends LinearLayoutManager {
        private boolean isScrollEnabled = true;

        public CustomGridLayoutManager(Context context) {
            super(context);
        }

        public void setScrollEnabled(boolean flag) {
            this.isScrollEnabled = flag;
        }

        @Override
        public boolean canScrollVertically() {
            //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
            return isScrollEnabled && super.canScrollVertically();
        }
    }
}
