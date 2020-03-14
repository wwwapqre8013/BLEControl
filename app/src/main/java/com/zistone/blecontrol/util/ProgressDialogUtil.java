package com.zistone.blecontrol.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zistone.blecontrol.R;

public class ProgressDialogUtil
{
    private static AlertDialog _alertDialog;
    private static Listener _listener;

    public interface Listener
    {
        void OnDismiss();
    }

    public static void ShowWarning(Context context, String title, String content)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton("知道了", (dialog, which) ->
        {
        });
        builder.show();
    }

    public static void ShowProgressDialog(Context context, Listener listener, String str)
    {
        _alertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        _listener = listener;
        View loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
        _alertDialog.setView(loadView, 0, 0, 0, 0);
        _alertDialog.setCanceledOnTouchOutside(true);
        TextView textView = loadView.findViewById(R.id.txt_dialog);
        textView.setText(str);
        _alertDialog.show();
        _alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                if(_listener != null)
                    _listener.OnDismiss();
            }
        });
    }

    public static void ShowProgressDialog(Context context, String str)
    {
        _alertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        View loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
        TextView textView = loadView.findViewById(R.id.txt_dialog);
        textView.setText(str);
        _alertDialog.setCanceledOnTouchOutside(true);
        _alertDialog.setView(loadView, 0, 0, 0, 0);
        _alertDialog.show();
    }

    public static void Dismiss()
    {
        if(_alertDialog != null)
        {
            _alertDialog.dismiss();
            _alertDialog = null;
        }
    }

}
