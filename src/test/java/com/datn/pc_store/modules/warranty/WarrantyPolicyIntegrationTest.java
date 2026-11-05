package com.datn.pc_store.modules.warranty;

import com.datn.pc_store.modules.warranty.repository.WarrantyPolicyRepository;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyTargetType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:warranty-policy-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false",
    "app.admin.password=",
    "app.jwt.secret=0123456789012345678901234567890123456789012345678901234567890123"
})
@Transactional
@WithMockUser(roles = "ADMIN")
class WarrantyPolicyIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired WarrantyPolicyRepository repository;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    MockMvc mvc;
    final String base = "/api/admin/warranty-policies";

    @BeforeEach void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void crudPersistsNumericStatusAndSoftDeletes() throws Exception {
        mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" Bảo hành tiêu chuẩn \",\"durationDays\":365}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.name").value("Bảo hành tiêu chuẩn"));
        em.flush(); em.clear();
        Long id = repository.findAll().getFirst().getId();
        assertThat(jdbc.queryForObject("select status from warranty_policy where id = ?", Integer.class, id)).isEqualTo(1);
        mvc.perform(get(base + "/" + id)).andExpect(status().isOk());
        mvc.perform(put(base + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Mở rộng\",\"durationDays\":730}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.durationDays").value(730));
        mvc.perform(patch(base + "/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("INACTIVE"));
        mvc.perform(get(base).param("keyword", "Mở rộng").param("status", "INACTIVE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(base).param("status", "ACTIVE"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(delete(base + "/" + id)).andExpect(status().isNoContent());
        em.flush(); em.clear();
        assertThat(jdbc.queryForObject("select is_deleted from warranty_policy where id = ?", Integer.class, id)).isEqualTo(1);
        mvc.perform(get(base + "/" + id)).andExpect(status().isNotFound());
        mvc.perform(get(base)).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test void validatesInputAndTargetReferences() throws Exception {
        for (String body : new String[]{"{}", "{\"name\":\"X\",\"durationDays\":0}",
                "{\"name\":\"X\",\"durationDays\":365,\"targetType\":2}",
                "{\"name\":\"X\",\"durationDays\":365,\"targetType\":0}"}) {
            mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        for (int type : new int[]{0, 1}) {
            mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"X\",\"durationDays\":365,\"targetType\":%d,\"targetId\":999999}".formatted(type)))
                    .andExpect(status().isNotFound());
        }
        mvc.perform(get(base).param("targetType", "2")).andExpect(status().isBadRequest());
        mvc.perform(delete(base + "/999999")).andExpect(status().isNotFound());
    }

    @Test void categoryScopeCanBeFilteredAndCleared() throws Exception {
        var category = com.datn.pc_store.modules.category.entity.Category.builder()
                .name("GPU").slug("warranty-gpu").build();
        em.persist(category);
        em.flush();
        mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"GPU policy\",\"durationDays\":365,\"targetType\":0,\"targetId\":%d}"
                        .formatted(category.getId())))
                .andExpect(status().isCreated());
        em.flush(); em.clear();
        Long id = repository.findAll().getFirst().getId();
        assertThat(repository.findById(id).orElseThrow().getTargetType()).isEqualTo(WarrantyPolicyTargetType.CATEGORY);
        mvc.perform(get(base).param("targetType", "CATEGORY").param("targetId", category.getId().toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(base).param("targetType", "PRODUCT"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(put(base + "/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"General policy\",\"durationDays\":365}"))
                .andExpect(status().isOk());
        em.flush(); em.clear();
        var policy = repository.findById(id).orElseThrow();
        assertThat(policy.getTargetType()).isNull();
        assertThat(policy.getTargetId()).isNull();
    }

    @Test void requiresAdmin() throws Exception {
        mvc.perform(get(base).with(anonymous())).andExpect(status().isUnauthorized());
        mvc.perform(post(base).with(user("customer").roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
