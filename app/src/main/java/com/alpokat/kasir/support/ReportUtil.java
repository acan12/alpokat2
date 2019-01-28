package com.alpokat.kasir.support;

import com.alpokat.kasir.Model.StatsModel;

import java.util.HashMap;
import java.util.List;

public class ReportUtil {

    public static HashMap<String, String> getPenjualanLaporanToday(List<StatsModel> datas) {

        HashMap hmap = new HashMap();
        long total = 0;

        for (StatsModel model : datas) {
            total += model.getTotal();
        }

        hmap.put("total", total);
        hmap.put("jumlah", datas.size());

        return hmap;
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


}
