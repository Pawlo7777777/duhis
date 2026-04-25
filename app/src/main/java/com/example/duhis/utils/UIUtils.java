package com.example.duhis.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.duhis.R;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UIUtils {

    public static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    public static void showSnackbar(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbarError(View view, String msg) {
        Snackbar snack = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
        snack.getView().setBackgroundResource(R.color.duhis_red);
        snack.show();
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showConfirmDialog(Context ctx, String title, String message,
                                         String posBtn, Runnable onConfirm) {
        new AlertDialog.Builder(ctx, R.style.Theme_DUHIS_AlertDialog)  // Changed theme
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(posBtn, (d, w) -> onConfirm.run())
                .setNegativeButton(ctx.getString(R.string.cancel), null)
                .show();
    }

    // NEW: Show option dialog with multiple choices
    public static void showOptionDialog(Context ctx, String title, String[] options,
                                        DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(ctx, R.style.Theme_DUHIS_AlertDialog)  // Changed theme
                .setTitle(title)
                .setItems(options, listener)
                .setNegativeButton(ctx.getString(R.string.cancel), null)
                .show();
    }

    // NEW: Show info dialog with single button
    public static void showInfoDialog(Context ctx, String title, String message,
                                      String buttonText, Runnable onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.Theme_DUHIS_AlertDialog)  // Changed theme
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonText, (d, w) -> {
                    if (onConfirm != null) onConfirm.run();
                });

        if (onConfirm == null) {
            builder.setPositiveButton(buttonText, null);
        }

        builder.show();
    }
    // NEW: Show yes/no confirmation dialog
    public static void showYesNoDialog(Context ctx, String title, String message,
                                       Runnable onYes, Runnable onNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.Theme_DUHIS_AlertDialog)  // Changed theme
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (d, w) -> {
                    if (onYes != null) onYes.run();
                })
                .setNegativeButton("No", (d, w) -> {
                    if (onNo != null) onNo.run();
                });
        builder.show();
    }

    // NEW: Show loading dialog (optional)
    public static AlertDialog showLoadingDialog(Context ctx, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.Theme_DUHIS_Dialog);
        View view = View.inflate(ctx, R.layout.dialog_loading, null);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        builder.setView(view);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
    }

    public static String formatDateTime(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(date);
    }

    public static String getRelativeTime(long timestampMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timestampMillis;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours / 24;

        if (seconds < 60)  return "Just now";
        if (minutes < 60)  return minutes + "m ago";
        if (hours < 24)    return hours + "h ago";
        if (days < 7)      return days + "d ago";
        return formatDate(new Date(timestampMillis));
    }
}