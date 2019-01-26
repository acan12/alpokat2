package com.alpokat.kasir.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.alpokat.kasir.R;

import app.beelabs.com.codebase.base.BaseDialog;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionReportTodayDialog extends BaseDialog {
    public TransactionReportTodayDialog(@NonNull Context context, int style) {
        super(context, style);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setWindowContentDialogLayout(R.layout.dialog_transaction_report_today);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.doneBtn)
    public void onDone(View view){
        dismiss();
    }



}
