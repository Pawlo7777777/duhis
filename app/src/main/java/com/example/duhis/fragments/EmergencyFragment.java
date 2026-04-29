package com.example.duhis.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duhis.R;
import com.example.duhis.adapters.EmergencyContactAdapter;
import com.example.duhis.models.EmergencyContact;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.UIUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmergencyFragment extends Fragment {

    private RecyclerView rvContacts;
    private final List<EmergencyContact> contactList = new ArrayList<>();
    private EmergencyContactAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvContacts = view.findViewById(R.id.rvContacts);

        adapter = new EmergencyContactAdapter(contactList, this::callNumber);
        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvContacts.setAdapter(adapter);

        loadContacts();
    }

    private void loadContacts() {
        FirebaseHelper.getInstance(requireContext()).emergencyContacts()
                .orderByChild("sortOrder")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        contactList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            EmergencyContact c = child.getValue(EmergencyContact.class);
                            if (c != null) {
                                c.setContactId(child.getKey());
                                contactList.add(c);
                            }
                        }
                        if (contactList.isEmpty()) addDefaultContacts();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        addDefaultContacts();
                    }
                });
    }

    /** Seed default contacts if Realtime DB is empty */
    private void addDefaultContacts() {
        contactList.clear();
        contactList.add(new EmergencyContact("Barangay Health Center", "Dagohoy BHC",             "09123456789", "health_center", 1));
        contactList.add(new EmergencyContact("Municipal Hospital",     "Dagohoy Municipal Hospital", "09123456789", "hospital",      2));
        contactList.add(new EmergencyContact("Ambulance",              "Emergency Ambulance",        "09123456789", "ambulance",     3));
        contactList.add(new EmergencyContact("BFP",                    "Bureau of Fire Protection",  "09123456789", "fire",          4));
        contactList.add(new EmergencyContact("PNP",                    "Philippine National Police", "911",          "police",        5));
        adapter.notifyDataSetChanged();
    }

    private void callNumber(String number) {
        UIUtils.showConfirmDialog(requireContext(),
                "Call " + number,
                "Do you want to call this number?",
                "Call",
                () -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + number.replaceAll("\\s", "")));
                    startActivity(intent);
                });
    }
}