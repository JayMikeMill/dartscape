package com.gworks.dartscape.fragments;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gworks.dartscape.database.PlayerDatabase;
import com.gworks.dartscape.dialogs.ConfirmDialog;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.MultiView;
import com.gworks.dartscape.ui.PlayersRecycleView;
import com.gworks.dartscape.R;

import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.UserPrefs;

/*
    PLAYERS DIALOG
 */
public class PlayersFragment extends Fragment {

    private PlayersRecycleView mRvPlayers;

    MultiView mBtnNew;
    MultiView mBtnEditSave;
    MultiView mBtnDelete;

    boolean mPlayersEdited = false;

    /** creates the view one time to save time. */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_players, container, false);

        super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize all game variables on first create.
        initControls();
    }

    @Override
    public void onResume() {
        super.onResume();
       if (mRvPlayers.isEditing())
            Helper.showKeyboardForced();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) {
            if (mPlayersEdited)
                ((HomeFragment) frags().getFragFromId
                        (FragManager.FragId.FRAG_HOME)).syncPlayersOnShow();
            return;
        }
        setLayoutEditing(false);

        mPlayersEdited = false;

        // initialize player fields
        mRvPlayers.refill();
        mRvPlayers.setSelectedIndex(NO_POSITION);

    }


    private void initControls() {
        mRvPlayers = requireView().findViewById(R.id.rvDiagPlayersList);
        mRvPlayers.setItemListener(itemListener);

        mBtnNew = requireView().findViewById(R.id.btnDiagPlayersNew);
        mBtnNew.setOnClickListener(v -> addPlayer());

        mBtnEditSave = requireView().findViewById(R.id.btnDiagPlayersEditSave);
        mBtnEditSave.setOnClickListener(v -> editSavePlayer());

        mBtnDelete = requireView().findViewById(R.id.btnDiagPlayersDelete);
        mBtnDelete.setOnClickListener(v -> deletePlayer());

    }

    public static final int FREE_VERSION_PLAYER_LIMIT = 4;

    private void addPlayer() {
        if (!UserPrefs.getOwnsPro() && mRvPlayers.getPlayerCount() >= FREE_VERSION_PLAYER_LIMIT) {
            Helper.dialogs().showConfirm(getString(R.string.pro_only), R.drawable.ic_dartboard,
                    getString(R.string.dialog_free_player_limit).replace("#", String.valueOf(FREE_VERSION_PLAYER_LIMIT)),
                    getString(R.string.buy_now), getString(R.string.maybe_later), false, dialog -> {
                        if(((ConfirmDialog) dialog).didConfirm())
                            Helper.activation().showBuyProDialog(); });

            return;
        }

        setLayoutEditing(true);

        mRvPlayers.addPlayer();
        mPlayersEdited = true;


    }

    private void editSavePlayer() {
        if (mRvPlayers.getSelectedIndex() == NO_POSITION) return;

        if (mRvPlayers.isEditing()) {
            if (!mRvPlayers.isEditing())
                setLayoutEditing(false);
            mRvPlayers.savePlayers();
            mPlayersEdited = true;
        } else {
            setLayoutEditing(true);
            mRvPlayers.editSelected();

        }
    }

    private void deletePlayer() {
        boolean itemSelected = mRvPlayers.getSelectedIndex() != NO_POSITION;

        if (mRvPlayers.isEditing() & itemSelected) {
            setLayoutEditing(false);

            if (mRvPlayers.isAddingPlayer())
                mRvPlayers.deleteSelected();

            mRvPlayers.stopEditing();

            return;
        }

        if (itemSelected) {
            mRvPlayers.deleteSelected();
            mPlayersEdited = true;
        }

    }

    private void setLayoutEditing(boolean editing) {
        boolean itemSelected = mRvPlayers.getSelectedIndex() != NO_POSITION;
        ((DartScapeActivity) requireContext()).frags().titleBar().setBackButtonActive(!editing);

        mBtnNew.setEnabled(!(editing && itemSelected));

        mBtnDelete.setEnabled(editing ||  itemSelected);
        mBtnDelete.setText(editing & itemSelected ?
                getString(R.string.cancel) : getString(R.string.delete));

        mBtnEditSave.setEnabled(itemSelected);
        mBtnEditSave.setText(editing & itemSelected ?
                getString(R.string.save) : getString(R.string.edit));


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Helper.closeKeyboard();
    }

    PlayersRecycleView.ItemListener itemListener =
            new PlayersRecycleView.ItemListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (!mRvPlayers.isEditing())
                setLayoutEditing(false);
        }

        @Override
        public void onItemEdited() {
            setLayoutEditing(false);
        }
    };


    /** grab the global player database */
    private PlayerDatabase db() {
        return ((DartScapeActivity)requireContext()).playerDB();
    }
    private FragManager frags() {
        return ((DartScapeActivity) requireContext()).frags();
    }
}
