// adminhomeFragment.java
package com.example.abcd.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.abcd.R;
import com.example.abcd.adminfeature.AllAdminDisplay;
import com.example.abcd.adminfeature.TotalActivityToday;
import com.example.abcd.selectChatModel;
import com.example.abcd.userSearch.userSearchingActivity;
import com.example.abcd.adminfeature.insertAiApiKey;
import com.example.abcd.utils.SessionManager;
import com.example.abcd.view.CustomGraphView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class adminhomeFragment extends Fragment {

    private TextView tvWelcome, tvDate, tvTotalUsers, tvActiveUsers;
    private CustomGraphView graphView;
    private ImageView ivProfile;
    private MaterialCardView  activeUsersCard;
    private DatabaseReference databaseReference;
    private MaterialButton btnViewAll, btnInsertApiKey; // Removed btnMessageEditView
    private MaterialCardView totalUsersCard;
    private SessionManager sessionManager;
    private String email;

    public adminhomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adminhome, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        sessionManager = new SessionManager(requireContext());
        email = sessionManager.getEmail();

        setDynamicDate();
        loadProfileImageFromFirebase();
        setupClickListeners();
        loadDashboardData();
        loadTodaysLogins();
        addGraphViewToActivityCard(view);
        loadAnalyticsDataForGraph();
    }

    private void initializeViews(View view) {
        activeUsersCard = view.findViewById(R.id.activeUsersCard);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvDate = view.findViewById(R.id.tvDate);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvActiveUsers = view.findViewById(R.id.tvActiveUsers);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnViewAll = view.findViewById(R.id.btnViewAll);
        btnInsertApiKey = view.findViewById(R.id.btnInsertApiKey); // Now outside card
        totalUsersCard = view.findViewById(R.id.totalUsersCard);
    }

    private void addGraphViewToActivityCard(View view) {
        ViewGroup activityCard = view.findViewById(R.id.activityCard);

        if (activityCard != null) {
            LinearLayout activityCardContent = null;
            for (int i = 0; i < activityCard.getChildCount(); i++) {
                View child = activityCard.getChildAt(i);
                if (child instanceof LinearLayout) {
                    activityCardContent = (LinearLayout) child;
                    break;
                }
            }

            if (activityCardContent != null) {
                graphView = new CustomGraphView(requireContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(250));
                graphView.setLayoutParams(params);

                int insertPosition = 0;
                activityCardContent.addView(graphView, insertPosition);
            } else {
                Toast.makeText(requireContext(), "Failed to find content layout for graph", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Failed to find activity card", Toast.LENGTH_SHORT).show();
        }
    }

    private void setDynamicDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        tvDate.setText(currentDate);
    }

    private void loadProfileImageFromFirebase() {
        if (email != null && isAdded() && getContext() != null) {
            String sanitizedEmail = email.replace(".", ",");
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(sanitizedEmail);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && isAdded()) {
                        String imageUrl = dataSnapshot.child("imageSend").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_default_profile)
                                    .error(R.drawable.ic_default_profile)
                                    .circleCrop()
                                    .into(ivProfile);
                        } else {
                            ivProfile.setImageResource(R.drawable.ic_default_profile);
                        }

                        String username = dataSnapshot.child("user").getValue(String.class);
                        if (username != null) {
                            tvWelcome.setText("Welcome, " + username);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (isAdded() && getContext() != null) {
                        ivProfile.setImageResource(R.drawable.ic_default_profile);
                        Toast.makeText(requireContext(),
                                "Failed to load profile: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (isAdded() && getContext() != null) {
            ivProfile.setImageResource(R.drawable.ic_default_profile);
        }
    }

    private void setupClickListeners() {
        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AllAdminDisplay.class);
            startActivity(intent);
        });

        btnInsertApiKey.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), insertAiApiKey.class);
            startActivity(intent);
        });

        if (totalUsersCard != null) {
            totalUsersCard.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), userSearchingActivity.class);
                startActivity(intent);
            });
        }

        if (activeUsersCard != null) {
            activeUsersCard.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), TotalActivityToday.class);
                startActivity(intent);
            });
        }
    }

    private void loadDashboardData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isAdded() && getContext() != null) {
                    long totalUsers = snapshot.getChildrenCount();
                    tvTotalUsers.setText(String.valueOf(totalUsers));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(requireContext(),
                            "Failed to load dashboard data: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadAnalyticsDataForGraph() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Map<String, String> datesToFetch = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            String date = dateFormat.format(calendar.getTime());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
            datesToFetch.put(date, displayFormat.format(calendar.getTime()));
        }

        Map<String, Integer> graphData = new TreeMap<>();

        databaseReference.child("analytics").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (String date : datesToFetch.keySet()) {
                    String displayDate = datesToFetch.get(date);
                    if (dataSnapshot.hasChild(date) &&
                            dataSnapshot.child(date).hasChild("totalAppViews")) {
                        Integer views = dataSnapshot.child(date).child("totalAppViews").getValue(Integer.class);
                        graphData.put(displayDate, views != null ? views : 0);
                    } else {
                        graphData.put(displayDate, 0);
                    }
                }
                if (graphView != null) {
                    graphView.setData(graphData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (graphView != null) {
                    graphView.setData(new HashMap<>());
                }
            }
        });
    }

    private void loadTodaysLogins() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        databaseReference.child("analytics").child(todayDate).child("totalAppViews")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Long totalAppViews = snapshot.getValue(Long.class);
                            tvActiveUsers.setText(String.valueOf(totalAppViews != null ? totalAppViews : 0));
                        } else {
                            tvActiveUsers.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvActiveUsers.setText("Error");
                    }
                });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}