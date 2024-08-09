package com.rhbgroup.dte.obc.common.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

  @Value("${HAZELCAST_MEMBERS}")
  private String hazelcastMembers;

  @Bean
  public Config customHazelcastConfig() {
    Config config = new Config();
    NetworkConfig networkConfig = config.getNetworkConfig();
    JoinConfig joinConfig = networkConfig.getJoin();

    // Disable multicast
    joinConfig.getMulticastConfig().setEnabled(false);

    // Enable TCP/IP
    joinConfig.getTcpIpConfig().setEnabled(true);

    // Split the IPs by comma and add to the member list
    Arrays.stream(hazelcastMembers.split(","))
        .forEach(ip -> joinConfig.getTcpIpConfig().addMember(ip.trim() + ":5701"));

    return config;
  }
}
