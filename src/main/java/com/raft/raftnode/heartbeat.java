package com.raft.raftnode;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;

@Service
@EnableScheduling
public class heartbeat {

    private final node raftNode;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Random random = new Random();

    private volatile long lastHeartbeatTime = System.currentTimeMillis();
    private static final int HEARTBEAT_INTERVAL = 150;
    private static final int ELECTION_TIMEOUT_MIN = 300;
    private static final int ELECTION_TIMEOUT_MAX = 600;

    public heartbeat(node raftNode) {
        this.raftNode = raftNode;
    }

    @PostConstruct
    public void start() {
        keepLeader();
    }
,
    @Scheduled(fixedDelay = HEARTBEAT_INTERVAL)
    public void sendHeartbeats() {
        if (!raftNode.isLeader()) return;

        for (String peer : raftNode.getPeers()) {
            try {
                restTemplate.postForObject(
                        peer + "/raft/heartbeat?term=" + raftNode.getCurrentTerm(),
                        null,
                        String.class
                );
            } catch (Exception e) {
                System.out.println("Failed to reach peer: " + peer);
            }
        }
    }

    public void receiveHeartbeat(int term) {
        if (term < raftNode.getCurrentTerm()) return;

        lastHeartbeatTime = System.currentTimeMillis();
        raftNode.setCurrentTerm(term);
        raftNode.setRole(node.Role.FOLLOWER);
    }

    private void keepLeader() {
        int random_timer = ELECTION_TIMEOUT_MIN + random.nextInt(ELECTION_TIMEOUT_MAX - ELECTION_TIMEOUT_MIN);

        scheduler.schedule(() -> {
            if (raftNode.isLeader()) return;

            if (System.currentTimeMillis() - lastHeartbeatTime > random_timer) {
                startElection();
            } else {
                keepLeader();
            }
        }, random_timer, TimeUnit.MILLISECONDS);
    }

    private void startElection() {
        System.out.println("Starting election, term: " + (raftNode.getCurrentTerm() + 1));
        raftNode.setRole(node.Role.CANDIDATE);
        raftNode.setCurrentTerm(raftNode.getCurrentTerm() + 1);
        raftNode.setVotedFor("self");

        int votes = 1;
        int majority = (raftNode.getPeers().size() + 1) / 2 + 1;

        for (String peer : raftNode.getPeers()) {
            try {
                String result = restTemplate.postForObject(
                        peer + "/raft/vote?term=" + raftNode.getCurrentTerm(),
                        null,
                        String.class
                );
                if ("YES".equals(result)) votes++;
            } catch (Exception e) {
                System.out.println("Vote request failed to: " + peer);
            }
        }

        if (votes >= majority) {
            System.out.println("Won election! Becoming leader for term: " + raftNode.getCurrentTerm());
            raftNode.setRole(node.Role.LEADER);
            raftNode.setLeaderId("self");
        } else {
            System.out.println("Lost election, staying follower");
            raftNode.setRole(node.Role.FOLLOWER);
            keepLeader();
        }
    }
}