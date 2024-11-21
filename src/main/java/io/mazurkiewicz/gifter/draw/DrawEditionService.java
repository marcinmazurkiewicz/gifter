package io.mazurkiewicz.gifter.draw;

import io.mazurkiewicz.gifter.group.GroupService;
import io.mazurkiewicz.gifter.user.AssignmentsRepository;
import io.mazurkiewicz.gifter.user.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawEditionService {

    private final AssignmentsRepository assignmentsRepository;
    private final DrawEditionRepository drawEditionRepository;
    private final GroupService groupService;
    private final UserService userService;

    public List<DrawEdition> findEditionsForGroup(String userPublicId, String groupPublicId) {
        return drawEditionRepository.findAllByGroupPublicId(groupPublicId)
                .stream()
                .map(entity -> {
                    Pair<String, String> assignmentForUser = getAssignmentForUser(entity.getAssignments(), userPublicId);
                    return new DrawEdition(entity.getEditionPublicId(), entity.getName(), entity.getGroupPublicId(), "",
                            assignmentForUser.getSecond(), assignmentForUser.getFirst());
                })
                .toList();
    }

    private Pair<String, String> getAssignmentForUser(List<Assignment> assignments, String userPublicId) {
        String receiverPublicId = assignments.stream()
                .filter(assignment -> assignment.getDonor().equals(userPublicId))
                .map(Assignment::getReceiver)
                .findFirst()
                .orElse(null);

        if (receiverPublicId == null) {
            return null;
        }
        UserService.UserDto userByPublicId = userService.getUserByPublicId(receiverPublicId);
        return Pair.of(receiverPublicId, userByPublicId.name() + " " + userByPublicId.lastname());
    }

    public UUID newEdition(@NotBlank String groupPublicId, @NotBlank String name) {
        List<UserService.UserDto> groupMembers = groupService.getGroupMembers(groupPublicId);
        DrawEditionEntity drawEditionEntity = new DrawEditionEntity(UUID.randomUUID(), name, ZonedDateTime.now(), groupPublicId);
        drawEditionRepository.save(drawEditionEntity);

        List<Assignment> assignments = pairUsers(groupMembers, drawEditionEntity);
        return drawEditionEntity.getEditionPublicId();
    }

    private List<Assignment> pairUsers(List<UserService.UserDto> users, DrawEditionEntity drawEditionEntity) {
        List<String> AVAILABLE = new ArrayList<>(users.stream().map(UserService.UserDto::userPublicId).toList());
        List<String> TO_DRAW;
        List<Assignment> assignments = new ArrayList<>();

        do {
            log.info("shuffle list");
            TO_DRAW = new ArrayList<>(AVAILABLE);
            Collections.shuffle(TO_DRAW);
        } while (!validateAssignments(users, TO_DRAW));

        for (int i = 0; i < users.size(); i++) {
            UserService.UserDto user = users.get(i);
            Assignment assignment = new Assignment(user.userPublicId(), TO_DRAW.get(i), drawEditionEntity);
            assignmentsRepository.save(assignment);
            assignments.add(assignment);
        }

        return assignments;
    }

    private String getName(List<UserService.UserDto> users, String userPublicId) {
        return users.stream()
                .filter(user -> user.userPublicId().equals(userPublicId))
                .findFirst()
                .map(user -> user.name() + " " + user.lastname())
                .orElse(null);
    }

    private boolean validateAssignments(List<UserService.UserDto> users, List<String> assignments) {
        for (int i = 0; i < users.size(); i++) {
            UserService.UserDto user = users.get(i);
            String assignment = assignments.get(i);
            if (user.userPublicId().equals(assignment) || user.excluded().contains(assignment)) {
                return false;
            }
        }
        return true;
    }

    public List<DrawEdition> findEditionsForUser(String userPublicId) {
        List<GroupService.Group> groups = groupService.findUserGroups(userPublicId)
                .stream()
                .toList();

        List<String> list = groups.stream().map(GroupService.Group::groupPublicId).toList();

        return drawEditionRepository.findAllByStatusAndGroupPublicIdIn(EditionStatus.ACTIVE, list)
                .stream()
                .map(entity -> {
                    Pair<String, String> assignmentForUser = getAssignmentForUser(entity.getAssignments(), userPublicId);
                    return new DrawEdition(entity.getEditionPublicId(), entity.getName(), entity.getGroupPublicId(),
                            getGroupName(entity.getGroupPublicId(), groups), assignmentForUser.getSecond(), assignmentForUser.getFirst());
                })
                .toList();

    }

    private String getGroupName(String groupPublicId, List<GroupService.Group> groups) {
        return groups.stream()
                .filter(g -> g.groupPublicId().equals(groupPublicId))
                .map(GroupService.Group::name)
                .findFirst()
                .orElse(null);
    }

    public record DrawEdition(UUID editionPublicId, String name, String groupPublicId, String groupName,
                              String assignmentForUser, String assignmentMemberId) {
    }
}
