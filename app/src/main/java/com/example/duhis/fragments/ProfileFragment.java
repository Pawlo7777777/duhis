package com.example.duhis.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.duhis.R;
import com.example.duhis.activities.LoginActivity;
import com.example.duhis.activities.ProfileActivity;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView ivAvatar;
    private TextView tvName, tvEmail, tvPhone, tvRole, tvMemberSince;
    private Button btnEditProfile, btnLogout;
    private SessionManager session;
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());
        firebaseHelper = FirebaseHelper.getInstance(requireContext());

        ivAvatar       = view.findViewById(R.id.ivAvatar);
        tvName         = view.findViewById(R.id.tvName);
        tvEmail        = view.findViewById(R.id.tvEmail);
        tvPhone        = view.findViewById(R.id.tvPhone);
        tvRole         = view.findViewById(R.id.tvRole);
        tvMemberSince  = view.findViewById(R.id.tvMemberSince);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout      = view.findViewById(R.id.btnLogout);

        loadProfile();
        loadMemberSince();

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ProfileActivity.class)));

        btnLogout.setOnClickListener(v ->
                UIUtils.showConfirmDialog(requireContext(), "Logout",
                        "Are you sure you want to logout?", "Logout", this::logout));
    }

    private void loadProfile() {
        // First load from SessionManager (fast)
        tvName.setText(session.getName() != null ? session.getName() : "No name set");
        tvEmail.setText(session.getEmail() != null ? session.getEmail() : "No email");
        tvPhone.setText(session.getPhone() != null ? session.getPhone() : "Not provided");
        tvRole.setText(session.isAdmin() ? "Administrator" : "Community Member");

        // Load avatar from session first (fast display)
        String avatarUrl = session.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }

        // Then fetch latest from Firebase to ensure we have the most up-to-date data
        loadLatestFromFirebase();
    }

    private void loadLatestFromFirebase() {
        String uid = session.getUid();
        if (uid == null) return;

        firebaseHelper.users().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || getActivity() == null) return;

                // Update name if changed
                String fullName = snapshot.child("fullName").getValue(String.class);
                if (fullName != null && !fullName.isEmpty() && !fullName.equals(session.getName())) {
                    tvName.setText(fullName);
                    session.updateName(fullName);
                }

                // Update phone if changed
                String phone = snapshot.child("phoneNumber").getValue(String.class);
                if (phone != null && !phone.isEmpty() && !phone.equals(session.getPhone())) {
                    tvPhone.setText(phone);
                }

                // Update avatar URL if changed
                String avatarUrl = snapshot.child("profileImageUrl").getValue(String.class);
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    // Only update if the URL is different from what we have in session
                    if (!avatarUrl.equals(session.getAvatarUrl())) {
                        session.updateAvatar(avatarUrl);
                        // Reload avatar with new URL
                        Glide.with(ProfileFragment.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.ic_person_placeholder)
                                .error(R.drawable.ic_person_placeholder)
                                .circleCrop()
                                .into(ivAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Keep using session data, already displayed
            }
        });
    }

    private void loadMemberSince() {
        String uid = session.getUid();
        if (uid == null) return;

        firebaseHelper.users()
                .child(uid)
                .child("createdAt")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || tvMemberSince == null) return;

                        Long seconds = null;

                        // Handle different timestamp formats
                        if (snapshot.hasChild("seconds")) {
                            seconds = snapshot.child("seconds").getValue(Long.class);
                        } else if (snapshot.getValue() instanceof Long) {
                            seconds = snapshot.getValue(Long.class);
                        } else if (snapshot.getValue() instanceof String) {
                            try {
                                seconds = Long.parseLong(snapshot.getValue(String.class));
                            } catch (NumberFormatException e) {
                                // Not a valid number
                            }
                        }

                        if (seconds != null) {
                            // Firebase Timestamp seconds → milliseconds
                            long millis = seconds * 1000L;
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                            String formatted = sdf.format(new Date(millis));
                            tvMemberSince.setText(formatted);
                        } else {
                            tvMemberSince.setText("—");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (tvMemberSince != null) tvMemberSince.setText("—");
                    }
                });
    }

    private void logout() {
        String uid = session.getUid();
        if (uid != null && firebaseHelper != null) {
            firebaseHelper.setUserOffline(uid);
        }
        FirebaseAuth.getInstance().signOut();
        session.logout();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile data when returning from ProfileActivity
        loadProfile();
        loadMemberSince();
    }
}