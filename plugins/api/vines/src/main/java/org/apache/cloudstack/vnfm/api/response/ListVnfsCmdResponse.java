package org.apache.cloudstack.vnfm.api.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.util.Vnf;

import com.cloud.serializer.Param;
import com.cloud.vnfm.VnfVO;
import com.google.gson.annotations.SerializedName;;

public class ListVnfsCmdResponse extends BaseResponse {

    @SerializedName("vnfs")
    @Param(description = "List of VNFs")
    private ArrayList<Vnf> vnfs;

    private Vnf toVnf(VnfVO vo) {
        Vnf vnf = new Vnf();
        vnf.setId(vo.getUuid());
        vnf.setName(vo.getName());
        vnf.setVnfpId(vo.getVnfpId());
        return vnf;
    }

    public void setResponse(VnfVO vo) {
        ArrayList<Vnf> tempVnfs = new ArrayList<>();
        tempVnfs.add(toVnf(vo));
        this.vnfs = tempVnfs;
    }

    public void setResponse(List<VnfVO> vnfs) {
        ArrayList<Vnf> tempVnfs = new ArrayList<>();
        for (VnfVO vo : vnfs) {
            tempVnfs.add(toVnf(vo));
        }
        this.vnfs = tempVnfs;
    }
}