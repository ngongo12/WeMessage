package com.wemessage.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wemessage.MainActivity;
import com.wemessage.R;

public class OtherFragment extends Fragment {

    TextView tvName;
    LinearLayout layoutEdit;

    DatabaseReference rootRef, userRef;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_other, container, false);

        //Khởi tạo các biến dành cho firebase

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Ánh xạ các view
        tvName = getView().findViewById(R.id.tvName);
        layoutEdit = getView().findViewById(R.id.layoutEdit);
        
        layoutEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).logout();
            }
        });
    }

}
