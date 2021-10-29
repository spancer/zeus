/*******************************************************************************
 * Copyright 2021 spancer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package io.hermes.discovery.zen.ping;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.HermesIllegalStateException;
import io.hermes.cluster.ClusterName;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.discovery.zen.DiscoveryNodesProvider;
import io.hermes.discovery.zen.ping.multicast.MulticastZenPing;
import io.hermes.discovery.zen.ping.unicast.UnicastZenPing;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.TransportService;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.settings.Settings;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author spancer.ray
 */
public class ZenPingService extends AbstractLifecycleComponent<ZenPing> implements ZenPing {

  private volatile ImmutableList<? extends ZenPing> zenPings = ImmutableList.of();

  @Inject
  public ZenPingService(Settings settings, ThreadPool threadPool, TransportService transportService,
      ClusterName clusterName) {
    super(settings);

    ImmutableList.Builder<ZenPing> zenPingsBuilder = ImmutableList.builder();

    /**
     * 暂时不要使用MulticastZenPing
     */
    if (componentSettings.getAsBoolean("multicast.enabled", false)) {
      zenPingsBuilder
          .add(new MulticastZenPing(settings, threadPool, transportService, clusterName));
    }
    if (componentSettings.getAsArray("unicast.hosts", new String[]{"127.0.0.1:9400"}).length > 0) {
      zenPingsBuilder.add(new UnicastZenPing(settings, threadPool, transportService, clusterName));
    }

    this.zenPings = zenPingsBuilder.build();
  }

  public ImmutableList<? extends ZenPing> zenPings() {
    return this.zenPings;
  }

  public void zenPings(ImmutableList<? extends ZenPing> pings) {
    this.zenPings = pings;
    if (lifecycle.started()) {
      for (ZenPing zenPing : zenPings) {
        zenPing.start();
      }
    } else if (lifecycle.stopped()) {
      for (ZenPing zenPing : zenPings) {
        zenPing.stop();
      }
    }
  }

  @Override
  public void setNodesProvider(DiscoveryNodesProvider nodesProvider) {
    if (lifecycle.started()) {
      throw new HermesIllegalStateException("Can't set nodes provider when started");
    }
    for (ZenPing zenPing : zenPings) {
      zenPing.setNodesProvider(nodesProvider);
    }
  }

  @Override
  protected void doStart() throws HermesException {
    for (ZenPing zenPing : zenPings) {
      zenPing.start();
    }
  }

  @Override
  protected void doStop() throws HermesException {
    for (ZenPing zenPing : zenPings) {
      zenPing.stop();
    }
  }

  @Override
  protected void doClose() throws HermesException {
    for (ZenPing zenPing : zenPings) {
      zenPing.close();
    }
  }

  public PingResponse[] pingAndWait(TimeValue timeout) {
    final AtomicReference<PingResponse[]> response = new AtomicReference<PingResponse[]>();
    final CountDownLatch latch = new CountDownLatch(1);
    ping(new PingListener() {
      @Override
      public void onPing(PingResponse[] pings) {
        response.set(pings);
        latch.countDown();
      }
    }, timeout);
    try {
      latch.await();
      return response.get();
    } catch (InterruptedException e) {
      return null;
    }
  }

  @Override
  public void ping(PingListener listener, TimeValue timeout) throws HermesException {
    ImmutableList<? extends ZenPing> zenPings = this.zenPings;
    CompoundPingListener compoundPingListener = new CompoundPingListener(listener, zenPings);
    for (ZenPing zenPing : zenPings) {
      zenPing.ping(compoundPingListener, timeout);
    }
  }

  private static class CompoundPingListener implements PingListener {

    private final PingListener listener;

    private final ImmutableList<? extends ZenPing> zenPings;

    private final AtomicInteger counter;

    private ConcurrentMap<DiscoveryNode, PingResponse> responses =
        new ConcurrentHashMap<DiscoveryNode, PingResponse>();

    private CompoundPingListener(PingListener listener, ImmutableList<? extends ZenPing> zenPings) {
      this.listener = listener;
      this.zenPings = zenPings;
      this.counter = new AtomicInteger(zenPings.size());
    }

    @Override
    public void onPing(PingResponse[] pings) {
      if (pings != null) {
        for (PingResponse pingResponse : pings) {
          responses.put(pingResponse.target(), pingResponse);
        }
      }
      if (counter.decrementAndGet() == 0) {
        listener.onPing(responses.values().toArray(new PingResponse[responses.size()]));
      }
    }
  }
}
