package io.mazurkiewicz.gifter.draw;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
class DrawController {

    private final DrawEditionService drawEditionService;

    @PostMapping("/draws")
    @PreAuthorize("hasRole('ROLE_MOD')")
    ResponseEntity<NewDrawResponse> draw(@RequestBody @Valid NewDrawRequest request) {
        UUID newEdition = drawEditionService.newEdition(request.groupPublicId, request.name);
        return ResponseEntity.ok(new NewDrawResponse(newEdition));
    }

    record NewDrawRequest(@NotBlank String groupPublicId, @NotBlank String name) {
    }

    record NewDrawResponse(UUID editionPublicId) {
    }

}
