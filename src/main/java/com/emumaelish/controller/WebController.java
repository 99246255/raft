package com.emumaelish.controller;

import com.emumaelish.dto.GetRequest;
import com.emumaelish.dto.PutRequest;
import com.emumaelish.entity.KVEntity;
import com.emumaelish.entity.Node;
import com.emumaelish.service.*;
import com.emumaelish.entity.RestResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author: enumaelish
 * @Date: 2018/9/12 18:39
 * @Description:
 */
@RestController
@Api(value = "WebController", description = "raft对外接口")
public class WebController {

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
    private RestResult<String> getLeaderResult(){
        Node raftNode = raftNodeService.getDefaultNode();
        RestResult<String> message = new RestResult<String>(300, "不是leadder");
        message.setResults(raftNode.getLeaderId() == null ? null : raftNode.getLeaderId().toString());
        return message;
    }

    @RequestMapping(value = "/put", method = RequestMethod.POST)
    @ApiOperation(value = "设置", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResult<String> put(@Valid @RequestBody PutRequest putRequest) {
        Node raftNode = raftNodeService.getDefaultNode();
        if (!raftNode.getPeerId().equals(raftNode.getLeaderId())) {
            return getLeaderResult();
        }
        KVEntity kvEntity = new KVEntity();
        kvEntity.setKey(putRequest.getKey());
        kvEntity.setValue(putRequest.getValue());
        String s = raftEngine.replicate(putRequest.getGroupId(), kvEntity);
        if(s == null){
            return new RestResult<String>(RestResult.ERROR, "插入失败");
        }else{
            RestResult<String> result = getSuccessResult();
            result.setResults(s);
            return result;
        }
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ApiOperation(value = "查询", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResult<String> queryBlock(GetRequest getRequest){
        Node raftNode = raftNodeService.getDefaultNode();
        if (!raftNode.getPeerId().equals(raftNode.getLeaderId())) {
            return getLeaderResult();
        }
        RestResult<String> result = getSuccessResult();
        String value = stateMachine.getKey(getRequest.getGroupId(), getRequest.getKey());
        result.setResults(value);
        return result;
    }

}
