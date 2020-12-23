package com.cloud.utils.vines;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;

public class GetToscaVnfdUtil {

    public static ToscaVnfd readVnfdFile(String vnfpUuid) {
        String vnfpRepositoryPath = "/var/cloudstack-vnfm/vnfp_repository/";
        String vnfdInternalPath = "/Definitions/VNFD.json";
        String fullFileName = vnfpRepositoryPath + vnfpUuid + vnfdInternalPath;

        ToscaVnfd vnfd = null;
        Gson gson = new Gson();
        BufferedReader data;
        try {
            data = new BufferedReader(new FileReader(fullFileName));
            vnfd = gson.fromJson(data, ToscaVnfd.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return vnfd;
    }
}