package com.emumaelish.controller;

import com.emumaelish.common.JsonUtil;
import com.emumaelish.entity.LogEntry;
import com.emumaelish.entity.Node;
import com.emumaelish.entity.RestResult;
import com.emumaelish.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @Author: enumaelish
 * @Date: 2019/3/18 13:52
 * @Description:
 */
@RestController
@Api(value = "NodeInfoController", description = "raft对内接口，查询节点信息")
public class NodeInfoController {

    @Autowired
    RaftNodeService raftNodeService;

    @Autowired
    RaftEngine raftEngine;

    @Autowired
    StateMachine stateMachine;

    @Autowired
    StoreService storeService;


    private <T> RestResult<T> getSuccessResult(){
        return new RestResult<T>(RestResult.SUCCESS, "成功");
    }

    @RequestMapping(value = "/isLeader", method = RequestMethod.GET)
    @ApiOperation(value = "是否是Leader", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean isLeader(@RequestParam(value="groupId",defaultValue=StoreService.DEFAULT_GROUP,required=false)String groupId){
        Node node = raftNodeService.getNodeByGroupId(groupId);
        if(node == null){
            return false;
        }
        return node.getPeerId().equals(node.getLeaderId());
    }

    @RequestMapping(value = "/getLastLogIndex", method = RequestMethod.GET)
    @ApiOperation(value = "获取最新一条日志", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResult<LogEntry> getLastLogIndex(){
        Node node = raftNodeService.getDefaultNode();
        LogEntry logEntry = storeService.getLastLogEntry(node.getGroupId());
        RestResult<LogEntry> message = getSuccessResult();
        message.setResults(logEntry);
        return message;
    }

    @RequestMapping(value = "/getNode", method = RequestMethod.GET)
    @ApiOperation(value = "查询当前节点信息", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getNode(){
        Collection<Node> allNodes = raftNodeService.getAllNodes();
        return  JsonUtil.toJSONString(allNodes);
    }


}
