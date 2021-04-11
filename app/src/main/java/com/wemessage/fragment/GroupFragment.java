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

public class GroupFragment extends Fragment {

    FragmentManager fm;
    MyGroupFragment myGroupFragment;
    GroupSuggestionFragment suggestionFragment;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fm = getFragmentManager();
        myGroupFragment = new MyGroupFragment();
        suggestionFragment = new GroupSuggestionFragment();

        fm.beginTransaction().add(R.id.frmMyGroup ,myGroupFragment).commit();
        fm.beginTransaction().add(R.id.frmGroupSuggestion ,suggestionFragment).commit();
    }
}
