package com.luminus.labs.noland.plugins.AppShortcut;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.luminus.labs.noland.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AppShortcutPickerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private EditText searchEditText;
    private SharedPreferences sharedPreferences;
    private List<AppShortcutIsland.AppInfo> allApps = new ArrayList<>();
    private List<AppShortcutIsland.AppInfo> filteredApps = new ArrayList<>();
    private Set<String> selectedPackages;
    private static final String PREF_SELECTED_APPS = "app_shortcut_selected_apps";
    private static final int MAX_SHORTCUTS = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_shortcut_picker);
        
        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        recyclerView = findViewById(R.id.app_list_recycler);
        searchEditText = findViewById(R.id.search_apps);
        MaterialButton saveButton = findViewById(R.id.save_button);
        MaterialButton cancelButton = findViewById(R.id.cancel_button);
        
        selectedPackages = new HashSet<>(sharedPreferences.getStringSet(PREF_SELECTED_APPS, new HashSet<>()));
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppAdapter();
        recyclerView.setAdapter(adapter);

        loadAllApps();
        
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        saveButton.setOnClickListener(v -> saveSelection());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadAllApps() {
        new Thread(() -> {
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            List<AppShortcutIsland.AppInfo> loadedApps = new ArrayList<>();
            
            for (ApplicationInfo packageInfo : packages) {
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                    try {
                        String appName = pm.getApplicationLabel(packageInfo).toString();
                        Drawable icon = pm.getApplicationIcon(packageInfo);
                        loadedApps.add(new AppShortcutIsland.AppInfo(appName, packageInfo.packageName, icon));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            Collections.sort(loadedApps, (a, b) -> a.appName.compareToIgnoreCase(b.appName));
            
            runOnUiThread(() -> {
                allApps = loadedApps;
                filteredApps = new ArrayList<>(allApps);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void filterApps(String query) {
        filteredApps = allApps.stream()
                .filter(app -> app.appName.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        adapter.notifyDataSetChanged();
    }

    private void saveSelection() {
        sharedPreferences.edit().putStringSet(PREF_SELECTED_APPS, selectedPackages).apply();
        Toast.makeText(this, "Apps saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_picker_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppShortcutIsland.AppInfo app = filteredApps.get(position);
            holder.label.setText(app.appName);
            holder.icon.setImageDrawable(app.icon);
            
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(selectedPackages.contains(app.packageName));
            
            boolean canSelectMore = selectedPackages.size() < MAX_SHORTCUTS;
            holder.checkBox.setEnabled(holder.checkBox.isChecked() || canSelectMore);
            
            holder.itemView.setOnClickListener(v -> {
                boolean isChecked = !holder.checkBox.isChecked();
                if (isChecked) {
                    if (selectedPackages.size() < MAX_SHORTCUTS) {
                        selectedPackages.add(app.packageName);
                        holder.checkBox.setChecked(true);
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(AppShortcutPickerActivity.this, 
                                "Maximum " + MAX_SHORTCUTS + " apps allowed", 
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedPackages.remove(app.packageName);
                    holder.checkBox.setChecked(false);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return filteredApps.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView label;
            MaterialCheckBox checkBox;

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.app_icon);
                label = itemView.findViewById(R.id.app_label);
                checkBox = itemView.findViewById(R.id.app_checkbox);
            }
        }
    }
}