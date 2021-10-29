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

package io.hermes.util.io;

import io.hermes.util.OsUtils;
import io.hermes.util.logging.HermesLogger;
import io.hermes.util.logging.Loggers;
import io.hermes.util.settings.Settings;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author spancer.ray
 */
public abstract class NetworkUtils {

  public static final String IPv4_SETTING = "java.net.preferIPv4Stack";
  public static final String IPv6_SETTING = "java.net.preferIPv6Addresses";
  public static final String NON_LOOPBACK_ADDRESS = "non_loopback_address";
  public static final String LOCAL = "#local#";
  public static final String GLOBAL_NETWORK_BINDHOST_SETTING = "network.bind_host";
  public static final String GLOBAL_NETWORK_PUBLISHHOST_SETTING = "network.publish_host";
  private final static HermesLogger logger = Loggers.getLogger(NetworkUtils.class);
  private final static InetAddress localAddress;

  static {
    InetAddress localAddressX = null;
    try {
      localAddressX = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      logger.warn("Failed to find local host", e);
    }
    localAddress = localAddressX;
  }

  private NetworkUtils() {

  }

  public static Boolean defaultReuseAddress() {
    return OsUtils.WINDOWS ? null : true;
  }

  public static boolean isIPv4() {
    return System.getProperty("java.net.preferIPv4Stack") != null
        && System.getProperty("java.net.preferIPv4Stack").equals("true");
  }

  public static InetAddress resolveBindHostAddress(String bindHost, Settings settings)
      throws IOException {
    return resolveBindHostAddress(bindHost, settings, null);
  }

  public static InetAddress resolveBindHostAddress(String bindHost, Settings settings,
      String defaultValue2) throws IOException {
    return resolveInetAddress(bindHost, settings.get(GLOBAL_NETWORK_BINDHOST_SETTING),
        defaultValue2);
  }

  public static InetAddress resolvePublishHostAddress(String publishHost, Settings settings)
      throws IOException {
    InetAddress address = resolvePublishHostAddress(publishHost, settings, null);
    // verify that its not a local address
    if (address == null || address.isAnyLocalAddress()) {
      address = localAddress;
    }
    return address;
  }

  public static InetAddress resolvePublishHostAddress(String publishHost, Settings settings,
      String defaultValue2) throws IOException {
    return resolveInetAddress(publishHost, settings.get(GLOBAL_NETWORK_PUBLISHHOST_SETTING),
        defaultValue2);
  }

  public static InetAddress resolveInetAddress(String host, String defaultValue1,
      String defaultValue2) throws IOException {
    if (host == null) {
      host = defaultValue1;
    }
    if (host == null) {
      host = defaultValue2;
    }
    if (host == null) {
      return null;
    }
    if (host.startsWith("#") && host.endsWith("#")) {
      host = host.substring(1, host.length() - 1);
      if (host.equals("local")) {
        return localAddress;
      } else {
        Collection<NetworkInterface> allInterfs = getAllAvailableInterfaces();
        for (NetworkInterface ni : allInterfs) {
          if (!ni.isUp() || ni.isLoopback()) {
            continue;
          }
          if (host.equals(ni.getName()) || host.equals(ni.getDisplayName())) {
            return getFirstNonLoopbackAddress(ni, getIpStackType());
          }
        }
      }
      throw new IOException("Failed to find network interface for [" + host + "]");
    }
    return InetAddress.getByName(host);
  }

  public static InetAddress getIPv4Localhost() throws UnknownHostException {
    return getLocalhost(StackType.IPv4);
  }

  public static InetAddress getIPv6Localhost() throws UnknownHostException {
    return getLocalhost(StackType.IPv6);
  }

  public static InetAddress getLocalhost(StackType ip_version) throws UnknownHostException {
    if (ip_version == StackType.IPv4) {
      return InetAddress.getByName("127.0.0.1");
    } else {
      return InetAddress.getByName("::1");
    }
  }


  /**
   * Returns the first non-loopback address on any interface on the current host.
   *
   * @param ip_version Constraint on IP version of address to be returned, 4 or 6
   */
  public static InetAddress getFirstNonLoopbackAddress(StackType ip_version)
      throws SocketException {
    InetAddress address = null;

    Enumeration intfs = NetworkInterface.getNetworkInterfaces();
    while (intfs.hasMoreElements()) {
      NetworkInterface intf = (NetworkInterface) intfs.nextElement();
      if (!intf.isUp() || intf.isLoopback()) {
        continue;
      }
      address = getFirstNonLoopbackAddress(intf, ip_version);
      if (address != null) {
        return address;
      }
    }
    return null;
  }


  /**
   * Returns the first non-loopback address on the given interface on the current host.
   *
   * @param intf      the interface to be checked
   * @param ipVersion Constraint on IP version of address to be returned, 4 or 6
   */
  public static InetAddress getFirstNonLoopbackAddress(NetworkInterface intf, StackType ipVersion)
      throws SocketException {
    if (intf == null) {
      throw new IllegalArgumentException("Network interface pointer is null");
    }

    for (Enumeration addresses = intf.getInetAddresses(); addresses.hasMoreElements(); ) {
      InetAddress address = (InetAddress) addresses.nextElement();
      if (!address.isLoopbackAddress()) {
        if ((address instanceof Inet4Address && ipVersion == StackType.IPv4)
            || (address instanceof Inet6Address && ipVersion == StackType.IPv6)) {
          return address;
        }
      }
    }
    return null;
  }

  /**
   * A function to check if an interface supports an IP version (i.e has addresses defined for that
   * IP version).
   *
   * @param intf
   * @return
   */
  public static boolean interfaceHasIPAddresses(NetworkInterface intf, StackType ipVersion)
      throws SocketException, UnknownHostException {
    boolean supportsVersion = false;
    if (intf != null) {
      // get all the InetAddresses defined on the interface
      Enumeration addresses = intf.getInetAddresses();
      while (addresses != null && addresses.hasMoreElements()) {
        // get the next InetAddress for the current interface
        InetAddress address = (InetAddress) addresses.nextElement();

        // check if we find an address of correct version
        if ((address instanceof Inet4Address && (ipVersion == StackType.IPv4))
            || (address instanceof Inet6Address && (ipVersion == StackType.IPv6))) {
          supportsVersion = true;
          break;
        }
      }
    } else {
      throw new UnknownHostException("network interface " + intf + " not found");
    }
    return supportsVersion;
  }

  /**
   * Tries to determine the type of IP stack from the available interfaces and their addresses and
   * from the system properties (java.net.preferIPv4Stack and java.net.preferIPv6Addresses)
   *
   * @return StackType.IPv4 for an IPv4 only stack, StackYTypeIPv6 for an IPv6 only stack, and
   * StackType.Unknown if the type cannot be detected
   */
  public static StackType getIpStackType() {
    boolean isIPv4StackAvailable = isStackAvailable(true);
    boolean isIPv6StackAvailable = isStackAvailable(false);

    // if only IPv4 stack available
    if (isIPv4StackAvailable && !isIPv6StackAvailable) {
      return StackType.IPv4;
    }
    // if only IPv6 stack available
    else if (isIPv6StackAvailable && !isIPv4StackAvailable) {
      return StackType.IPv6;
    }
    // if dual stack
    else if (isIPv4StackAvailable && isIPv6StackAvailable) {
      // get the System property which records user preference for a stack on a dual stack machine
      if (Boolean.getBoolean(IPv4_SETTING)) // has preference over java.net.preferIPv6Addresses
      {
        return StackType.IPv4;
      }
      if (Boolean.getBoolean(IPv6_SETTING)) {
        return StackType.IPv6;
      }
      return StackType.IPv6;
    }
    return StackType.Unknown;
  }


  public static boolean isStackAvailable(boolean ipv4) {
    Collection<InetAddress> allAddrs = getAllAvailableAddresses();
    for (InetAddress addr : allAddrs) {
      if (ipv4 && addr instanceof Inet4Address || (!ipv4 && addr instanceof Inet6Address)) {
        return true;
      }
    }
    return false;
  }


  public static List<NetworkInterface> getAllAvailableInterfaces() throws SocketException {
    List<NetworkInterface> allInterfaces = new ArrayList<NetworkInterface>(10);
    NetworkInterface intf;
    for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
      intf = (NetworkInterface) en.nextElement();
      allInterfaces.add(intf);
    }
    return allInterfaces;
  }

  public static Collection<InetAddress> getAllAvailableAddresses() {
    Set<InetAddress> retval = new HashSet<InetAddress>();
    Enumeration en;

    try {
      en = NetworkInterface.getNetworkInterfaces();
      if (en == null) {
        return retval;
      }
      while (en.hasMoreElements()) {
        NetworkInterface intf = (NetworkInterface) en.nextElement();
        Enumeration<InetAddress> addrs = intf.getInetAddresses();
        while (addrs.hasMoreElements()) {
          retval.add(addrs.nextElement());
        }
      }
    } catch (SocketException e) {
      logger.warn("Failed to derive all available interfaces", e);
    }

    return retval;
  }


  public enum StackType {
    IPv4, IPv6, Unknown
  }
}
