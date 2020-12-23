package org.apache.cloudstack.vnfm.api.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.vnfm.vo.VnfpVO;

import com.cloud.serializer.Param;
import com.cloud.utils.vines.GetToscaVnfdUtil;
import com.cloud.utils.vines.ToscaVnfd;
import com.google.gson.annotations.SerializedName;;

public class ListVnfdsCmdResponse extends BaseResponse {

    @SerializedName("vnfds")
    @Param(description = "List of VNFDs contents")
    private ArrayList<ToscaVnfd> vnfds;

    public void setVnfdData(String vnfpId) {
        ArrayList<ToscaVnfd> tempVnfds = new ArrayList<>();
        System.out.println("VNFP ID: " + vnfpId);
        tempVnfds.add(GetToscaVnfdUtil.readVnfdFile(vnfpId));
        this.vnfds = tempVnfds;
    }

    public void setVnfdData(List<VnfpVO> vnfpList) {
        ArrayList<ToscaVnfd> tempVnfds = new ArrayList<>();
        for (VnfpVO vo : vnfpList) {
            tempVnfds.add(GetToscaVnfdUtil.readVnfdFile(vo.getUuid()));
        }
        this.vnfds = tempVnfds;
    }
}