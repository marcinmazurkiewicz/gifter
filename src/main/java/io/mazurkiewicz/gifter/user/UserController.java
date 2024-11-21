package io.mazurkiewicz.gifter.user;

import io.mazurkiewicz.gifter.draw.DrawEditionService;
import io.mazurkiewicz.gifter.group.GroupService;
import io.mazurkiewicz.gifter.wishlist.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WishListService wishListService;
    private final GroupService groupService;
    private final DrawEditionService drawEditionService;


    @GetMapping("/users/{userPublicId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<UserService.UserWithWishListDto> getUser(@PathVariable String userPublicId) {
        UserService.UserWithWishListDto userWithWishList = userService.getUserWithWishList(userPublicId);

        return ResponseEntity.ok(userWithWishList);
    }


    @GetMapping("/users/info")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<UserInfoResponse> getUsers(@AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal) {
        System.out.println("Principal " + principal.getName());
        String userPublicId = principal.getName();
        String userFirstName = principal.getAttribute("given_name");
        String userLastName = principal.getAttribute("family_name");

        List<UserGroupResponse> groups = groupService.findUserGroups(userPublicId)
                .stream()
                .map(group -> new UserGroupResponse(group.groupPublicId(), group.name(), group.isPending()))
                .toList();

        List<UserDrawResponse> activeDraws = drawEditionService.findEditionsForUser(userPublicId)
                .stream().map(d -> new UserDrawResponse(d.editionPublicId(), d.name(), d.groupName(), d.assignmentForUser(), d.assignmentMemberId()))
                .toList();

        List<UserWishItemResponse> wishList = wishListService.findItemsForUser(userPublicId)
                .stream()
                .map(item -> new UserWishItemResponse(item.itemPublicId(), item.itemName(), item.link()))
                .toList();

        return ResponseEntity.ok(new UserInfoResponse(userPublicId, userFirstName, userLastName, groups, activeDraws, wishList));
    }

    record UserInfoResponse(String userPublicId, String userFirstName, String userLastName,
                            List<UserGroupResponse> groups,
                            List<UserDrawResponse> activeDraws, List<UserWishItemResponse> wishList) {
    }

    record UserGroupResponse(String groupPublicId, String name, Boolean isPending) {
    }

    record UserDrawResponse(UUID drawPublicId, String drawName, String groupName, String assignedMember,
                            String assignedMemberId) {
    }

    record UserWishItemResponse(UUID wishItemPublicId, String name, String link) {
    }
}

