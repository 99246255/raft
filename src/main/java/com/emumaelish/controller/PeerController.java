package com.emumaelish.controller;

import com.emumaelish.entity.PeerId;
import com.emumaelish.entity.RestResult;
import com.emumaelish.service.RaftEngine;
import com.emumaelish.service.StoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: enumaelish
 * @Date: 2019/5/30 17:15
 * @Description:
 */
@RestController
@Api(value = "PeerController", description = "节点变化接口")
public class PeerController {

    @Autowired
    RaftEngine raftEngine;

    @RequestMapping(value = "/peer/add", method = RequestMethod.GET)
    @ApiOperation(value = "加点节点", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResult<String> add(@RequestParam(value="groupId",defaultValue= StoreService.DEFAULT_GROUP,required=false)String groupId, String peer, boolean add){
        PeerId peerId = PeerId.parsePeer(peer);
        long l = 0;
        try {
            l = raftEngine.replicateConfig(groupId, peerId, add);
            return new RestResult<String>(RestResult.SUCCESS, "成功", String.valueOf(l));
        } catch (Exception e) {
            e.printStackTrace();
            return new RestResult<String>(RestResult.ERROR, e.getMessage());
        }
    }
}
