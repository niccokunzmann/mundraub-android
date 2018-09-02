package eu.quelltext.mundraub.initialization;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Settings;

public class Permissions {
    private final List<Permission> allPermissions = new ArrayList<Permission>();
    public static final boolean CAN_ASK_FOR_PERMISSIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    private static SharedPermissionState STATE_ACCESS_FINE_LOCATION = new SharedPermissionState(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_purpose_access_fine_location);
    private static SharedPermissionState STATE_CAMERA = new SharedPermissionState(Manifest.permission.CAMERA, R.string.permission_purpose_camera);
    private static SharedPermissionState STATE_WRITE_EXTERNAL_STORAGE = new SharedPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_purpose_external_storage);
    private static SharedPermissionState STATE_INTERNET = new SharedPermissionState(Manifest.permission.INTERNET, R.string.permission_purpose_internet);


    private final Activity activity;
    public final Permission ACCESS_FINE_LOCATION;
    public final Permission CAMERA;
    public final Permission WRITE_EXTERNAL_STORAGE;
    public final Permission INTERNET;

    public static Permissions of(Activity activity) {
        return new Permissions(activity);
    }
    
    private Permissions(Activity activity) {
        this.activity = activity;
        this.ACCESS_FINE_LOCATION = new Permission(STATE_ACCESS_FINE_LOCATION);
        this.CAMERA = new Permission(STATE_CAMERA);
        this.WRITE_EXTERNAL_STORAGE = new Permission(STATE_WRITE_EXTERNAL_STORAGE);
        this.INTERNET = new Permission(STATE_INTERNET);
    }

    /* All permissions share a common state about whether they were asked or not. */
    static private int nextPermissionQuestionId = 100;
    static class SharedPermissionState {
        private final int id;
        private final String permissionName;
        private boolean isRequested = false;
        private final int purposeResourceId;
        private boolean requestDeclined = false;

        SharedPermissionState(String permissionName, int purposeResourceId) {
            this.id = ++nextPermissionQuestionId;
            this.permissionName = permissionName;
            this.purposeResourceId = purposeResourceId;
        }
    }

    public class Permission {

        private final SharedPermissionState state; // there shall be no other field for global state
        private List<PermissionChange> listeners = new ArrayList<PermissionChange>();

        private Permission(SharedPermissionState state) {
            this.state = state;
            allPermissions.add(this);
        }

        public boolean isGranted() {
            // from https://stackoverflow.com/a/38366540
            return ContextCompat.checkSelfPermission(activity, state.permissionName) == PackageManager.PERMISSION_GRANTED;
        }

        private boolean isRequested() {
            return state.isRequested;
        }

        private void request() {
            if (CAN_ASK_FOR_PERMISSIONS) {
                askForPermissionDialog();
            } else {
                alertAboutPermission();
            }
        }

        private void alertAboutPermission() {
            String message = activity.getResources().getString(state.purposeResourceId) + "\n" +
                    activity.getResources().getString(R.string.permission_error_no_api);
            new Dialog(activity).alertError(message);
            state.isRequested = true;
        }

        private void askForPermissionDialog() {
            new Dialog(activity).askYesNo(state.purposeResourceId, R.string.permission_request, new Dialog.YesNoCallback() {
                @Override
                public void yes() {
                    state.requestDeclined = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        activity.requestPermissions(new String[]{state.permissionName}, state.id);
                    } else {
                        new Dialog(activity).alertError(R.string.permission_error_no_api);
                    }
                }

                @Override
                public void no() {
                    state.requestDeclined = true;
                }
            });
            state.isRequested = true;
        }

        public void check() {
            if (!isGranted() && !isRequested()) {
                request();
            }
        }

        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode != state.id) {
                return;
            }
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i] == state.permissionName) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onGranted();
                    } else {
                        onDenied();
                    }
                }
            }
        }

        private void onDenied() {
            String message = activity.getResources().getString(state.purposeResourceId) + "\n" +
                    activity.getResources().getString(R.string.permission_denied_consequences);
            new Dialog(activity).alertError(message);
            for (PermissionChange listener : listeners) {
                listener.onDenied(this);
            }
        }

        private void onGranted() {
            for (PermissionChange listener : listeners) {
                listener.onGranted(this);
            }
        }

        public void onChange(PermissionChange listener) {
            listeners.add(listener);
        }

        public boolean canAsk() {
            return Settings.canAskForPermissionNamed(state.permissionName);
        }

        public int canAsk(boolean canAsk) {
            return Settings.canAskForPermissionNamed(state.permissionName, canAsk);
        }

        /* This should be used by activities other than the Settings to ask for permissions. */
        public boolean askIfNotGranted() {
            if (canAsk()) {
                check();
            }
            return isGranted();
        }
    }

    public interface PermissionChange {
        void onGranted(Permission permission);
        void onDenied(Permission permission);
    }

    public void checkAllPermissions() {
        for (Permission permission : allPermissions) {
            permission.check();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (Permission permission : allPermissions) {
            permission.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
