package com.civictrace.issue;

import com.civictrace.auth.Role;
import com.civictrace.auth.User;
import com.civictrace.auth.UserRepository;
import com.civictrace.common.api.ConflictException;
import com.civictrace.common.api.NotFoundException;
import com.civictrace.realtime.IssueChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class IssueService {

    private static final Set<IssueStatus> OPEN_STATUSES = EnumSet.of(
            IssueStatus.REPORTED, IssueStatus.UNDER_REVIEW, IssueStatus.IN_PROGRESS
    );
    private static final double SUGGESTION_RADIUS_KM = 2.0;

    private final IssueRepository issueRepository;
    private final IssueSupportRepository supportRepository;
    private final IssueUpdateRepository updateRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public IssueService(IssueRepository issueRepository,
                        IssueSupportRepository supportRepository,
                        IssueUpdateRepository updateRepository,
                        AssignmentRepository assignmentRepository,
                        UserRepository userRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.issueRepository = issueRepository;
        this.supportRepository = supportRepository;
        this.updateRepository = updateRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public IssueResponses.Page list(String query, IssueCategory category, IssueStatus status,
                                    Pageable pageable, User currentUser) {
        Page<Issue> page = issueRepository.search(normalizeQuery(query), category, status, pageable);
        return new IssueResponses.Page(
                page.getContent().stream().map(issue -> toSummary(issue, currentUser)).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public IssueResponses.Page listMine(User reporter, IssueStatus status, Pageable pageable) {
        Page<Issue> page = issueRepository.findByReporter(reporter.getId(), status, pageable);
        return new IssueResponses.Page(
                page.getContent().stream().map(issue -> toSummary(issue, reporter)).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        );
    }

    @Transactional
    public IssueResponses.Detail create(User reporter, IssueRequests.Create request) {
        Issue issue = new Issue(
                reporter,
                request.title().trim(),
                request.description().trim(),
                request.category(),
                request.latitude(),
                request.longitude(),
                normalizeOptional(request.locationLabel())
        );
        Issue saved = issueRepository.save(issue);
        updateRepository.save(new IssueUpdate(saved, reporter, null, IssueStatus.REPORTED, "Report submitted"));
        eventPublisher.publishEvent(new IssueChangeEvent("CREATED", saved.getId(), saved.getTitle(), saved.getStatus(), Instant.now()));
        return detail(saved, reporter);
    }

    @Transactional(readOnly = true)
    public IssueResponses.Detail detail(UUID issueId, User currentUser) {
        Issue issue = findIssue(issueId);
        return detail(issue, currentUser);
    }

    @Transactional(readOnly = true)
    public List<IssueResponses.Update> updates(UUID issueId) {
        Issue issue = findIssue(issueId);
        return updateRepository.findByIssueIdOrderByCreatedAtAsc(issue.getId()).stream()
                .map(this::toUpdate)
                .toList();
    }

    @Transactional
    public IssueResponses.Detail support(UUID issueId, User user) {
        Issue issue = findIssue(issueId);
        if (supportRepository.existsByIssueIdAndUserId(issueId, user.getId())) {
            throw new ConflictException("You already support this report.");
        }
        try {
            supportRepository.save(new IssueSupport(issue, user));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("You already support this report.");
        }
        eventPublisher.publishEvent(new IssueChangeEvent("SUPPORTED", issue.getId(), issue.getTitle(), issue.getStatus(), Instant.now()));
        return detail(issue, user);
    }

    @Transactional
    public IssueResponses.Detail changeStatus(UUID issueId, User actor, IssueRequests.StatusChange request) {
        Issue issue = findIssue(issueId);
        IssueStatus oldStatus = issue.getStatus();
        if (!StatusWorkflow.isValid(oldStatus, request.status())) {
            throw new IllegalArgumentException("Invalid status transition from " + oldStatus + " to " + request.status() + ".");
        }
        issue.changeStatus(request.status());
        issueRepository.save(issue);
        updateRepository.save(new IssueUpdate(issue, actor, oldStatus, request.status(), normalizeOptional(request.comment())));
        eventPublisher.publishEvent(new IssueChangeEvent("STATUS_CHANGED", issue.getId(), issue.getTitle(), issue.getStatus(), Instant.now()));
        return detail(issue, actor);
    }

    @Transactional
    public IssueResponses.Detail addProgress(UUID issueId, User actor, IssueRequests.Progress request) {
        Issue issue = findIssue(issueId);
        String comment = request.comment().trim();
        updateRepository.save(new IssueUpdate(issue, actor, issue.getStatus(), issue.getStatus(), comment));
        eventPublisher.publishEvent(new IssueChangeEvent("UPDATED", issue.getId(), issue.getTitle(), issue.getStatus(), Instant.now()));
        return detail(issue, actor);
    }

    @Transactional
    public IssueResponses.Detail assign(UUID issueId, User actor, IssueRequests.Assignment request) {
        Issue issue = findIssue(issueId);
        User staff = userRepository.findById(request.staffId())
                .orElseThrow(() -> new NotFoundException("Staff member not found."));
        if (staff.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("Assignments can only target staff members.");
        }
        assignmentRepository.findFirstByIssueIdAndEndedAtIsNullOrderByAssignedAtDesc(issueId)
                .ifPresent(Assignment::end);
        assignmentRepository.save(new Assignment(issue, staff, actor));
        eventPublisher.publishEvent(new IssueChangeEvent("ASSIGNED", issue.getId(), issue.getTitle(), issue.getStatus(), Instant.now()));
        return detail(issue, actor);
    }

    @Transactional(readOnly = true)
    public List<IssueResponses.Suggestion> suggestions(IssueRequests.Suggestions request) {
        return issueRepository.findOpenByCategory(request.category(), OPEN_STATUSES).stream()
                .map(issue -> new IssueResponses.Suggestion(
                        issue.getId(), issue.getTitle(), issue.getCategory(), issue.getStatus(),
                        issue.getLatitude(), issue.getLongitude(), issue.getLocationLabel(),
                        distanceKm(request.latitude(), request.longitude(), issue.getLatitude(), issue.getLongitude())
                ))
                .filter(suggestion -> suggestion.distanceKm() <= SUGGESTION_RADIUS_KM)
                .sorted(Comparator.comparingDouble(IssueResponses.Suggestion::distanceKm))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.civictrace.auth.UserResponse> staffMembers() {
        return userRepository.findAllByRoleOrderByNameAsc(Role.STAFF).stream().map(com.civictrace.auth.UserResponse::from).toList();
    }

    private IssueResponses.Detail detail(Issue issue, User currentUser) {
        return new IssueResponses.Detail(
                toSummary(issue, currentUser),
                updateRepository.findByIssueIdOrderByCreatedAtAsc(issue.getId()).stream().map(this::toUpdate).toList()
        );
    }

    private IssueResponses.Summary toSummary(Issue issue, User currentUser) {
        Assignment activeAssignment = assignmentRepository.findFirstByIssueIdAndEndedAtIsNullOrderByAssignedAtDesc(issue.getId()).orElse(null);
        boolean supported = currentUser != null && supportRepository.existsByIssueIdAndUserId(issue.getId(), currentUser.getId());
        return new IssueResponses.Summary(
                issue.getId(), issue.getTitle(), issue.getDescription(), issue.getCategory(), issue.getStatus(),
                issue.getLatitude(), issue.getLongitude(), issue.getLocationLabel(), publicName(issue.getReporter()),
                issue.getCreatedAt(), issue.getUpdatedAt(), issue.getResolvedAt(),
                supportRepository.countByIssueId(issue.getId()), supported,
                activeAssignment == null ? null : activeAssignment.getStaff().getId(),
                activeAssignment == null ? null : activeAssignment.getStaff().getName()
        );
    }

    private IssueResponses.Update toUpdate(IssueUpdate update) {
        User actor = update.getActor();
        return new IssueResponses.Update(
                update.getId(), actor.getId(), publicName(actor), actor.getRole(), update.getOldStatus(),
                update.getNewStatus(), update.getComment(), update.getCreatedAt()
        );
    }

    private Issue findIssue(UUID issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Issue not found."));
    }

    private String publicName(User user) {
        return user.getRole() == Role.RESIDENT ? "Community resident" : user.getName();
    }

    private String normalizeQuery(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private double distanceKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
