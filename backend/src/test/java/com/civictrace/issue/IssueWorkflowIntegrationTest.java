package com.civictrace.issue;

import com.civictrace.auth.Role;
import com.civictrace.auth.User;
import com.civictrace.auth.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class IssueWorkflowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private User resident;
    private User staff;
    private MockHttpSession residentSession;
    private MockHttpSession staffSession;

    @BeforeEach
    void setUp() throws Exception {
        resident = userRepository.save(new User("Workflow Resident", "workflow-resident-" + UUID.randomUUID() + "@example.test", passwordEncoder.encode("resident-password"), Role.RESIDENT));
        staff = userRepository.save(new User("Workflow Staff", "workflow-staff-" + UUID.randomUUID() + "@example.test", passwordEncoder.encode("staff-password"), Role.STAFF));
        residentSession = login(resident.getEmail(), "resident-password");
        staffSession = login(staff.getEmail(), "staff-password");
    }

    @Test
    void residentCanCreateViewAndSupportAnIssueOnlyOnce() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/issues")
                        .session(residentSession).with(csrf()).contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Pothole at bus stop","description":"A deep pothole is unsafe for two-wheelers.","category":"ROADS","latitude":28.6139,"longitude":77.2090,"locationLabel":"Central bus stop"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.issue.status").value("REPORTED"))
                .andExpect(jsonPath("$.updates[0].newStatus").value("REPORTED"))
                .andReturn();
        UUID issueId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).path("issue").path("id").asText());

        mockMvc.perform(get("/api/issues/" + issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issue.title").value("Pothole at bus stop"))
                .andExpect(jsonPath("$.updates.length()").value(1));

        mockMvc.perform(post("/api/issues/" + issueId + "/support").session(residentSession).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issue.supportCount").value(1));
        mockMvc.perform(post("/api/issues/" + issueId + "/support").session(residentSession).with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void staffMustFollowValidTransitionsAndHistoryIsPersisted() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/issues")
                        .session(residentSession).with(csrf()).contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Broken streetlight","description":"The lamp is out after sunset.","category":"STREETLIGHTS","latitude":28.62,"longitude":77.21,"locationLabel":"Park lane"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        UUID issueId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).path("issue").path("id").asText());

        mockMvc.perform(patch("/api/issues/" + issueId + "/status").session(staffSession).with(csrf()).contentType(APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\",\"comment\":\"Skipped review\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/issues/" + issueId + "/status").session(staffSession).with(csrf()).contentType(APPLICATION_JSON)
                        .content("{\"status\":\"UNDER_REVIEW\",\"comment\":\"Reviewed by staff\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/issues/" + issueId + "/status").session(staffSession).with(csrf()).contentType(APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\",\"comment\":\"Crew scheduled\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/issues/" + issueId + "/status").session(staffSession).with(csrf()).contentType(APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\",\"comment\":\"Light repaired\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issue.status").value("RESOLVED"));

        mockMvc.perform(get("/api/issues/" + issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updates.length()").value(4))
                .andExpect(jsonPath("$.issue.resolvedAt").isNotEmpty());
    }

    @Test
    void residentCannotChangeStaffWorkflow() throws Exception {
        mockMvc.perform(patch("/api/issues/" + UUID.randomUUID() + "/status")
                        .session(residentSession).with(csrf()).contentType(APPLICATION_JSON)
                        .content("{\"status\":\"UNDER_REVIEW\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicIssueListWorksWhenOptionalSearchFilterIsOmitted() throws Exception {
        mockMvc.perform(get("/api/issues?size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    private MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(APPLICATION_JSON)
                        .content("{" + "\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
