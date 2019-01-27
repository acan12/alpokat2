package com.alpokat.kasir.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.alpokat.kasir.Model.api.TransaksiModel;
import com.alpokat.kasir.R;
import com.alpokat.kasir.support.ReportUtil;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import app.beelabs.com.codebase.base.BaseDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionReportTodayDialog extends BaseDialog {

    @BindView(R.id.totalTransaksi)
    TextView totalTransaksi;
    @BindView(R.id.jumlahTransaksi)
    TextView jumlahTransaksi;


    private List<TransaksiModel> dataTransaksi;

    public TransactionReportTodayDialog(List<TransaksiModel> dataTransaksi, @NonNull Context context, int style) {
        super(context, style);
        this.dataTransaksi = dataTransaksi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setWindowContentDialogLayout(R.layout.dialog_transaction_report_today);
        ButterKnife.bind(this);

        HashMap report = ReportUtil.getPenjualanLaporanToday(dataTransaksi);

        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String total = formatRupiah.format(Double.parseDouble(String.valueOf(report.get("total"))));

        totalTransaksi.setText(total);
        jumlahTransaksi.setText(String.valueOf(report.get("jumlah")));
    }

    @OnClick(R.id.main_content_dialog)
    public void onDone(View view){
        dismiss();
    }

}
