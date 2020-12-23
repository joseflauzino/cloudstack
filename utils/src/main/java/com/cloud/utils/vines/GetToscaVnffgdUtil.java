package com.cloud.utils.vines;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;

public class GetToscaVnffgdUtil {

    public static ToscaVnffgd readVnffgdFile(String vnffgdUuid) {
        String vnffgdRepositoryPath = "/var/cloudstack-nfvo/vnffgd_repository/";
        String fullFileName = vnffgdRepositoryPath + vnffgdUuid + ".json";

        ToscaVnffgd vnffgd = null;
        Gson gson = new Gson();
        BufferedReader data;
        try {
            data = new BufferedReader(new FileReader(fullFileName));
            vnffgd = gson.fromJson(data, ToscaVnffgd.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return vnffgd;
    }
}