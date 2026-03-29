package com.raft.raftnode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class node {

    public enum Role { FOLLOWER, CANDIDATE, LEADER }

    private Role role = Role.FOLLOWER;
    private int currentTerm = 0;
    private String votedFor = null;
    private String leaderId = null;
    private List<String> peers;

    @Value("${raft.nodes}")
    private String allNodes;

    // finds peers
    @PostConstruct
    public void init() throws Exception {
        String myIp = InetAddress.getLocalHost().getHostAddress();
        peers = Arrays.stream(allNodes.split(",")).map(String::trim).filter(url -> !url.contains(myIp)).collect(Collectors.toList());
        System.out.println("My IP: " + myIp);
        System.out.println("Peers: " + peers);
    }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public int getCurrentTerm() { return currentTerm; }
    public void setCurrentTerm(int term) { this.currentTerm = term; }
    public String getVotedFor() { return votedFor; }
    public void setVotedFor(String votedFor) { this.votedFor = votedFor; }
    public String getLeaderId() { return leaderId; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }
    public List<String> getPeers() { return peers; }
    public boolean isLeader() { return role == Role.LEADER; }
}