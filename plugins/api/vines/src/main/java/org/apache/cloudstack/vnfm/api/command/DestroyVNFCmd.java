// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.vnfm.api.command;

import javax.inject.Inject;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.vnfm.VNFManager;
import org.apache.cloudstack.vnfm.api.response.VnfResponse;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.vm.VirtualMachine;
import com.cloud.vnfm.VnfVO;

@APICommand(name = "destroyVnf", description = "Destroys a VNF instance.", responseObject = VnfResponse.class, responseView = ResponseView.Restricted, entityType = {
        VirtualMachine.class }, requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class DestroyVNFCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(DestroyVNFCmd.class.getName());

    private static final String s_name = "destroyvirtualmachineresponse";

    @Inject
    private VNFManager _vnfManager;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.VNF_ID, type = CommandType.STRING, required = true, description = "The VNF ID")
    private String id;

    @Parameter(name = ApiConstants.EXPUNGE, type = CommandType.BOOLEAN, description = "If true is passed, the VNF is expunged immediately. False by default.", since = "4.2.1")
    private Boolean expunge;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public boolean getExpunge() {
        if (expunge == null) {
            return false;
        }
        return expunge;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return CallContext.current().getCallingAccount().getId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_DESTROY;
    }

    @Override
    public String getEventDescription() {
        return "destroying VNF: " + this.id;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.VirtualMachine;
    }

    @Override
    public void execute() throws ResourceUnavailableException, ConcurrentOperationException {
        VnfVO result = null;
        result = _vnfManager.destroyVnf(this);
        if (result != null) {
            VnfResponse response = new VnfResponse(result.getUuid(), result.getName(), result.getVnfpId(),
                    result.getCreated());
            response.setObjectName("vnf");
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to destroy VNF");
        }
    }
}
