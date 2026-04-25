package com.example.duhis.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.Query;
import com.example.duhis.R;
import com.example.duhis.adapters.UserAdapter;
import com.example.duhis.models.User;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private TextView tvEmpty;
    private ImageView ivClearSearch;

    private UserAdapter adapter;
    private final List<User> allUsers = new ArrayList<>();
    private final List<User> filteredUsers = new ArrayList<>();
    private FirebaseHelper fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        fb = FirebaseHelper.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Users");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        setupSearch();
        loadUsers();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        rvUsers = findViewById(R.id.rvUsers);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
        ivClearSearch = findViewById(R.id.ivClearSearch);

        adapter = new UserAdapter(filteredUsers, this::showUserOptions);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.duhis_green);
        swipeRefresh.setOnRefreshListener(this::loadUsers);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            filterUsers("");
        });
    }

    private void loadUsers() {
        swipeRefresh.setRefreshing(true);
        fb.users()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    swipeRefresh.setRefreshing(false);
                    allUsers.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setUid(doc.getId());
                            allUsers.add(user);
                        }
                    }
                    filterUsers(etSearch.getText().toString());
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    UIUtils.showToast(this, "Failed to load users: " + e.getMessage());
                });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        String q = query.toLowerCase(Locale.getDefault()).trim();

        for (User user : allUsers) {
            if (q.isEmpty() ||
                    user.getFullName().toLowerCase(Locale.getDefault()).contains(q) ||
                    user.getEmail().toLowerCase(Locale.getDefault()).contains(q) ||
                    (user.getPhoneNumber() != null && user.getPhoneNumber().contains(q))) {
                filteredUsers.add(user);
            }
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredUsers.isEmpty() ? View.VISIBLE : View.GONE);
        ivClearSearch.setVisibility(!q.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showUserOptions(User user) {
        String[] options;
        if ("admin".equals(user.getRole())) {
            options = new String[]{"View Details", "Remove Admin", "Delete User"};
        } else {
            options = new String[]{"View Details", "Make Admin", "Delete User"};
        }

        UIUtils.showOptionDialog(this, user.getFullName(), options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showUserDetails(user);
                    break;
                case 1:
                    toggleAdminRole(user);
                    break;
                case 2:
                    confirmDeleteUser(user);
                    break;
            }
        });
    }

    private void showUserDetails(User user) {
        UIUtils.showInfoDialog(this,
                "User Details",
                "Name: " + user.getFullName() + "\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Phone: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A") + "\n" +
                        "Role: " + user.getRole() + "\n" +
                        "Member Since: " + (user.getCreatedAt() != null ?
                        UIUtils.formatDate(user.getCreatedAt().toDate()) : "N/A"),
                "OK",
                null);
    }

    private void toggleAdminRole(User user) {
        String newRole = "admin".equals(user.getRole()) ? "user" : "admin";
        String message = "Are you sure you want to " +
                ("admin".equals(user.getRole()) ? "remove admin privileges from" : "make") +
                " " + user.getFullName() + " an admin?";

        UIUtils.showConfirmDialog(this, "Change Role", message, "Confirm", () -> {
            fb.users().document(user.getUid())
                    .update("role", newRole)
                    .addOnSuccessListener(v -> {
                        user.setRole(newRole);
                        adapter.notifyDataSetChanged();
                        UIUtils.showToast(this, "User role updated successfully");
                    })
                    .addOnFailureListener(e ->
                            UIUtils.showToast(this, "Failed to update role: " + e.getMessage()));
        });
    }

    private void confirmDeleteUser(User user) {
        UIUtils.showConfirmDialog(this,
                "Delete User",
                "Are you sure you want to delete " + user.getFullName() + "? This action cannot be undone.",
                "Delete",
                () -> {
                    fb.users().document(user.getUid())
                            .delete()
                            .addOnSuccessListener(v -> {
                                allUsers.remove(user);
                                filterUsers(etSearch.getText().toString());
                                UIUtils.showToast(this, "User deleted successfully");
                            })
                            .addOnFailureListener(e ->
                                    UIUtils.showToast(this, "Failed to delete user: " + e.getMessage()));
                });
    }
}