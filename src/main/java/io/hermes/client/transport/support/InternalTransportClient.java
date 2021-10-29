/*******************************************************************************
 * Copyright 2021 spancer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
/*
 * Copyright (c) Chalco-Steering Technology Co., Ltd. All Rights Reserved. This software is licensed
 * not sold. Use or reproduction of this software by any unauthorized individual or entity is
 * strictly prohibited. This software is the confidential and proprietary information of
 * Chalco-Steering Technology Co., Ltd. Disclosure of such confidential information and shall use it
 * only in accordance with the terms of the license agreement you entered into with Chalco-Steering
 * Technology Co., Ltd. Chalco-Steering Technology Co., Ltd. MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * Chalco-Steering Technology Co., Ltd. SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ANY DERIVATIVES THEREOF.
 */


package io.hermes.client.transport.support;

import com.google.inject.Inject;
import io.hermes.client.internal.InternalClient;
import io.hermes.client.transport.TransportClientNodesService;
import io.hermes.threadpool.ThreadPool;
import io.hermes.util.component.AbstractComponent;
import io.hermes.util.settings.Settings;

/**
 * @author spancer.ray
 */
public class InternalTransportClient extends AbstractComponent implements InternalClient {

  private final ThreadPool threadPool;

  private final TransportClientNodesService nodesService;

  private final InternalTransportAdminClient adminClient;


  @Inject
  public InternalTransportClient(Settings settings, ThreadPool threadPool,
      TransportClientNodesService nodesService, InternalTransportAdminClient adminClient) {
    super(settings);
    this.threadPool = threadPool;
    this.nodesService = nodesService;
    this.adminClient = adminClient;
  }

  @Override
  public void close() {
    // nothing to do here
  }

  @Override
  public ThreadPool threadPool() {
    return this.threadPool;
  }

}
