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
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treat_me_well, container, false);
        
        recyclerView = view.findViewById(R.id.cardsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Initialize with 6 cards using placeholder images
        ArrayList<Integer> cards = new ArrayList<>(Arrays.asList(
            R.drawable.dark_gradient_background,
            R.drawable.dark_gradient_background,
            R.drawable.dark_gradient_background,
            R.drawable.dark_gradient_background,
            R.drawable.dark_gradient_background,
            R.drawable.dark_gradient_background
        ));

        adapter = new CardAdapter(cards);
        recyclerView.setAdapter(adapter);

        // Set up swipe and drag functionality
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                adapter.swapItems(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not implementing swipe functionality
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
        return view;
    }
}