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
package io.hermes.action;

import com.google.inject.AbstractModule;
import io.hermes.action.admin.cluster.health.TransportClusterHealthAction;
import io.hermes.action.admin.cluster.node.info.TransportNodesInfo;
import io.hermes.action.admin.cluster.node.shutdown.TransportNodesShutdown;
import io.hermes.action.admin.cluster.ping.broadcast.TransportBroadcastPingAction;
import io.hermes.action.admin.cluster.ping.single.TransportSinglePingAction;
import io.hermes.action.admin.cluster.state.TransportClusterStateAction;

/**
 * @author spancer.ray
 */
public class TransportActionModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(TransportNodesInfo.class).asEagerSingleton();
    bind(TransportNodesShutdown.class).asEagerSingleton();
    bind(TransportClusterStateAction.class).asEagerSingleton();
    bind(TransportClusterHealthAction.class).asEagerSingleton();

    bind(TransportSinglePingAction.class).asEagerSingleton();
    bind(TransportBroadcastPingAction.class).asEagerSingleton();

  }
}
