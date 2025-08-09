package com.unithon.tadadak.groups.api;

import com.unithon.tadadak.groups.domain.Groups;
import com.unithon.tadadak.groups.dto.GroupsRequest;
import com.unithon.tadadak.groups.dto.GroupsResponse;
import com.unithon.tadadak.groups.service.GroupsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupsController {

    private final GroupsService groupsService;

    @PostMapping
    public GroupsResponse createGroup(@RequestBody GroupsRequest request) {
        return groupsService.createGroup(request);
    }

    @GetMapping
    public List<GroupsResponse> getAllGroups() {
        return groupsService.getAllGroups();
    }

    @GetMapping("/{groupId}")
    public GroupsResponse getGroup(@PathVariable Long groupId) {
        return groupsService.getGroupById(groupId);
    }
}