package org.apache.cloudstack.util;

import org.apache.cloudstack.nfvo.vo.VnffgdVO;

import com.cloud.utils.vines.ToscaVnffgd;

public class VnffgdResponse {
    String id;
    String url;
    ToscaVnffgd vnffgd;

    public VnffgdResponse() {
    }

    public VnffgdResponse(VnffgdVO vo, ToscaVnffgd vnffgd) {
        this.id = vo.getUuid();
        this.url = vo.getUrlDecoded();
        this.vnffgd = vnffgd;
    }
}
