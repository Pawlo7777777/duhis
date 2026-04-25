package com.example.duhis.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.example.duhis.R;
import com.example.duhis.adapters.HealthInfoAdapter;
import com.example.duhis.models.HealthInfo;
import com.example.duhis.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HealthInfoActivity extends AppCompatActivity {

    private RecyclerView rvHealthInfo;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private TextView tvEmpty;
    private HealthInfoAdapter adapter;
    private final List<HealthInfo> allList      = new ArrayList<>();
    private final List<HealthInfo> filteredList = new ArrayList<>();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Health Information");
        toolbar.setNavigationOnClickListener(v -> finish());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvHealthInfo = findViewById(R.id.rvHealthInfo);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        etSearch     = findViewById(R.id.etSearch);
        tvEmpty      = findViewById(R.id.tvEmpty);

        adapter = new HealthInfoAdapter(filteredList, item -> {
            Intent intent = new Intent(this, HealthInfoDetailActivity.class);
            intent.putExtra("infoId", item.getInfoId());
            startActivity(intent);
        });
        rvHealthInfo.setLayoutManager(new LinearLayoutManager(this));
        rvHealthInfo.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.duhis_green);
        swipeRefresh.setOnRefreshListener(this::loadHealthInfo);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filter(s.toString()); }
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
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        HealthInfo h = doc.toObject(HealthInfo.class);
                        if (h != null) {
                            h.setInfoId(doc.getId());
                            allList.add(h);
                        }
                    }
                    filter(etSearch.getText().toString());
                })
                .addOnFailureListener(e -> swipeRefresh.setRefreshing(false));
    }

    private void filter(String query) {
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
}