package com.raft.raftnode;

import com.raft.raftnode.node;
import org.springframework.web.bind.annotation.*;

@RestController
public class http {

    private final hashmap db;
    private final node raftNode;
    private final heartbeat heartbeatService;

    public http(hashmap db, node raftNode, heartbeat heartbeatService) {
        this.db = db;
        this.raftNode = raftNode;
        this.heartbeatService = heartbeatService;
    }

    @PostMapping("/set")
    public String set(@RequestParam String key, @RequestParam String value) {
        db.set(key, value);
        return "OK";
    }

    @GetMapping("/get")
    public String get(@RequestParam String key) {
        return db.get(key);
    }

    @DeleteMapping("/del")
    public String del(@RequestParam String key) {
        return db.del(key);
    }

    @PostMapping("/raft/heartbeat")
    public String heartbeat(@RequestParam int term) {
        heartbeatService.receiveHeartbeat(term);
        return "OK";
    }

    @PostMapping("/raft/vote")
    public String vote(@RequestParam int term) {
        if (term > raftNode.getCurrentTerm() && raftNode.getVotedFor() == null) {
            raftNode.setCurrentTerm(term);
            raftNode.setVotedFor("candidate");
            return "YES";
        }
        return "NO";
    }
}

/*
/raft/heartbeat

 */