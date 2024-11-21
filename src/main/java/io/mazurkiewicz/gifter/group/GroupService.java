package io.mazurkiewicz.gifter.group;

import io.mazurkiewicz.gifter.user.UserNotFoundException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.mazurkiewicz.gifter.user.UserService.UserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final RealmResource keycloakRealmResource;
    private final JoinGroupRequestRepository joinGroupRequestRepository;

    public List<Group> getGroups() {
        return keycloakRealmResource.groups()
                .groups()
                .stream()
                .map(group -> new Group(group.getId(), group.getName(), false))
                .toList();
    }

    public void joinGroup(String userPublicId, String groupPublicId) {
        joinGroupRequestRepository.save(new JoinGroupRequestEntity(groupPublicId, userPublicId));
    }

    public List<Group> findUserGroups(String userPublicId) {
        List<Group> groups;
        try {
            groups = keycloakRealmResource.users()
                    .get(userPublicId)
                    .groups()
                    .stream()
                    .map(group -> new Group(group.getId(), group.getName(), false))
                    .toList();
        } catch (Exception e) {
            throw new UserNotFoundException(userPublicId);
        }

        List<String> pendingGroupsId = joinGroupRequestRepository.findAllByUserPublicIdAndStatus(userPublicId, RequestStatus.PENDING)
                .stream()
                .map(JoinGroupRequestEntity::getGroupPublicId)
                .toList();

        List<Group> pendingGroups = keycloakRealmResource.groups()
                .groups()
                .stream().filter(group -> pendingGroupsId.contains(group.getId()))
                .map(group -> new Group(group.getId(), group.getName(), true))
                .toList();

        return Stream.concat(groups.stream(), pendingGroups.stream()).toList();
    }

    public GroupDetails getGroupDetails(String groupPublicId) {
        try {
            GroupRepresentation representation = keycloakRealmResource.groups()
                    .group(groupPublicId)
                    .toRepresentation();

            List<GroupMember> members = getGroupMembers(groupPublicId)
                    .stream()
                    .map(member -> new GroupMember(member.name(), member.lastname(), member.userPublicId()))
                    .toList();

            return new GroupDetails(representation.getId(), representation.getName(), members);
        } catch (Exception e) {
            throw new GroupNotFoundException(groupPublicId);
        }
    }

    public List<UserDto> getGroupMembers(@NotBlank String groupPublicId) {
        try {
            return keycloakRealmResource.groups()
                    .group(groupPublicId)
                    .members()
                    .stream()
                    .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
                            user.getAttributes() == null ? List.of() : user.getAttributes().getOrDefault("excluded", List.of())))
                    .toList();

        } catch (Exception e) {
            throw new GroupNotFoundException(groupPublicId);
        }
    }

    public List<GroupRequestPreview> getGroupRequests() {
        List<GroupRequestPreview> result = new ArrayList<>();
        List<JoinGroupRequestEntity> all = joinGroupRequestRepository.findAllByStatus(RequestStatus.PENDING);

        for (JoinGroupRequestEntity request : all) {
            try {
                String groupName = keycloakRealmResource.groups()
                        .group(request.getGroupPublicId())
                        .toRepresentation()
                        .getName();

                UserRepresentation representation = keycloakRealmResource.users()
                        .get(request.getUserPublicId())
                        .toRepresentation();

                String username = representation.getFirstName() + " " + representation.getLastName();


                result.add(new GroupRequestPreview(request.getId(), request.getUserPublicId(), request.getGroupPublicId(), groupName, username, representation.getEmail()));

            } catch (Exception e) {
                log.error("Error getting group requests for data: group: {} user: {}. {} ", request.getGroupPublicId(), request.getUserPublicId(), e.getMessage());
            }

        }
        return result;
    }

    @Transactional
    public void acceptRequest(Long requestId) {
        JoinGroupRequestEntity entity = joinGroupRequestRepository.findById(requestId)
                .orElseThrow(() -> new GroupNotFoundException(requestId.toString()));

        try {
            keycloakRealmResource.users()
                    .get(entity.getUserPublicId())
                    .joinGroup(entity.getGroupPublicId());
        } catch (Exception e) {
            log.error("Cannot add user {} to group {}: {}", entity.getUserPublicId(), entity.getGroupPublicId(), e.getMessage());
        }
        entity.setStatus(RequestStatus.ACCEPTED);
    }

    public record GroupRequestPreview(Long requestId, String userPublicId, String groupPublicId, String groupName,
                                      String userName, String email) {
    }

    public record Group(String groupPublicId, String name, boolean isPending) {

    }

    public record GroupMember(String name, String lastname, String userPublicId) {
    }

    public record GroupDetails(String groupPublicId, String groupName, List<GroupMember> members) {
    }
}
