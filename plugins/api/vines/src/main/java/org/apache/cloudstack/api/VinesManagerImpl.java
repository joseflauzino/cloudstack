package org.apache.cloudstack.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.cloudstack.nfvo.api.command.CreateSfcCmd;
import org.apache.cloudstack.nfvo.api.command.CreateVnfPlatformCmd;
import org.apache.cloudstack.nfvo.api.command.CreateVnffgdCmd;
import org.apache.cloudstack.nfvo.api.command.ListSfcsCmd;
import org.apache.cloudstack.nfvo.api.command.ListVnfPlatformsCmd;
import org.apache.cloudstack.nfvo.api.command.ListVnffgdsCmd;
import org.apache.cloudstack.vnfm.api.command.CreateVnfpCmd;
import org.apache.cloudstack.vnfm.api.command.DeployVNFCmd;
import org.apache.cloudstack.vnfm.api.command.DestroyVNFCmd;
import org.apache.cloudstack.vnfm.api.command.GetFunctionStatusCmd;
import org.apache.cloudstack.vnfm.api.command.InstallFunctionCmd;
import org.apache.cloudstack.vnfm.api.command.ListVnfdsCmd;
import org.apache.cloudstack.vnfm.api.command.ListVnfpsCmd;
import org.apache.cloudstack.vnfm.api.command.ListVnfsCmd;
import org.apache.cloudstack.vnfm.api.command.NotifyVnfStateCmd;
import org.apache.cloudstack.vnfm.api.command.PushVnfpCmd;
import org.apache.cloudstack.vnfm.api.command.RecoveryVNFCmd;
import org.apache.cloudstack.vnfm.api.command.RegisterEmsCmd;
import org.apache.cloudstack.vnfm.api.command.ScaleVNFCmd;
import org.apache.cloudstack.vnfm.api.command.StartFunctionCmd;
import org.apache.cloudstack.vnfm.api.command.StopFunctionCmd;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
//@Local(value = { VnfmManager.class })
public class VinesManagerImpl implements VinesManager {
    private static final Logger s_logger = Logger.getLogger(VinesManagerImpl.class);

    public VinesManagerImpl() {
        super();
    }

    @Override
    public List<Class<?>> getCommands() {
        List<Class<?>> cmdList = new ArrayList<Class<?>>();
        // VNFM
        cmdList.add(DeployVNFCmd.class);
        cmdList.add(DestroyVNFCmd.class);
        cmdList.add(ListVnfdsCmd.class);
        cmdList.add(ListVnfpsCmd.class);
        cmdList.add(ListVnfsCmd.class);
        cmdList.add(GetFunctionStatusCmd.class);
        cmdList.add(InstallFunctionCmd.class);
        cmdList.add(StartFunctionCmd.class);
        cmdList.add(StopFunctionCmd.class);
        cmdList.add(CreateVnfpCmd.class);
        cmdList.add(RegisterEmsCmd.class);
        cmdList.add(PushVnfpCmd.class);
        cmdList.add(NotifyVnfStateCmd.class);
        cmdList.add(RecoveryVNFCmd.class);
        cmdList.add(ScaleVNFCmd.class);
        // NFVO
        cmdList.add(CreateVnffgdCmd.class);
        cmdList.add(ListVnffgdsCmd.class);
        cmdList.add(CreateSfcCmd.class);
        cmdList.add(ListSfcsCmd.class);
        cmdList.add(CreateVnfPlatformCmd.class);
        cmdList.add(ListVnfPlatformsCmd.class);

        return cmdList;
    }
}