package com.civictrace.config;

import com.civictrace.auth.Role;
import com.civictrace.auth.User;
import com.civictrace.auth.UserRepository;
import com.civictrace.issue.Issue;
import com.civictrace.issue.IssueCategory;
import com.civictrace.issue.IssueRepository;
import com.civictrace.issue.IssueStatus;
import com.civictrace.issue.IssueUpdate;
import com.civictrace.issue.IssueUpdateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final IssueUpdateRepository updateRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedDemo;
    private final String staffEmail;
    private final String staffPassword;
    private final String residentEmail;
    private final String residentPassword;

    public DemoDataInitializer(
            UserRepository userRepository,
            IssueRepository issueRepository,
            IssueUpdateRepository updateRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed-demo:false}") boolean seedDemo,
            @Value("${app.demo-staff-email:}") String staffEmail,
            @Value("${app.demo-staff-password:}") String staffPassword,
            @Value("${app.demo-resident-email:resident@civictrace.local}") String residentEmail,
            @Value("${app.demo-resident-password:}") String residentPassword
    ) {
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.updateRepository = updateRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedDemo = seedDemo;
        this.staffEmail = staffEmail;
        this.staffPassword = staffPassword;
        this.residentEmail = residentEmail;
        this.residentPassword = residentPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedDemo) {
            return;
        }
        if (staffEmail.isBlank() || staffPassword.length() < 8 || residentPassword.length() < 8) {
            throw new IllegalStateException("Demo seeding requires email values and passwords of at least 8 characters.");
        }

        User staff = userRepository.findByEmailIgnoreCase(staffEmail.trim().toLowerCase()).orElseGet(() ->
                userRepository.save(new User("CivicTrace Operations", staffEmail.trim().toLowerCase(), passwordEncoder.encode(staffPassword), Role.STAFF))
        );
        User resident = userRepository.findByEmailIgnoreCase(residentEmail.trim().toLowerCase()).orElseGet(() ->
                userRepository.save(new User("Demo Resident", residentEmail.trim().toLowerCase(), passwordEncoder.encode(residentPassword), Role.RESIDENT))
        );

        if (issueRepository.count() > 0) {
            return;
        }

        createIssue(resident, "Deep pothole near the community gate", "A large pothole is slowing traffic and becomes difficult to see after rain.", IssueCategory.ROADS, "Sector 12 community gate", new BigDecimal("28.613900"), new BigDecimal("77.209000"), IssueStatus.IN_PROGRESS, staff, "Crew assigned for patching.");
        createIssue(resident, "Streetlight out on Park Avenue", "The streetlight beside the park has been dark for several nights.", IssueCategory.STREETLIGHTS, "Park Avenue", new BigDecimal("28.616200"), new BigDecimal("77.211600"), IssueStatus.RESOLVED, staff, "Bulb replaced and tested after dusk.");
        createIssue(resident, "Overflowing waste collection point", "The collection point is overflowing and needs an additional pickup.", IssueCategory.WASTE, "Market Road collection point", new BigDecimal("28.610500"), new BigDecimal("77.206700"), IssueStatus.REPORTED, staff, null);
    }

    private void createIssue(User reporter, String title, String description, IssueCategory category, String location,
                             BigDecimal latitude, BigDecimal longitude, IssueStatus finalStatus, User staff, String finalComment) {
        Issue issue = issueRepository.save(new Issue(reporter, title, description, category, latitude, longitude, location));
        updateRepository.save(new IssueUpdate(issue, reporter, null, IssueStatus.REPORTED, "Report submitted"));
        if (finalStatus == IssueStatus.REPORTED) {
            return;
        }
        issue.changeStatus(IssueStatus.UNDER_REVIEW);
        issueRepository.save(issue);
        updateRepository.save(new IssueUpdate(issue, staff, IssueStatus.REPORTED, IssueStatus.UNDER_REVIEW, "Report reviewed by operations."));
        if (finalStatus == IssueStatus.UNDER_REVIEW) {
            return;
        }
        issue.changeStatus(IssueStatus.IN_PROGRESS);
        issueRepository.save(issue);
        updateRepository.save(new IssueUpdate(issue, staff, IssueStatus.UNDER_REVIEW, IssueStatus.IN_PROGRESS, "Work has been scheduled."));
        if (finalStatus == IssueStatus.IN_PROGRESS) {
            return;
        }
        issue.changeStatus(finalStatus);
        issueRepository.save(issue);
        updateRepository.save(new IssueUpdate(issue, staff, IssueStatus.IN_PROGRESS, finalStatus, finalComment));
    }
}
