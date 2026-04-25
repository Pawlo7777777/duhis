package com.example.duhis.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.duhis.R;
import com.example.duhis.activities.AppointmentActivity;
import com.example.duhis.activities.HealthInfoActivity;
import com.example.duhis.activities.NotificationsActivity;
import com.example.duhis.adapters.HealthInfoAdapter;
import com.example.duhis.models.HealthInfo;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvUserName;
    private RecyclerView rvFeatured;
    private SwipeRefreshLayout swipeRefresh;
    private View cardAppointment, cardHealthInfo, cardEmergency, cardNotifications;

    private HealthInfoAdapter adapter;
    private final List<HealthInfo> featuredList = new ArrayList<>();
    private FirebaseHelper fb;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fb = FirebaseHelper.getInstance();
        session = new SessionManager(requireContext());

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserName = view.findViewById(R.id.tvUserName);
        rvFeatured = view.findViewById(R.id.rvFeatured);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        cardAppointment = view.findViewById(R.id.cardAppointment);
        cardHealthInfo = view.findViewById(R.id.cardHealthInfo);
        cardEmergency = view.findViewById(R.id.cardEmergency);
        cardNotifications = view.findViewById(R.id.cardNotifications);

        tvGreeting.setText(UIUtils.getGreeting() + ",");
        tvUserName.setText(session.getName() != null ? session.getName() : "");

        adapter = new HealthInfoAdapter(featuredList, item -> {
            Intent intent = new Intent(getActivity(), HealthInfoActivity.class);
            intent.putExtra("infoId", item.getInfoId());
            startActivity(intent);
        });
        rvFeatured.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeatured.setAdapter(adapter);

        setupCardClicks();
        loadFeaturedHealth();

        swipeRefresh.setColorSchemeResources(R.color.duhis_green);
        swipeRefresh.setOnRefreshListener(this::loadFeaturedHealth);
    }

    private void setupCardClicks() {
        cardAppointment.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AppointmentActivity.class)));
        cardHealthInfo.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), HealthInfoActivity.class)));
        cardEmergency.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.duhis.MainActivity) {
                ((com.example.duhis.MainActivity) getActivity())
                        .navigateTo(R.id.nav_emergency);
            }
        });
        cardNotifications.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), NotificationsActivity.class)));
    }

    private void loadFeaturedHealth() {
        fb.healthInfo()
                .whereEqualTo("isFeatured", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(snap -> {
                    swipeRefresh.setRefreshing(false);
                    featuredList.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        HealthInfo info = doc.toObject(HealthInfo.class);
                        if (info != null) {
                            info.setInfoId(doc.getId());
                            featuredList.add(info);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Optional: Show message if no featured items
                    if (featuredList.isEmpty()) {
                        UIUtils.showToast(requireContext(), "No featured health information available");
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    UIUtils.showToast(requireContext(), "Failed to load health information: " + e.getMessage());
                });
    }
}