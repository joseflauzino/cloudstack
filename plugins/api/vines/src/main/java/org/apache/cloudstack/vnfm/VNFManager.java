package org.apache.cloudstack.vnfm;

import java.util.List;

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
import org.apache.cloudstack.vnfm.vo.VnfpVO;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.ExecutionException;
import com.cloud.vnfm.VnfVO;

/**
 * Vines' VNF Manager
 *
 * @author Jose Flauzino
 */
public interface VNFManager {

    /**
     * Create a VNF VM.
     *
     * @param cmd the DeployVNFCmd class
     * @return UserVm object if successful.
     *
     * @throws InsufficientCapacityException if there is insufficient capacity to
     *                                       deploy the VM.
     * @throws ConcurrentOperationException  if there are multiple users working on
     *                                       the same VM or in the same environment.
     * @throws ResourceUnavailableException  if the resources required to deploy the
     *                                       VM is not currently available.
     */
    public UserVm createVnfVm(DeployVNFCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException,
            ConcurrentOperationException, StorageUnavailableException, ResourceAllocationException;

    public VnfVO createVnfRecord(String vnfpId, long vmId);

    /**
     * List one or all Virtual Network Functions
     *
     * @param vnfUuid the VNF UUID
     * @return the VnfResponse list
     */
    public List<VnfResponse> listVnf(ListVnfsCmd cmd);

    /**
     * Deploy a VNF. This includes 1) start the VM, 2) push the VNF Package to VM,
     * 3) Install the network function (application), and 4) start the network
     * function
     *
     * @param cmd the DeployVNFCmd class
     * @return VnfVO object if successful.
     */
    public VnfVO deployVnf(DeployVNFCmd cmd) throws ExecutionException, ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException, InterruptedException;

    /**
     * Destroy a VNF instance.
     *
     * @param cmd the DestroyVNFCmd class
     * @return VnfVO object if successful.
     */
    public VnfVO destroyVnf(DestroyVNFCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException;

    /**
     * Handle a VNF State notification
     *
     * @param cmd the NotifyVnfStateCmd class
     * @return VnfStateNotificationResponse object if successful.
     */
    public VnfStateNotificationResponse handleVnfStateNotification(NotifyVnfStateCmd cmd);

    /**
     * Recovery a VNF. This includes
     *
     * @param cmd the RecoveryVNFCmd class
     * @return VnfResponse object if successful.
     */
    public VnfResponse recoveryVnf(RecoveryVNFCmd cmd)
            throws InsufficientCapacityException, ResourceUnavailableException;

    /**
     * Scale a VNF. This includes
     *
     * @param cmd the ScaleVNFCmd class
     * @return VnfResponse object if successful.
     */
    public VnfResponse scaleVnf(ScaleVNFCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException,
            ManagementServerException, VirtualMachineMigrationException;

    /**
     * Downloads and records a VNF Package
     *
     * @param name the VNFP name
     * @param url  the URL of where the VNFP is hosted
     * @return the VNFP VO
     */
    public VnfpVO createVnfp(CreateVnfpCmd cmd);

    /**
     * List one or all VNF Packages
     *
     * @param vnfpUuid the VNFP UUID
     * @return the VnfpResponse list
     */
    public List<VnfpResponse> listVnfp(ListVnfpsCmd cmd);

    /**
     * Push the VNF Package to the VNF VM
     *
     * @param vnfUuid the VNF UUID to push the VNF Package
     * @return true if success, false otherwise
     */
    public EMSOperationResponse pushVnfp(String vnfUuid);

    /**
     * Install the Network Function within the VM
     *
     * @param installFunctionCmd the VNF UUID to send the install command
     * @return the Network Function status (Running or Stopped)
     */
    public EMSOperationResponse installFunction(InstallFunctionCmd cmd);

    /**
     * Start the Network Function within the VM
     *
     * @param vnfUuid the VNF UUID to start
     * @return the Network Function status (Running or Stopped)
     */
    public EMSOperationResponse startFunction(StartFunctionCmd cmd);

    /**
     * Stop the Network Function within the VM
     *
     * @param vnfUuid the VNF UUID to stop
     * @return the Network Function status (Running or Stopped)
     */
    public EMSOperationResponse stopFunction(StopFunctionCmd cmd);

    /**
     * Get Network Function status
     *
     * @param vnfUuid the VNF UUID
     * @return the Network Function status (Running or Stopped)
     */
    public EMSOperationResponse getFunctionStatus(GetFunctionStatusCmd cmd);

    public EMSOperationResponse sendOrchestrationCmd(String type, String url, String parameters, String httpMethod);
}