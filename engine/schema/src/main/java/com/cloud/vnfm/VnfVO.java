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
package com.cloud.vnfm;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.cloud.utils.db.GenericDao;

@Entity
@Table(name = "vnf")
public class VnfVO implements Vnf {

    /***
     * Attributes
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "uuid")
    private String uuid = UUID.randomUUID().toString();

    @Column(name = "vnfp_id")
    private String vnfpid;

    @Column(name = "name")
    private String name;

    @Column(name = "vm_id")
    long vmid;

    @Column(name=GenericDao.CREATED_COLUMN)
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date created;

    @Column(name=GenericDao.REMOVED_COLUMN)
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date removed;


    /***
     * Getters and Setters
     */
    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVnfpId() {
        return vnfpid;
    }

    public void setVnfpId(String vnfpid) {
        this.vnfpid = vnfpid;
    }

    public long getVmId() {
        return vmid;
    }

    public void setVmId(long vmid) {
        this.vmid = vmid;
    }


    /***
     * Constructors
     */
    public VnfVO(String name, String vnfpid, long vmid) {
        this.name = name;
        this.vnfpid = vnfpid;
        this.vmid = vmid;
    }

    public VnfVO() {
    }

    @Override
    public Date getCreated() {
        return created;
    }

}
