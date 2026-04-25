package com.example.duhis.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.Query;
import com.example.duhis.R;
import com.example.duhis.activities.HealthInfoDetailActivity;
import com.example.duhis.adapters.HealthInfoAdapter;
import com.example.duhis.models.HealthInfo;
import com.example.duhis.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HealthInfoFragment extends Fragment {

    private RecyclerView rvHealthInfo;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private TextView tvEmpty;

    private HealthInfoAdapter adapter;
    private final List<HealthInfo> allList      = new ArrayList<>();
    private final List<HealthInfo> filteredList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health_info, container, false);
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHealthInfo = view.findViewById(R.id.rvHealthInfo);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        etSearch     = view.findViewById(R.id.etSearch);
        tvEmpty      = view.findViewById(R.id.tvEmpty);

        adapter = new HealthInfoAdapter(filteredList, item -> {
            Intent intent = new Intent(getActivity(), HealthInfoDetailActivity.class);
            intent.putExtra("infoId", item.getInfoId());
            startActivity(intent);
        });
        rvHealthInfo.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHealthInfo.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.duhis_green);
        swipeRefresh.setOnRefreshListener(this::loadHealthInfo);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filterList(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadHealthInfo();
    }

    private void loadHealthInfo() {
        FirebaseHelper.getInstance().healthInfo()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    swipeRefresh.setRefreshing(false);
                    allList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        HealthInfo h = doc.toObject(HealthInfo.class);
                        if (h != null) { h.setInfoId(doc.getId()); allList.add(h); }
                    }
                    filterList(etSearch.getText().toString());
                })
                .addOnFailureListener(e -> swipeRefresh.setRefreshing(false));
    }

    private void filterList(String query) {
        filteredList.clear();
        String q = query.toLowerCase(Locale.getDefault()).trim();
        for (HealthInfo h : allList) {
            if (q.isEmpty()
                    || h.getTitle().toLowerCase(Locale.getDefault()).contains(q)
                    || h.getCategory().toLowerCase(Locale.getDefault()).contains(q)) {
                filteredList.add(h);
            }
        }
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() { super.onResume(); loadHealthInfo(); }
}