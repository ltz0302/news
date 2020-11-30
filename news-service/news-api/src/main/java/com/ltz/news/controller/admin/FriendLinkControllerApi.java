package com.ltz.news.controller.admin;

import com.ltz.news.pojo.bo.SaveFriendLinkBO;
import com.ltz.news.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Api(value = "首页友情链接维护", tags = {"首页友情链接维护controller"})
@RequestMapping("friendLinkMng")
public interface FriendLinkControllerApi {

    @ApiOperation(value = "新增或者修改友情链接", notes = "新增或者修改友情链接", httpMethod = "POST")
    @PostMapping("/saveOrUpdateFriendLink")
    GraceJSONResult saveOrUpdateFriendLink(
            @RequestBody @Valid SaveFriendLinkBO saveFriendLinkBO);
//    ,BindingResult result);

    @ApiOperation(value = "查询友情链接列表", notes = "查询友情链接列表", httpMethod = "POST")
    @PostMapping("/getFriendLinkList")
    GraceJSONResult getFriendLinkList();

    @ApiOperation(value = "删除友情链接", notes = "删除友情链接", httpMethod = "POST")
    @PostMapping("/delete")
    GraceJSONResult delete(@RequestParam String linkId);
//
//
//    @ApiOperation(value = "门户端查询友情链接列表", notes = "门户端查询友情链接列表", httpMethod = "GET")
//    @GetMapping("portal/list")
//    GraceJSONResult queryPortalAllFriendLinkList();
}
