package ru.linkstuff.friday;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.linkstuff.friday.HelperClasses.MyAdmin;
import ru.linkstuff.friday.Interfaces.RequestCodes;
import ru.linkstuff.friday.PhraseService.PhraseService;

import static android.Manifest.permission.*;

public class FirstStart extends Activity implements RequestCodes {

    SharedPreferences.Editor editor;
    PermissionAdapter adapter;
    SharedPreferences preferences;

    DevicePolicyManager devicePolicyManager;
    ComponentName componentName;

    int permissionCount = 9;

    String[] PREF_NAMES = {
            "ROOT", "ADMIN", "OVERLAY", "WSET"
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_first_start_layout);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        adapter = new PermissionAdapter(getItemList());
        adapter.setHasStableIds(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        if (!preferences.getBoolean("FIRST_DIALOG_SHOWED", false)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.firststart_dialog_title));
            builder.setMessage(getResources().getString(R.string.firststart_dialog_message));
            builder.setNegativeButton(
                    getResources().getString(R.string.firststart_dialog_negativebutton),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    }
            );
            builder.setPositiveButton(
                    getResources().getString(R.string.firststart_dialog_positivebutton),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editor.putBoolean("FIRST_DIALOG_SHOWED", true);
                            editor.apply();

                            dialogInterface.dismiss();
                        }
                    }
            );

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    private List<PermissionItem> getItemList(){
        List<PermissionItem> items = new ArrayList<>();

        items.add(new PermissionItem(
                getResources().getString(R.string.per_type_root),
                getResources().getString(R.string.per_type_root_desc),
                preferences.getBoolean(PREF_NAMES[0], false)
        ));

        if (devicePolicyManager == null && componentName == null){
            devicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
            componentName = new ComponentName(getApplicationContext(), MyAdmin.class);
        }

        items.add(new PermissionItem(
                getResources().getString(R.string.per_type_admin),
                getResources().getString(R.string.per_type_admin_desc),
                devicePolicyManager.isAdminActive(componentName)
        ));

        items.add(new PermissionItem(
                getResources().getString(R.string.per_type_overlay),
                getResources().getString(R.string.per_type_overlay_desc),
                preferences.getBoolean(PREF_NAMES[2], false)
        ));

        items.add(new PermissionItem(
                getResources().getString(R.string.per_type_wset),
                getResources().getString(R.string.per_type_wset_desc),
                preferences.getBoolean(PREF_NAMES[3], false)
        ));

        items.add(new PermissionItem(
                getResources().getString(R.string.per_type_call),
                getResources().getString(R.string.per_type_call_desc),
                checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        ));

        items.add(new PermissionItem(
                getResources().getString(R.string.per_type_storage),
                getResources().getString(R.string.per_type_storage_desc),
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ));

        items.add(new PermissionItem(
                getResources().getString(R.string.per_type_sms),
                getResources().getString(R.string.per_type_sms_desc),
                checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        ));

        items.add(new PermissionItem(
                getResources().getString(R.string.per_type_record),
                getResources().getString(R.string.per_type_record_desc),
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ));

        items.add(new PermissionItem(
                getResources().getString(R.string.per_type_location),
                getResources().getString(R.string.per_type_location_desc),
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ));

        return items;
    }

    public void getPermission(int permId){
        switch (permId){
            case 0:
                getRootAccess();
                --permissionCount;
                break;
            case 1:
                getAdminAccess();
                --permissionCount;
                break;
            case 2:
                getWindowOverlay();
                --permissionCount;
                break;
            case 3:
                getWriteSettingsPermission();
                --permissionCount;
                break;
            case 4:
                requestPermissions(new String[]{CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
                break;
            case 5:
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                break;
            case 6:
                requestPermissions(new String[]{SEND_SMS}, SEND_SMS_REQUEST_CODE);
                break;
            case 7:
                requestPermissions(new String[]{RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
                break;
            case 8:
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                break;
        }

        adapter.getItem(permId).setPermState(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean state = false;

        if (grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                --permissionCount;
                Log.d("Count", ""+permissionCount);
                state = true;
            }

            switch (requestCode){
                case ADMIN_ACCESS_REQUEST_CODE:
                    adapter.getItem(1).setPermState(state);
                    break;
                case CALL_PERMISSION_REQUEST_CODE:
                    adapter.getItem(4).setPermState(state);
                    break;

                case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                    adapter.getItem(5).setPermState(state);
                    break;

                case SEND_SMS_REQUEST_CODE:
                    adapter.getItem(6).setPermState(state);
                    break;

                case RECORD_AUDIO_REQUEST_CODE:
                    adapter.getItem(7).setPermState(state);
                    break;

                case LOCATION_PERMISSION_REQUEST_CODE:
                    adapter.getItem(8).setPermState(state);
                    break;
            }

            adapter.notifyDataSetChanged();
        }

        if (permissionCount == 0){
            editor.putBoolean("FIRST_START", false);
            editor.apply();

            startService(new Intent(getApplicationContext(), PhraseService.class));
            finish();
        }
    }

    private static void getRootAccess(){
        try {
            Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getAdminAccess(){
        devicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(getApplicationContext(), MyAdmin.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "fridayRequest");
        startActivityForResult(intent, ADMIN_ACCESS_REQUEST_CODE);
    }

    private void getWindowOverlay(){
        startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
    }

    private void getWriteSettingsPermission(){
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    //RECYCLER ADAPTER

    class PermissionAdapter extends RecyclerView.Adapter<PermissionViewHolder>{
        private List<PermissionItem> items;

        PermissionAdapter(List<PermissionItem> items){
            this.items = items;
        }

        PermissionItem getItem(int position){
            return items.get(position);
        }

        @NonNull
        @Override
        public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.first_start_item, parent, false);
            return new PermissionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
            PermissionItem item = items.get(position);

            holder.permType.setText(item.getPermType());
            holder.permDesc.setText(item.getPermDesc());

            if (item.getPermState()) {
                --permissionCount;
                Log.d("COUNT", ""+ permissionCount);
                holder.permEnabler.setChecked(true);
                holder.permEnabler.setEnabled(false);
            } else {
                holder.permEnabler.setChecked(false);
                holder.permEnabler.setClickable(true);
                holder.permEnabler.setEnabled(true);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    class PermissionViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener{
        CheckBox permEnabler;
        TextView permType;
        TextView permDesc;

        PermissionViewHolder(View itemView) {
            super(itemView);

            permType = itemView.findViewById(R.id.permission_type);
            permDesc = itemView.findViewById(R.id.permission_description);
            permEnabler = itemView.findViewById(R.id.permission_enabler);

            permEnabler.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                getPermission(getAdapterPosition());
                buttonView.setClickable(false);
            }
        }
    }

    class PermissionItem {
        private String permType;
        private String permDesc;
        private boolean permState;

        PermissionItem (String type, String desc, boolean state){
            permType = type;
            permDesc = desc;
            permState = state;
        }

        String getPermType() {
            return permType;
        }

        String getPermDesc() {
            return permDesc;
        }

        boolean getPermState() {
            return permState;
        }

        void setPermState(boolean permState) {
            this.permState = permState;
        }

    }

}
