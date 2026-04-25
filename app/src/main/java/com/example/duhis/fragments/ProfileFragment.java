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
import com.example.duhis.R;
import com.example.duhis.activities.LoginActivity;
import com.example.duhis.activities.ProfileActivity;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.SessionManager;
import com.example.duhis.utils.UIUtils;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView ivAvatar;
    private TextView tvName, tvEmail, tvPhone, tvRole;
    private Button btnEditProfile, btnLogout;
    private SessionManager session;

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

        ivAvatar      = view.findViewById(R.id.ivAvatar);
        tvName        = view.findViewById(R.id.tvName);
        tvEmail       = view.findViewById(R.id.tvEmail);
        tvPhone       = view.findViewById(R.id.tvPhone);
        tvRole        = view.findViewById(R.id.tvRole);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout     = view.findViewById(R.id.btnLogout);

        loadProfile();

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ProfileActivity.class)));

        btnLogout.setOnClickListener(v ->
                UIUtils.showConfirmDialog(requireContext(), "Logout",
                        "Are you sure you want to logout?", "Logout", this::logout));
    }

    private void loadProfile() {
        tvName.setText(session.getName());
        tvEmail.setText(session.getEmail());
        tvPhone.setText(session.getPhone());
        tvRole.setText(session.isAdmin() ? "Administrator" : "Community Member");

        String avatar = session.getAvatarUrl();
        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this).load(avatar)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(ivAvatar);
        }
    }

    private void logout() {
        String uid = session.getUid();
        if (uid != null) FirebaseHelper.getInstance().setUserOffline(uid);
        FirebaseAuth.getInstance().signOut();
        session.logout();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override public void onResume() { super.onResume(); loadProfile(); }
}