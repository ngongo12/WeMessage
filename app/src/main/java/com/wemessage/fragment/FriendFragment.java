package com.wemessage.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.wemessage.R;

public class FriendFragment extends Fragment {

    SuggestionFragment suggestionFragment;
    ReceiveFriendRequestFragment requestFragment;
    FragmentManager fm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fm = getFragmentManager();
        suggestionFragment = new SuggestionFragment();
        requestFragment = new ReceiveFriendRequestFragment();

        fm.beginTransaction().add(R.id.frmSuggestion, suggestionFragment).commit();
        fm.beginTransaction().add(R.id.frmReceivedRequest, requestFragment).commit();
    }
}
