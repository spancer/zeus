package io.hermes.client;

/**
 * A client provides a one stop interface for performing actions/operations against the cluster.
 *
 * <p>
 * All operations performed are asynchronous by nature. Each action/operation has two flavors, the
 * first simply returns an {@link io.hermes.action.ActionFuture}, while the second accepts an
 * {@link io.hermes.action.ActionListener}.
 *
 * <p>
 * A client can either be retrieved from a {@link io.hermes.node.Node} started, or connected
 * remotely to one or more nodes using {@link io.hermes.client.transport.TransportClient}.
 *
 * @author spancer.ray
 * @see io.hermes.node.Node#client()
 * @see io.hermes.client.transport.TransportClient
 */
public interface Client {

  /**
   * Closes the client.
   */
  void close();


}
