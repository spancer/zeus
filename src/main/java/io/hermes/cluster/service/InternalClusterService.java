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

package io.hermes.cluster.service;

import static io.hermes.cluster.ClusterState.newClusterStateBuilder;
import static io.hermes.util.TimeValue.timeValueMillis;
import static io.hermes.util.concurrent.DynamicExecutors.daemonThreadFactory;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import com.google.inject.Inject;
import io.hermes.HermesException;
import io.hermes.cluster.ClusterChangedEvent;
import io.hermes.cluster.ClusterService;
import io.hermes.cluster.ClusterState;
import io.hermes.cluster.ClusterStateListener;
import io.hermes.cluster.ClusterStateUpdateTask;
import io.hermes.cluster.ProcessedClusterStateUpdateTask;
import io.hermes.cluster.TimeoutClusterStateListener;
import io.hermes.cluster.node.DiscoveryNode;
import io.hermes.cluster.node.DiscoveryNodes;
import io.hermes.discovery.DiscoveryService;
import io.hermes.threadpool.ThreadPool;
import io.hermes.transport.TransportService;
import io.hermes.util.TimeValue;
import io.hermes.util.component.AbstractLifecycleComponent;
import io.hermes.util.settings.Settings;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author spancer.ray
 */
public class InternalClusterService extends AbstractLifecycleComponent<ClusterService>
    implements ClusterService {

  private final TimeValue timeoutInterval;

  private final ThreadPool threadPool;

  private final DiscoveryService discoveryService;

  private final TransportService transportService;
  private final List<ClusterStateListener> clusterStateListeners =
      new CopyOnWriteArrayList<ClusterStateListener>();
  private final List<TimeoutHolder> clusterStateTimeoutListeners =
      new CopyOnWriteArrayList<TimeoutHolder>();
  private volatile ExecutorService updateTasksExecutor;
  private volatile ScheduledFuture scheduledFuture;

  private volatile ClusterState clusterState = newClusterStateBuilder().build();

  @Inject
  public InternalClusterService(Settings settings, DiscoveryService discoveryService,
      TransportService transportService, ThreadPool threadPool) {
    super(settings);
    this.transportService = transportService;
    this.discoveryService = discoveryService;
    this.threadPool = threadPool;

    this.timeoutInterval = componentSettings.getAsTime("timeoutInterval", timeValueMillis(500));
  }

  @Override
  protected void doStart() throws HermesException {
    this.updateTasksExecutor =
        newSingleThreadExecutor(daemonThreadFactory(settings, "clusterService#updateTask"));
    scheduledFuture = threadPool.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        long timestamp = System.currentTimeMillis();
        for (final TimeoutHolder holder : clusterStateTimeoutListeners) {
          if ((timestamp - holder.timestamp) > holder.timeout.millis()) {
            clusterStateTimeoutListeners.remove(holder);
            threadPool.execute(new Runnable() {
              @Override
              public void run() {
                holder.listener.onTimeout(holder.timeout);
              }
            });
          }
        }
      }
    }, timeoutInterval);
  }

  @Override
  protected void doStop() throws HermesException {
    scheduledFuture.cancel(false);
    for (TimeoutHolder holder : clusterStateTimeoutListeners) {
      holder.listener.onTimeout(holder.timeout);
    }
    updateTasksExecutor.shutdown();
    try {
      updateTasksExecutor.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // ignore
    }
  }

  @Override
  protected void doClose() throws HermesException {
  }

  public ClusterState state() {
    return this.clusterState;
  }

  public void add(ClusterStateListener listener) {
    clusterStateListeners.add(listener);
  }

  public void remove(ClusterStateListener listener) {
    clusterStateListeners.remove(listener);
  }

  public void add(TimeValue timeout, TimeoutClusterStateListener listener) {
    clusterStateTimeoutListeners
        .add(new TimeoutHolder(listener, System.currentTimeMillis(), timeout));
  }

  public void remove(TimeoutClusterStateListener listener) {
    clusterStateTimeoutListeners.remove(new TimeoutHolder(listener, -1, null));
  }

  public void submitStateUpdateTask(final String source, final ClusterStateUpdateTask updateTask) {
    if (!lifecycle.started()) {
      return;
    }
    updateTasksExecutor.execute(new Runnable() {
      @Override
      public void run() {
        if (!lifecycle.started()) {
          return;
        }
        ClusterState previousClusterState = clusterState;
        try {
          clusterState = updateTask.execute(previousClusterState);
        } catch (Exception e) {
          StringBuilder sb =
              new StringBuilder("Failed to execute cluster state update, state:\nVersion [")
                  .append(clusterState.version()).append("], source [").append(source)
                  .append("]\n");
          sb.append(clusterState.nodes().prettyPrint());
          logger.warn(sb.toString(), e);
          return;
        }
        if (previousClusterState != clusterState) {
          if (clusterState.nodes().localNodeMaster()) {
            // only the master controls the version numbers
            clusterState = new ClusterState(clusterState.version() + 1, clusterState.getMetaData(),
                clusterState.nodes());
          } else {
            // we got this cluster state from the master, filter out based on versions (don't call
            // listeners)
            if (clusterState.version() < previousClusterState.version()) {
              logger.info("Got old cluster state [" + clusterState.version() + "<"
                  + previousClusterState.version() + "] from source [" + source + "], ignoring");
              return;
            }
          }

          if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder("Cluster State updated:\nVersion [")
                .append(clusterState.version()).append("], source [").append(source).append("]\n");
            sb.append(clusterState.nodes().prettyPrint());
            logger.trace(sb.toString());
          } else if (logger.isDebugEnabled()) {
            logger.debug("Cluster state updated, version [{}], source [{}]", clusterState.version(),
                source);
          }

          ClusterChangedEvent clusterChangedEvent = new ClusterChangedEvent(source, clusterState,
              previousClusterState, discoveryService.firstMaster());
          // new cluster state, notify all listeners
          final DiscoveryNodes.Delta nodesDelta = clusterChangedEvent.nodesDelta();
          if (nodesDelta.hasChanges() && logger.isInfoEnabled()) {
            String summary = nodesDelta.shortSummary();
            if (summary.length() > 0) {
              logger.info(summary);
            }
          }

          // TODO, do this in parallel (and wait)
          for (DiscoveryNode node : nodesDelta.addedNodes()) {
            try {
              transportService.connectToNode(node);
            } catch (Exception e) {
              // TODO, need to mark this node as failed...
              logger.warn("Failed to connect to node [" + node + "]", e);
            }
          }

          for (TimeoutHolder timeoutHolder : clusterStateTimeoutListeners) {
            timeoutHolder.listener.clusterChanged(clusterChangedEvent);
          }
          for (ClusterStateListener listener : clusterStateListeners) {
            listener.clusterChanged(clusterChangedEvent);
          }

          threadPool.execute(new Runnable() {
            @Override
            public void run() {
              for (DiscoveryNode node : nodesDelta.removedNodes()) {
                transportService.disconnectFromNode(node);
              }
            }
          });

          // if we are the master, publish the new state to all nodes
          if (clusterState.nodes().localNodeMaster()) {
            discoveryService.publish(clusterState);
          }

          if (updateTask instanceof ProcessedClusterStateUpdateTask) {
            ((ProcessedClusterStateUpdateTask) updateTask).clusterStateProcessed(clusterState);
          }
        }
      }
    });
  }

  private static class TimeoutHolder {

    final TimeoutClusterStateListener listener;
    final long timestamp;
    final TimeValue timeout;

    private TimeoutHolder(TimeoutClusterStateListener listener, long timestamp, TimeValue timeout) {
      this.listener = listener;
      this.timestamp = timestamp;
      this.timeout = timeout;
    }

    @Override
    public int hashCode() {
      return listener.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return ((TimeoutHolder) obj).listener == listener;
    }
  }
}
