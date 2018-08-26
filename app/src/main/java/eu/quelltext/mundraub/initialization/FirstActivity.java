package eu.quelltext.mundraub.initialization;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.error.ErrorAwareActivity;

public class FirstActivity extends ErrorAwareActivity {

    private final List<Permission> allPermissions = new ArrayList<Permission>();
    private final boolean CAN_ASK_FOR_PERMISSIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    static private int nextPermissionQuestionId = 100;

    private Activity getActivity() {
        return this;
    }

    class Permission {

        private final int id;
        private final String permissionName;
        private boolean isRequested = false;
        private int purposeResourceId;
        private boolean requestDeclined = false;

        Permission(String permissionName, int purposeResourceId) {
            this.id = ++nextPermissionQuestionId;
            this.permissionName = permissionName;
            this.purposeResourceId = purposeResourceId;
        }

        private boolean isGranted() {
            // from https://stackoverflow.com/a/38366540
            return ContextCompat.checkSelfPermission(getActivity(), permissionName) == PackageManager.PERMISSION_GRANTED;
        }

        private boolean isRequested() {
            return isRequested;
        }

        private void request() {
            if (CAN_ASK_FOR_PERMISSIONS) {
                askForPermissionDialog();
            } else {
                alertAboutPermission();
            }
        }

        private void alertAboutPermission() {
            String message = getResources().getString(purposeResourceId) + "\n" +
                    getResources().getString(R.string.permission_error_no_api);
            new Dialog(getActivity()).alertError(message);
            isRequested = true;
        }

        private void askForPermissionDialog() {
            new Dialog(getActivity()).askYesNo(purposeResourceId, R.string.permission_request, new Dialog.YesNoCallback() {
                @Override
                public void yes() {
                    requestDeclined = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{permissionName}, id);
                    } else {
                        new Dialog(getApplicationContext()).alertError(R.string.permission_error_no_api);
                    }
                }

                @Override
                public void no() {
                    requestDeclined = true;
                }
            });
            isRequested = true;
        }

        private void check() {
            if (!isGranted() && !isRequested()) {
                request();
            }
        }

        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode != id) {
                return;
            }
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i] == permissionName) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onGranted();
                    } else {
                        onDenied();
                    }
                }
            }
        }

        private void onDenied() {
            String message = getResources().getString(purposeResourceId) + "\n" +
                    getResources().getString(R.string.permission_denied_consequences);
            new Dialog(getActivity()).alertError(message);
        }

        private void onGranted() {
        }
    }

    public FirstActivity() {
        super();
        allPermissions.add(new Permission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_purpose_access_fine_location));
        allPermissions.add(new Permission(Manifest.permission.CAMERA, R.string.permission_purpose_camera));
        allPermissions.add(new Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_purpose_external_storage));
        allPermissions.add(new Permission(Manifest.permission.INTERNET, R.string.permission_purpose_internet));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Initialization.provideContext(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAllPermissions();
    }

    private void checkAllPermissions() {
        for (Permission permission : allPermissions) {
            permission.check();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (Permission permission : allPermissions) {
            permission.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
