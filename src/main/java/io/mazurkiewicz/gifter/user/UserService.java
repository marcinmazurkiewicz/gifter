package io.mazurkiewicz.gifter.user;

import io.mazurkiewicz.gifter.wishlist.WishListService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final RealmResource keycloak;
    private final WishListService wishListService;

    public UserService(RealmResource keycloak, WishListService wishListService) {
        this.keycloak = keycloak;
        this.wishListService = wishListService;
    }

//    public List<UserDto> getUsers() {
//        return keycloak.users()
//                .list()
//                .stream()
//                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
//                        user.getAttributes() == null ? List.of() : user.getAttributes().getOrDefault("excluded", List.of())))
//                .toList();
//    }
//
//
//    public void getUser(String name) {
//        List<UserRepresentation> userRepresentations = keycloak.users()
//                .searchByUsername(name, true);
//    }

    public UserDto getUserByPublicId(String userPublicId) {
        try {
            UserRepresentation user = keycloak.users()
                    .get(userPublicId)
                    .toRepresentation();

            return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
                    user.getAttributes() == null ? List.of() : user.getAttributes().getOrDefault("excluded", List.of()));

        } catch (Exception e) {
            throw new UserNotFoundException(userPublicId);
        }
    }

    public UserWithWishListDto getUserWithWishList(String userPublicId) {
        try {
            UserRepresentation user = keycloak.users()
                    .get(userPublicId)
                    .toRepresentation();

            List<WishListService.WishItem> itemsForUser = wishListService.findItemsForUser(userPublicId);

            return new UserWithWishListDto(user.getId(), user.getFirstName(), user.getLastName(), itemsForUser);
        } catch (Exception e) {
            throw new UserNotFoundException(userPublicId);
        }
    }

    public record UserDto(String userPublicId, String username, String email, String name, String lastname,
                          List<String> excluded) {

    }

    public record UserWithWishListDto(String userPublicId, String firstName, String lastName,
                                      List<WishListService.WishItem> wishItems) {
    }
}
