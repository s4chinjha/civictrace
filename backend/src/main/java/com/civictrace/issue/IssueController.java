package com.civictrace.issue;

import com.civictrace.auth.User;
import com.civictrace.auth.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping("/issues")
    public IssueResponses.Page list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) IssueCategory category,
            @RequestParam(required = false) IssueStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        return issueService.list(query, category, status, pageable, currentUser(authentication));
    }

    @GetMapping("/my/issues")
    @PreAuthorize("hasRole('RESIDENT')")
    public IssueResponses.Page myIssues(
            @RequestParam(required = false) IssueStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        return issueService.listMine(requiredUser(authentication), status, pageable);
    }

    @PostMapping("/issues")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<IssueResponses.Detail> create(
            @Valid @RequestBody IssueRequests.Create request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.create(requiredUser(authentication), request));
    }

    @GetMapping("/issues/{issueId}")
    public IssueResponses.Detail detail(@PathVariable UUID issueId, Authentication authentication) {
        return issueService.detail(issueId, currentUser(authentication));
    }

    @GetMapping("/issues/{issueId}/updates")
    public List<IssueResponses.Update> updates(@PathVariable UUID issueId) {
        return issueService.updates(issueId);
    }

    @PostMapping("/issues/{issueId}/support")
    @PreAuthorize("hasRole('RESIDENT')")
    public IssueResponses.Detail support(@PathVariable UUID issueId, Authentication authentication) {
        return issueService.support(issueId, requiredUser(authentication));
    }

    @PostMapping("/issues/suggestions")
    @PreAuthorize("hasRole('RESIDENT')")
    public List<IssueResponses.Suggestion> suggestions(@Valid @RequestBody IssueRequests.Suggestions request) {
        return issueService.suggestions(request);
    }

    @PostMapping("/issues/{issueId}/assignments")
    @PreAuthorize("hasRole('STAFF')")
    public IssueResponses.Detail assign(
            @PathVariable UUID issueId,
            @Valid @RequestBody IssueRequests.Assignment request,
            Authentication authentication
    ) {
        return issueService.assign(issueId, requiredUser(authentication), request);
    }

    @PatchMapping("/issues/{issueId}/status")
    @PreAuthorize("hasRole('STAFF')")
    public IssueResponses.Detail changeStatus(
            @PathVariable UUID issueId,
            @Valid @RequestBody IssueRequests.StatusChange request,
            Authentication authentication
    ) {
        return issueService.changeStatus(issueId, requiredUser(authentication), request);
    }

    @PostMapping("/issues/{issueId}/updates")
    @PreAuthorize("hasRole('STAFF')")
    public IssueResponses.Detail addProgress(
            @PathVariable UUID issueId,
            @Valid @RequestBody IssueRequests.Progress request,
            Authentication authentication
    ) {
        return issueService.addProgress(issueId, requiredUser(authentication), request);
    }

    @GetMapping("/staff/members")
    @PreAuthorize("hasRole('STAFF')")
    public List<com.civictrace.auth.UserResponse> staffMembers() {
        return issueService.staffMembers();
    }

    private User currentUser(Authentication authentication) {
        return authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)
                ? null : principal.user();
    }

    private User requiredUser(Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null) {
            throw new IllegalStateException("Authentication is required.");
        }
        return user;
    }
}
