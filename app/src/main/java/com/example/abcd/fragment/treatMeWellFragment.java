package com.example.abcd.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.abcd.R;
import com.example.abcd.adapter.CardAdapter;
import java.util.ArrayList;
import java.util.Arrays;

public class treatMeWellFragment extends Fragment {
    private RecyclerView recyclerView;
    private CardAdapter adapter;

    public treatMeWellFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treat_me_well, container, false);


        return view;
    }
}