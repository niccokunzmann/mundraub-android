package eu.quelltext.mundraub.common;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import eu.quelltext.mundraub.R;

public class Dialog {

    private final Context context;

    public Dialog(Context context) {
        this.context = context;
    }

    public void alertError(int errorResourceString) {
        alert(R.string.error, android.R.drawable.ic_dialog_alert, errorResourceString);
    }

    public void alertSuccess(int successResourceString) {
        alert(R.string.success, android.R.drawable.ic_dialog_info, successResourceString);
    }

    public void alert(int title, int icon, int message) {
        // from https://stackoverflow.com/a/2115770/1320237
        alert(title, icon, message, new DialogClosedCallback() {
            @Override
            public void onClosed() {
            }
        });
    }

    public void alert(int title, int icon, int message, final DialogClosedCallback cb) {
        // from https://stackoverflow.com/a/2115770/1320237
        AlertDialog.Builder builder = Helper.getAlertBuilder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cb.onClosed();
                    }
                })
                .setIcon(icon)
                .show();
    }

    public interface DialogClosedCallback {
        void onClosed();
    }

    public interface YesNoCallback {
        void yes();
        void no();
    }

    public void askYesNo(int repositionReason, int ask_open_the_map, final YesNoCallback callback) {
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
            }
        };
        AlertDialog.Builder builder = Helper.getAlertBuilder(context);;
        builder .setMessage(context.getResources().getString(repositionReason) + "\n" + context.getResources().getString(ask_open_the_map))
                .setNegativeButton(R.string.no, dialogClickListener)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .show();
    }
}
