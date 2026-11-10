package com.datn.pc_store.modules.comment;

import com.datn.pc_store.modules.auth.security.CustomUserDetails;
import com.datn.pc_store.modules.comment.repository.ProductCommentRepository;
import com.datn.pc_store.modules.product.entity.Product;
import com.datn.pc_store.modules.product.repository.ProductRepository;
import com.datn.pc_store.modules.user.entity.User;
import com.datn.pc_store.modules.user.enums.RoleType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:comments;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false",
        "app.admin.password=",
        "app.jwt.secret=0123456789012345678901234567890123456789012345678901234567890123"
})
@Transactional
class CommentIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired ProductRepository products;
    @Autowired ProductCommentRepository comments;
    MockMvc mvc;
    Long productId;

    CustomUserDetails principal(long id, RoleType role) {
        return new CustomUserDetails(User.builder().id(id).username("user" + id)
                .passwordHash("unused").role(role).build());
    }

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        productId = products.saveAndFlush(Product.builder().name("PC").code("PC").slug("pc").build()).getId();
    }

    String path() { return "/api/products/" + productId + "/comments"; }

    long create(String path, long author) throws Exception {
        String json = mvc.perform(post(path).with(user(principal(author, RoleType.CUSTOMER)))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Hello\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.createdBy").value(author))
                .andExpect(jsonPath("$.lastModifiedBy").value(author))
                .andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
    }

    @Test void lifecycleAndAuditPreserveReplies() throws Exception {
        long root = create(path(), 11);
        long reply = create(path() + "/" + root + "/replies", 12);
        mvc.perform(put(path() + "/" + root).with(user(principal(11, RoleType.CUSTOMER)))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Edited\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").value("Edited"))
                .andExpect(jsonPath("$.createdBy").value(11));
        mvc.perform(delete(path() + "/" + root).with(user(principal(99, RoleType.ADMIN))))
                .andExpect(status().isNoContent());
        assertThat(comments.findById(root).orElseThrow().getLastModifiedBy()).isEqualTo(99L);
        assertThat(comments.findById(root).orElseThrow().getCreatedBy()).isEqualTo(11L);
        mvc.perform(get(path() + "/" + root).with(anonymous())).andExpect(status().isOk())
                .andExpect(jsonPath("$.isDeleted").value(true)).andExpect(jsonPath("$.deleted").doesNotExist())
                .andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(get(path()).param("parentId", "" + root).with(anonymous()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(reply));
        mvc.perform(post(path() + "/" + root + "/replies").with(user(principal(12, RoleType.CUSTOMER)))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Reply\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void permissionsValidationAndProductBoundaries() throws Exception {
        mvc.perform(post(path()).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Hello\"}"))
                .andExpect(status().isUnauthorized());
        long id = create(path(), 11);
        for (RoleType role : new RoleType[]{RoleType.CUSTOMER, RoleType.ADMIN}) {
            mvc.perform(put(path() + "/" + id).with(user(principal(12, role)))
                    .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Overwrite\"}"))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(delete(path() + "/" + id).with(user(principal(12, RoleType.CUSTOMER))))
                .andExpect(status().isForbidden());
        for (String body : new String[]{"{}", "{\"content\":\"   \"}", "{\"content\":\"" + "x".repeat(10001) + "\"}"}) {
            mvc.perform(post(path()).with(user(principal(11, RoleType.CUSTOMER)))
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
        mvc.perform(get(path()).param("size", "101")).andExpect(status().isBadRequest());
        Long other = products.saveAndFlush(Product.builder().name("Other").code("OTHER").slug("other").build()).getId();
        mvc.perform(post("/api/products/" + other + "/comments/" + id + "/replies")
                .with(user(principal(11, RoleType.CUSTOMER))).contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Cross product\"}")).andExpect(status().isNotFound());
    }
}
