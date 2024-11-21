package io.mazurkiewicz.gifter.group;

import io.mazurkiewicz.gifter.draw.DrawEditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final DrawEditionService drawEditionService;

    @GetMapping("/groups")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<GroupsResponse> getGroups() {
        List<GroupResponse> groups = groupService.getGroups()
                .stream()
                .map(group -> new GroupResponse(group.groupPublicId(), group.name()))
                .toList();
        return ResponseEntity.ok(new GroupsResponse(groups));
    }

    @PostMapping("/groups/{groupPublicId}/join")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<GroupsResponse> joinGroup(@PathVariable("groupPublicId") String groupPublicId,
                                             @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal) {
        groupService.joinGroup(principal.getName(), groupPublicId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/groups/{groupPublicId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<GroupDetailsResponse> getGroupDetails(@PathVariable("groupPublicId") String groupPublicId,
                                                         @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal) {
        GroupService.GroupDetails groupDetails = groupService.getGroupDetails(groupPublicId);
        List<DrawEditionService.DrawEdition> editionsForGroup = drawEditionService.findEditionsForGroup(groupPublicId, principal.getName());
        return ResponseEntity.ok(new GroupDetailsResponse(groupDetails.groupPublicId(), groupDetails.groupName(), groupDetails.members(), editionsForGroup));
    }

    @GetMapping("/groups/requests")
    @PreAuthorize("hasRole('ROLE_MOD')")
    ResponseEntity<GroupRequestResponse> getRequestForGroups() {
        List<GroupService.GroupRequestPreview> responses = groupService.getGroupRequests();
        return ResponseEntity.ok(new GroupRequestResponse(responses));
    }


    @PostMapping("/groups/requests")
    @PreAuthorize("hasRole('ROLE_MOD')")
    ResponseEntity<GroupsResponse> acceptRequestToGroup(@RequestBody AcceptRequest request) {
        groupService.acceptRequest(request.requestId());
        return ResponseEntity.accepted().build();
    }

    record GroupsResponse(List<GroupResponse> groups) {
    }

    record AcceptRequest(Long requestId) {
    }

    record GroupResponse(String groupPublicId, String name) {
    }

    public record GroupDetailsResponse(String groupPublicId, String groupName, List<GroupService.GroupMember> members,
                                       List<DrawEditionService.DrawEdition> drawEditions) {

    }

    record GroupRequestResponse(List<GroupService.GroupRequestPreview> requests) {}
}
