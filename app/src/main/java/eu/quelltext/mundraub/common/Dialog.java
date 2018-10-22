package eu.quelltext.mundraub.common;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.MundraubBaseActivity;
import eu.quelltext.mundraub.plant.Plant;

public class Dialog {

    private final MundraubBaseActivity activity;
    private boolean closed = false;

    public Dialog(Context context) {
        /* Pattern: Destroyed activity check
         * before you access anything from the activity, use a guard clause to ensure it is
         * usable.
         *     if (!canCreateDialog()) return;
         */
        this.activity = (MundraubBaseActivity) context;
    }

    public void alertInfo(int messageResourceString) {
        alert(R.string.attention, android.R.drawable.ic_dialog_info, messageResourceString, ClosedCallback.NULL);
    }
    public void alertError(int messageResourceString) {
        alertError(messageResourceString, ClosedCallback.NULL);
    }

    public void alertError(String messageResourceString) {
        alert(R.string.error, android.R.drawable.ic_dialog_alert, messageResourceString, ClosedCallback.NULL);
    }

    public void alertError(int messageResourceString, ClosedCallback cb) {
        alert(R.string.error, android.R.drawable.ic_dialog_alert, messageResourceString, cb);
    }

    public void alertSuccess(int messageResourceString) {
        alert(R.string.success, android.R.drawable.ic_dialog_info, messageResourceString, ClosedCallback.NULL);
    }

    private void alert(int title, int icon, int message, final ClosedCallback cb) {
        if (!canCreateDialog()) return;
        alert(title, icon, getString(message), cb);
    }

    private boolean canCreateDialog() {
        return activity != null && activity.canCreateDialog();
    }

    private void alert(int title, int icon, String message, final ClosedCallback cb) {
        if (!canCreateDialog()) return;
        // from https://stackoverflow.com/a/2115770/1320237
        AlertDialog.Builder builder = Helper.getAlertBuilder(activity);
        android.app.Dialog dialog = builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onClose();
                        cb.onClosed();
                    }
                })
                .setIcon(icon)
                .show();
        handleOnCancel(dialog);
        activity.onDialogOpened(this);
    }

    private void handleOnCancel(android.app.Dialog dialog) {
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onClose();
            }
        });
    }

    private void onClose() {
        if (closed) {
            return;
        }
        closed = true;
        activity.onDialogClosed(this);
    }

    public void askAndDeleteThePlantAndFinishTheActivity(final Plant plant) {
        askYesNo(R.string.delete_plant_reason, R.string.delete_question, new YesNoCallback() {
            @Override
            public void yes() {
                plant.delete();
                activity.finish();
            }

            @Override
            public void no() {
            }
        });
    }

    public interface ClosedCallback {
        ClosedCallback NULL = new ClosedCallback() {
            @Override
            public void onClosed() {

            }
        };
        void onClosed();
    }

    public interface YesNoCallback {
        void yes();
        void no();
    }

    public void askYesNo(int reason, int question, final YesNoCallback callback) {
        if (!canCreateDialog()) return;
        String reasonString = getString(reason);
        askYesNo(reasonString, question, callback);
    }

    private String getString(int resourceId) {
        return activity.getResources().getString(resourceId);
    }

    public void askYesNo(String reason, int question, final YesNoCallback callback) {
        if (!canCreateDialog()) return;
        // from https://stackoverflow.com/a/2478662
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        callback.yes();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        callback.no();
                        break;
                }
                onClose();
            }
        };
        AlertDialog.Builder builder = Helper.getAlertBuilder(activity);;
        android.app.Dialog dialog = builder.setMessage(reason + "\n" + getString(question))
                .setNegativeButton(R.string.no, dialogClickListener)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .show();
        handleOnCancel(dialog);
        activity.onDialogOpened(this);
    }
}
