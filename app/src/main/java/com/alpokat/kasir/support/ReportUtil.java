package com.alpokat.kasir.support;

import com.alpokat.kasir.Model.api.TransaksiModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ReportUtil {

    public static HashMap<String, String> getPenjualanLaporanToday(List<TransaksiModel> datas){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String today = dateFormat.format(date);

        HashMap hmap = new HashMap();
        long total = 0;
        int jumlah = 0;

        for(TransaksiModel model : datas){
            if(!model.getTanggal().equals(today)) continue;;

            total += model.getTotal();
            jumlah++;
        }

        hmap.put("total", total);
        hmap.put("jumlah", jumlah);

        return hmap;
    }
}
