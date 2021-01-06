package org.apache.cloudstack.vnfm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.nfvo.NFVOrchestrator;
import org.apache.cloudstack.nfvo.dao.VnfPlatformDao;
import org.apache.cloudstack.nfvo.vo.VnfPlatformVO;
import org.apache.cloudstack.util.EMSResponse;
import org.apache.cloudstack.util.VinesUtil;
import org.apache.cloudstack.vnfm.api.command.CreateVnfpCmd;
import org.apache.cloudstack.vnfm.api.command.DeployVNFCmd;
import org.apache.cloudstack.vnfm.api.command.DestroyVNFCmd;
import org.apache.cloudstack.vnfm.api.command.GetFunctionStatusCmd;
import org.apache.cloudstack.vnfm.api.command.InstallFunctionCmd;
import org.apache.cloudstack.vnfm.api.command.ListVnfpsCmd;
import org.apache.cloudstack.vnfm.api.command.ListVnfsCmd;
import org.apache.cloudstack.vnfm.api.command.NotifyVnfStateCmd;
import org.apache.cloudstack.vnfm.api.command.RecoveryVNFCmd;
import org.apache.cloudstack.vnfm.api.command.ScaleVNFCmd;
import org.apache.cloudstack.vnfm.api.command.StartFunctionCmd;
import org.apache.cloudstack.vnfm.api.command.StopFunctionCmd;
import org.apache.cloudstack.vnfm.api.response.EMSOperationResponse;
import org.apache.cloudstack.vnfm.api.response.VnfResponse;
import org.apache.cloudstack.vnfm.api.response.VnfStateNotificationResponse;
import org.apache.cloudstack.vnfm.api.response.VnfpResponse;
import org.apache.cloudstack.vnfm.dao.VnfpDao;
import org.apache.cloudstack.vnfm.vo.VnfpVO;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import com.cloud.dc.DataCenter;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.network.Network;
import com.cloud.network.Network.IpAddresses;
import com.cloud.offering.ServiceOffering;
import com.cloud.storage.Storage;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.template.TemplateApiService;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountService;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.EntityManager;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.ExecutionException;
import com.cloud.utils.vines.GetToscaVnfdUtil;
import com.cloud.utils.vines.ToscaVnfd;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.UserVmService;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vnfm.VnfVO;
import com.cloud.vnfm.dao.VnfDao;
import com.google.gson.Gson;

/**
 * Vines VNF Manager implementation
 *
 * @author joseflauzino
 */
@Component
@DB
public class VNFManagerImpl implements VNFManager {
    @Inject
    private EntityManager _entityMgr;
    @Inject
    private VnfDao _vnfDao;
    @Inject
    private VnfPlatformDao _vnfPlatformDao;
    @Inject
    private VnfpDao _vnfpDao;
    @Inject
    private NFVOrchestrator _nfvo;

    @Inject
    private UserVmService _userVmService;
    @Inject
    private UserVmManager _userVmManager;
    @Inject
    private VMTemplateDao _templateDao;
    @Inject
    private TemplateApiService _tmplService;
    @Inject
    private AccountService _accountService;
    @Inject
    private UserVmDao _vmDao;

    // HTTP Methods
    private final static String POST = "POST";
    // private final static String GET = "GET";

    /////////////////////////////////////////////////////
    //////// Virtual Network Functions Commands /////////
    /////////////////////////////////////////////////////

    public UserVm createVnfVm(DeployVNFCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException,
            ConcurrentOperationException, StorageUnavailableException, ResourceAllocationException {

        ToscaVnfd vnfd = GetToscaVnfdUtil.readVnfdFile(cmd.getVnfpId());
        Account owner = _accountService.getActiveAccountById(cmd.getEntityOwnerId());
        Long zoneId = cmd.getZoneId();
        DataCenter zone = _entityMgr.findById(DataCenter.class, zoneId);
        if (zone == null) {
            throw new InvalidParameterValueException("Unable to find zone by id=" + zoneId);
        }

        String serviceOfferingId = vnfd.getServiceOfferingUuid();
        ServiceOffering serviceOffering = _entityMgr.findByUuid(ServiceOffering.class, serviceOfferingId);
        if (serviceOffering == null) {
            throw new InvalidParameterValueException("Unable to find service offering: " + serviceOfferingId);
        }

        VirtualMachineTemplate template = _entityMgr.findByUuid(VirtualMachineTemplate.class, vnfd.getTemplateUuid());
        if (template == null) {
            throw new InvalidParameterValueException("Unable to use template " + vnfd.getTemplateUuid());
        }

        // String name = vnfd.getVnfName();
        String displayName = vnfd.getVnfName();
        Boolean displayVm = true;

        // Getting management network ID
        long netMgmtId = _entityMgr.findByUuid(Network.class, vnfd.getManagementInterfaceUuid()).getId();

        // Getting all networks IDs from VNFD
        List<Long> networkIds = new ArrayList<Long>();
        for (String netUuid : vnfd.getNetworkUuids()) {
            networkIds.add(_entityMgr.findByUuid(Network.class, netUuid).getId());
        }

        // Getting traffic network (the first after the management network on VNFD)
        long trafficNetworkId = 0;
        for (long net : networkIds) {
            if (net != netMgmtId) {
                trafficNetworkId = net;
                break;
            }
        }

        // Detecting the default network
        // The first network list index will be the default network of the VNF VM
        List<Long> sortedNetworkIds = new ArrayList<Long>();
        VnfPlatformVO vnfPlatformVO = _vnfPlatformDao.findByUuid(vnfd.getVnfPlatformId());
        if (vnfPlatformVO == null) {
            throw new CloudRuntimeException("Unable to find VNF Platform with ID " + vnfd.getVnfPlatformId());
        }
        String defaultNic = vnfPlatformVO.getDefaultNic();
        if (defaultNic.equals("mgmtInterface")) { // the management interface should be the default NIC
            // Sorting network list in order to leave netMgmtId in first index
            networkIds.remove(netMgmtId);
            sortedNetworkIds.add(netMgmtId);
            sortedNetworkIds.addAll(networkIds);
        } else {
            // Sorting network list in order to leave trafficNetworkId in first index
            networkIds.remove(trafficNetworkId);
            sortedNetworkIds.add(trafficNetworkId);
            sortedNetworkIds.addAll(networkIds);
        }

        UserVm vm = null;
        Map<String, String> customparameterMap = new HashMap<String, String>(); // empty
        IpAddresses defaultIps = new IpAddresses(null, null);// empty
        vm = _userVmService.createAdvancedVirtualMachine(zone, serviceOffering, template, sortedNetworkIds, owner, null,
                displayName, null, null, null, null, cmd.getHttpMethod(), null, null, null, defaultIps, displayVm, null,
                null, customparameterMap, cmd.getCustomId(), null, null, null);

        List<VMTemplateVO> child_templates = _templateDao.listByParentTemplatetId(template.getId());
        for (VMTemplateVO tmpl : child_templates) {
            if (tmpl.getFormat() == Storage.ImageFormat.ISO) {
                // s_logger.info("MDOV trying to attach disk to the VM " + tmpl.getId() + "
                // vmid=" + vm.getId());
                _tmplService.attachIso(tmpl.getId(), vm.getId());
            }
        }
        return vm;
    }

    @Override
    public VnfVO createVnfRecord(String vnfpId, long vmId) {
        ToscaVnfd vnfd = GetToscaVnfdUtil.readVnfdFile(vnfpId);
        if (vnfd == null) {
            throw new InvalidParameterValueException("Unable to find the VNFD within VNFP: " + vnfpId);
        }
        VnfVO vnfVO = new VnfVO(vnfd.getVnfName(), vnfpId, vmId);
        _vnfDao.persist(vnfVO);
        return vnfVO;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_CREATE, eventDescription = "deploying VNF", async = true)
    public VnfVO deployVnf(DeployVNFCmd cmd) throws ExecutionException, ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException, InterruptedException {
        // Find VNF
        VnfVO vnfVO = _vnfDao.findById(cmd.getEntityId());
        if (vnfVO == null) {
            throw new CloudRuntimeException("Unable to find VNF with ID " + cmd.getEntityId());
        }

        long vmId = vnfVO.getVmId();
        Long hostId = cmd.getHostId();

        // Start the VM
        System.out.println("Starting VNF");
        UserVm vm = startVnfVm(vmId, hostId);

        finishVnfDeployment(vnfVO);

        return vnfVO;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_HANDLE_NOTIFICATION, eventDescription = "Handling notification", async = true)
    public VnfStateNotificationResponse handleVnfStateNotification(NotifyVnfStateCmd cmd) {
        // ler as informacoes da politica de monitoramento de falhas

        // decidir de tem que recuperar a VNF
        boolean vnfIsUp = true;
        if (cmd.getVnfState().equals("inactive")) {
            vnfIsUp = false;
        }

        // recuperar a VNF se for preciso
        try {
            Thread.sleep(9000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new VnfStateNotificationResponse(vnfIsUp);
    }

    @Override
    public VnfResponse recoveryVnf(RecoveryVNFCmd cmd)
            throws InsufficientCapacityException, ResourceUnavailableException {
        VnfVO vnfVO = _entityMgr.findByUuid(VnfVO.class, cmd.getId());
        if (vnfVO == null) {
            throw new CloudRuntimeException("Unable to find VNF with ID " + cmd.getId());
        }
        UserVm vm = _entityMgr.findById(UserVm.class, vnfVO.getVmId());
        if (vm == null) {
            throw new CloudRuntimeException("Unable to find VM with ID " + vnfVO.getVmId());
        }

        // Send stop command to VNF
        EMSOperationResponse response = sendRequest("stop", prepareLifecycleUrl("stop"),
                prepareLifecycleUrlParam(vnfVO.getUuid()), POST);

        // Restore VM
        UserVm restoredVm = null;
        restoredVm = _userVmService.restoreVM(CallContext.current().getCallingAccount(), vm.getId(),
                vm.getTemplateId());
        if (restoredVm == null) {
            throw new CloudRuntimeException("Unable to restore VNF " + vnfVO.getId());
        }

        // Updating VNF information
        vnfVO.setVmId(restoredVm.getId());
        _vnfDao.update(vnfVO.getId(), vnfVO);

        finishVnfDeployment(vnfVO);

        return new VnfResponse(vnfVO.getUuid(), vnfVO.getName(), vnfVO.getVnfpId(), vnfVO.getCreated());
    }

    private void waitVmInitialize(VnfVO vnfVO) {
        // Wait for the VM to be up (polling)
        System.out.println("Waiting VNF initialize");
        int n = 30; // number of attempts
        for (int i = 0; i < n; i++) {
            System.out.println("Getting VNF status " + i);
            EMSOperationResponse vnfStatusResponse = sendRequest("vnfstatus", prepareLifecycleUrl("vnfstatus"),
                    prepareLifecycleUrlParam(vnfVO.getUuid()), POST);
            if (vnfStatusResponse.isSuccess() && vnfStatusResponse.getData().equals("Running")) {
                break; // stop polling because VNF VM is up
            }
            if (i == n - 1) {
                throw new CloudRuntimeException(
                        "Number of attempts to get EMS status has been exceeded. VNF ID: " + vnfVO.getUuid());
            }
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public VnfResponse scaleVnf(ScaleVNFCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException,
            ManagementServerException, VirtualMachineMigrationException {
        VnfVO vnfVO = _entityMgr.findByUuid(VnfVO.class, cmd.getId());
        if (vnfVO == null) {
            throw new CloudRuntimeException("Unable to find VNF with UUID " + cmd.getId());
        }
        ServiceOffering newServiceOffering = _entityMgr.findByUuid(ServiceOffering.class, cmd.getServiceOfferingId());
        if (newServiceOffering == null) {
            throw new CloudRuntimeException("Unable to find service offering with UUID " + cmd.getServiceOfferingId());
        }
        // Stop VM
        UserVm vm = _userVmService.stopVirtualMachine(vnfVO.getVmId(), true);
        if (vm == null) {
            throw new CloudRuntimeException("Unable to stop VM with ID " + vnfVO.getVmId());
        }
        // Upgrade VM
        vm = null;
        vm = _userVmService.upgradeVirtualMachine(vnfVO.getVmId(), newServiceOffering.getId());
        if (vm == null) {
            throw new CloudRuntimeException("Unable to upgrade VM with ID " + vnfVO.getVmId());
        }

        // Start VM
        try {
            vm = startVnfVm(vm.getId(), vm.getHostId());
        } catch (InsufficientCapacityException | ExecutionException e) {
            throw new CloudRuntimeException("Unable to start VM with ID " + vnfVO.getVmId());
        }

        // Wait VNF VM initialize
        waitVmInitialize(vnfVO);

        // Start the network function
        System.out.println("Starting the network function");
        EMSOperationResponse startResponse = sendRequest("start", prepareLifecycleUrl("start"),
                prepareLifecycleUrlParam(vnfVO.getUuid()), POST);
        if (!startResponse.isSuccess()) {
            throw new CloudRuntimeException("Unable to start the VNF with ID " + vnfVO.getId());
        }

        return new VnfResponse(vnfVO.getUuid(), vnfVO.getName(), vnfVO.getVnfpId(), vnfVO.getCreated());
    }

    private void finishVnfDeployment(VnfVO vnfVO) {
        waitVmInitialize(vnfVO);

        // Push the VNF Package to the VM
        System.out.println("Pushing VNFP");
        EMSOperationResponse pushVnfpResponse = pushVnfp(vnfVO);
        if (!pushVnfpResponse.isSuccess()) {
            throw new CloudRuntimeException("Unable to push VNFP to VNF with ID " + vnfVO.getId());
        }

        // Install the network function (application)
        System.out.println("Installing the network function");
        EMSOperationResponse installResponse = sendRequest("install", prepareLifecycleUrl("install"),
                prepareLifecycleUrlParam(vnfVO.getUuid()), POST);
        if (!installResponse.isSuccess()) {
            throw new CloudRuntimeException("Unable to install the VNF with ID " + vnfVO.getId());
        }

        // Start the network function
        System.out.println("Starting the network function");
        EMSOperationResponse startResponse = sendRequest("start", prepareLifecycleUrl("start"),
                prepareLifecycleUrlParam(vnfVO.getUuid()), POST);
        if (!startResponse.isSuccess()) {
            throw new CloudRuntimeException("Unable to start the VNF with ID " + vnfVO.getId());
        }
    }

    private UserVm startVnfVm(long vmId, Long hostId) throws ExecutionException, ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException {
        System.out.println("----------- Dentro de start VM");

        UserVmVO vm = _vmDao.findById(vmId);
        Pair<UserVmVO, Map<VirtualMachineProfile.Param, Object>> vmParamPair = null;
        try {
            vmParamPair = _userVmManager.startVirtualMachine(vmId, hostId, null, null);
            System.out.println("----------- depois de executar e recuperar o vm pair");
            vm = vmParamPair.first();
            // At this point VM should be in "Running" state
            UserVmVO tmpVm = _vmDao.findById(vm.getId());
            if (!tmpVm.getState().equals(State.Running)) {
                // Some other thread changed state of VM, possibly vmsync
                // s_logger.error("VM " + tmpVm + " unexpectedly went to " + tmpVm.getState() +
                // " state");
                throw new ConcurrentOperationException("Failed to deploy VM " + vm);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CloudRuntimeException("Erro enquanto iniciava a VM");
        } finally {
            // TODO: check line below
            System.out.println("Caiu em finally");
            // updateVmStateForFailedVmCreation(vm.getId(), hostId);
        }
        // Check that the password was passed in and is valid
        VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vm.getTemplateId());
        if (template.isEnablePassword()) {
            // this value is not being sent to the backend; need only for api
            // display purposes
            vm.setPassword((String) vmParamPair.second().get(VirtualMachineProfile.Param.VmPassword));
        }
        System.out.println("Finalizando start Vm");
        return vm;
    }

    @Override
    public List<VnfResponse> listVnf(ListVnfsCmd cmd) {
        String vnfUuid = cmd.getVnfId();
        List<VnfVO> vnfs = new ArrayList<VnfVO>();
        List<VnfResponse> responses = new ArrayList<VnfResponse>();
        if (vnfUuid != null && !vnfUuid.equals("")) {
            VnfVO vo = _vnfDao.findByUuid(vnfUuid);
            if (vo == null) {
                throw new CloudRuntimeException("Unable to find VNF with ID: " + vnfUuid);
            }
            vnfs.add(vo);
        } else {
            vnfs = _vnfDao.listAll();
            if (vnfs == null) {
                throw new CloudRuntimeException("Unable to list VNFs");
            }
        }
        if (!vnfs.isEmpty()) {
            for (VnfVO vnf : vnfs) {
                VnfResponse response = new VnfResponse(vnf.getUuid(), vnf.getName(), vnf.getVnfpId(), vnf.getCreated());
                response.setObjectName("vnf");
                responses.add(response);
            }
        }
        return responses;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VNF_DESTROY, eventDescription = "Destroying VNF", async = true)
    public VnfVO destroyVnf(DestroyVNFCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException {
        VnfVO vnfVO = _vnfDao.findByUuid(cmd.getId());
        if (vnfVO == null) {
            throw new CloudRuntimeException("Unable to find VNF with ID:" + cmd.getId());
        }

        // Stop VM
        UserVm vm = _userVmService.stopVirtualMachine(vnfVO.getVmId(), true);
        if (vm == null) {
            throw new CloudRuntimeException("Unable to stop VM with ID " + vnfVO.getVmId());
        }

        vm = null;
        vm = _userVmService.destroyVm(vnfVO.getVmId(), cmd.getExpunge());
        if (vm == null) {
            throw new CloudRuntimeException("Unable to destroy VNF " + cmd.getId() + ". Error while destroy VM.");
        }

        _vnfDao.remove(vnfVO.getId());
        return vnfVO;
    }

    /////////////////////////////////////////////////////
    ////////////// VNF Packages Commands ////////////////
    /////////////////////////////////////////////////////

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VNFP_CREATE, eventDescription = "Creating the VNFP", async = true)
    public VnfpVO createVnfp(CreateVnfpCmd cmd) {
        String name = cmd.getName();
        String url = cmd.getUrl();
        VnfpVO vo = new VnfpVO(name, url);
        // Get and Save the VNF Package
        if (VinesUtil.downloadVnfp(vo.getUuid(), vo.getName(), vo.getUrl())) {
            try {
                if (!VinesUtil.extractVnfpZip(vo.getUuid())) {
                    throw new CloudRuntimeException("Unable to extract the VNF Package");
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new CloudRuntimeException("Unable to download the VNF Package");
            }
        }
        _vnfpDao.persist(vo);
        return vo;
    }

    @Override
    public List<VnfpResponse> listVnfp(ListVnfpsCmd cmd) {
        String vnfpUuid = cmd.getVnfpId();
        List<VnfpVO> vnfps = new ArrayList<VnfpVO>();
        List<VnfpResponse> responses = new ArrayList<VnfpResponse>();
        if (vnfpUuid != null && !vnfpUuid.equals("")) {
            VnfpVO vo = _vnfpDao.findByUuid(vnfpUuid);
            if (vo == null) {
                throw new CloudRuntimeException("Unable to find VNFP with ID: " + vnfpUuid);
            }
            vnfps.add(vo);
        } else {
            vnfps = _vnfpDao.listAll();
            if (vnfps == null) {
                throw new CloudRuntimeException("Unable to list VNF Packages");
            }
        }
        if (!vnfps.isEmpty()) {
            for (VnfpVO vnfp : vnfps) {
                VnfpResponse response = new VnfpResponse(vnfp.getUuid(), vnfp.getName(), vnfp.getUrlDecoded(),
                        vnfp.getCreated());
                response.setObjectName("vnfp");
                responses.add(response);
            }
        }
        return responses;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_PUSH_VNFP, eventDescription = "Pushing the VNFP to VNF VM", async = true)
    public EMSOperationResponse pushVnfp(String vnfUuid) {
        // Find VNF
        VnfVO vnfVO = _vnfDao.findByUuid(vnfUuid);
        if (vnfVO == null) {
            throw new CloudRuntimeException("Unable to find VNF with UUID " + vnfUuid);
        }
        return pushVnfp(vnfVO);
    }

    private EMSOperationResponse pushVnfp(VnfVO vnfVO) {
        boolean success = true;
        EMSOperationResponse emsOpResponse = null;
        // Define VNFP paths
        String vnfpUuid = vnfVO.getVnfpId();
        String source_folder = "/var/cloudstack-vnfm/vnfp_repository/" + vnfpUuid;
        System.out.println("Source_folder: " + source_folder);
        String output_zip_file = "/var/cloudstack-vnfm/vnfp_repository/" + vnfpUuid + ".zip";
        System.out.println("Output_zip_file: " + output_zip_file);

        // Zip VNFP
        // ZipFolder zip = new ZipFolder(output_zip_file, source_folder);
        VinesUtil.zipFolder(source_folder, output_zip_file);

        // Push VNFP
        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println("Enviando vnfp");
        try {
            HttpPost httppost = new HttpPost(prepareLifecycleUrl("pushvnfp"));

            FileBody bin = new FileBody(new File(output_zip_file));
            StringBody json = new StringBody(prepareLifecycleUrlParam(vnfVO.getUuid()), ContentType.APPLICATION_JSON);

            HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("json", json).addPart("file", bin).build();
            httppost.addHeader("Accept", "application/json;odata=verbose");
            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());
            CloseableHttpResponse httpResponse = null;
            try {
                httpResponse = httpclient.execute(httppost);
                System.out.println("----------------------------------------");
                HttpEntity resEntity = httpResponse.getEntity();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                System.out.println("Response: " + response);
                System.out.println("Response (string): " + response.toString());
                EMSResponse emsResponse = null;
                Gson gson = new Gson();
                emsResponse = gson.fromJson(response.toString(), EMSResponse.class);
                if (emsResponse.getStatus().equals("error")) {
                    success = false;
                }
                emsOpResponse = new EMSOperationResponse("pushvnfp", success, emsResponse.getData());
                EntityUtils.consume(resEntity);
                httpResponse.close();
                httpclient.close();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                throw new CloudRuntimeException("Unable to push VNFP to VNF with UUID " + vnfVO.getUuid());
            } catch (IOException e) {
                e.printStackTrace();
                throw new CloudRuntimeException("Unable to push VNFP to VNF with UUID " + vnfVO.getUuid());
            }
        } finally {
            // Delete zip file
            File zip_file = new File(output_zip_file);
            zip_file.delete();
        }
        return emsOpResponse;
    }

    /////////////////////////////////////////////////////
    /////////////////// Lifecycle ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_INSTALL_FUNCTION, eventDescription = "Installing the Network Function", async = true)
    public EMSOperationResponse installFunction(InstallFunctionCmd cmd) {
        return sendRequest("install", prepareLifecycleUrl("install"), prepareLifecycleUrlParam(cmd.getVnfId()), POST);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_START_FUNCTION, eventDescription = "Starting the Network Function", async = true)
    public EMSOperationResponse startFunction(StartFunctionCmd cmd) {
        return sendRequest("start", prepareLifecycleUrl("start"), prepareLifecycleUrlParam(cmd.getVnfId()), POST);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_STOP_FUNCTION, eventDescription = "Stopping the Network Function", async = true)
    public EMSOperationResponse stopFunction(StopFunctionCmd cmd) {
        return sendRequest("stop", prepareLifecycleUrl("stop"), prepareLifecycleUrlParam(cmd.getVnfId()), POST);
    }

    @Override
    public EMSOperationResponse getFunctionStatus(GetFunctionStatusCmd cmd) {
        return sendRequest("status", prepareLifecycleUrl("status"), prepareLifecycleUrlParam(cmd.getVnfId()), POST);
    }

    @Override
    public EMSOperationResponse sendOrchestrationCmd(String type, String url, String parameters, String httpMethod) {
        return sendRequest(type, url, parameters, httpMethod);
    }

    /////////////////////////////////////////////////////
    ///////////////// Private Utils /////////////////////
    /////////////////////////////////////////////////////

    private String prepareLifecycleUrl(String cmd) {
        /*
         * TODO: - get host IP based on VNF; - get port from DB (needs to create Vines'
         * configuration table)
         */
        return "http://192.168.122.11:9000/api/lifecycle/" + cmd;
    }

    private String prepareLifecycleUrlParam(String vnfUuid) {
        // Find VNF
        VnfVO vnfVO = _vnfDao.findByUuid(vnfUuid);
        if (vnfVO == null) {
            throw new CloudRuntimeException("Unable to find VNF with UUID " + vnfUuid);
        }

        // Find VNF VM
        VMInstanceVO vnfVm = _nfvo.findVnfVmInstance(vnfUuid);
        if (vnfVm == null) {
            throw new CloudRuntimeException("Unable to the VM of the VNF with UUID " + vnfUuid);
        }

        // Read VNFD of the VNF
        ToscaVnfd vnfd = GetToscaVnfdUtil.readVnfdFile(vnfVO.getVnfpId());

        // Find VNF management IP
        String mgmtNetGatewayIp = _nfvo.findGuestNetworkGatewayIp(vnfd.getManagementInterfaceUuid());
        String vnfMgmtIp = _nfvo.getVnfVmIp(vnfVm.getId(), mgmtNetGatewayIp); // VNF management IP

        // Find (guest network) gateway IP
        String routerIp = _nfvo.findRouterIP(mgmtNetGatewayIp);

        // Detect VNF Platform Driver
        VnfPlatformVO vnfPlatformVO = _vnfPlatformDao.findByUuid(vnfd.getVnfPlatformId());
        if (vnfPlatformVO == null) {
            throw new CloudRuntimeException("Unable to find VNF Platform Driver with UUID " + vnfd.getVnfPlatformId());
        }
        String vnfPlatform = vnfPlatformVO.getDriverName();
        return "{\"vnf_ip\":\"" + vnfMgmtIp + "\",\"router_ip\":\"" + routerIp + "\", \"vnf_platform\":\"" + vnfPlatform
                + "\"}";
    }

    private EMSOperationResponse sendRequest(String type, String urlStr, String urlParameters, String httpMethod) {
        StringBuffer httpResponse = new StringBuffer();
        try {
            URL urlObj = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            con.setRequestMethod(httpMethod);
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            System.out.println("Sending " + httpMethod + " request to URL: " + urlStr);
            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                httpResponse.append(inputLine);
            in.close();

            System.out.println("RESPONSE: " + httpResponse.toString());
        } catch (MalformedURLException e) {
            // erro ao criar urlObj
            e.printStackTrace();
        } catch (IOException e) {
            // erro ao criar con
            e.printStackTrace();
        }

        EMSResponse emsResponse = null;
        Gson gson = new Gson();
        emsResponse = gson.fromJson(httpResponse.toString(), EMSResponse.class);
        boolean success = false;
        if (emsResponse.getStatus().equals("success")) {
            success = true;
        }
        EMSOperationResponse response = new EMSOperationResponse(type, success, emsResponse.getData());
        return response;
    }
}