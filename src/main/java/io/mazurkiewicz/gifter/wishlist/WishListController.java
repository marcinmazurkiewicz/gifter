package io.mazurkiewicz.gifter.wishlist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class WishListController {

    private final WishListService wishListService;

    @PostMapping("/wishlist")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<WishItemResponse> addWishItem(@AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal,
                                                 @RequestBody @Valid WishItemRequest request) {
        UUID itemPublicId = wishListService.addItem(principal.getName(), request.name(), request.link());
        return ResponseEntity.ok(new WishItemResponse(itemPublicId));
    }

    @DeleteMapping("/wishlist/{itemPublicId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<WishItemResponse> deleteWishItem(@AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal,
                                                 @PathVariable UUID itemPublicId) {
        wishListService.deleteItem(principal.getName(), itemPublicId);
        return ResponseEntity.noContent().build();
    }


    record WishItemRequest(@NotBlank String name, String link) {
    }

    record WishItemResponse(UUID itemPublicId) {}
}
