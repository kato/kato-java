package me.danwi.kato.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.danwi.kato.common.ExceptionResult;
import me.danwi.kato.common.exception.KatoAccessDeniedException;
import me.danwi.kato.common.exception.KatoAuthenticationException;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class KatoServerExceptionTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mvc;

    @BeforeEach
    public void before() {
        //获取mockmvc对象实例
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
    }

    @Test
    public void testKatoAuth() throws Exception {
        final String exceptionName = KatoAuthenticationException.class.getName();

        final ResultActions perform = mvc.perform(MockMvcRequestBuilders.post("/katoAuth").contentType(MediaType.APPLICATION_JSON_VALUE));
        perform.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string(StringContains.containsStringIgnoringCase(exceptionName)));
        final ExceptionResult exceptionResult = objectMapper.readValue(perform.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), ExceptionResult.class);
        Assertions.assertTrue(exceptionResult.getId().contains(exceptionName));
    }

    @Test
    public void testKatoAccessDenied() throws Exception {
        final String exceptionName = KatoAccessDeniedException.class.getName();

        final ResultActions perform = mvc.perform(MockMvcRequestBuilders.post("/katoAccessDenied").contentType(MediaType.APPLICATION_JSON_VALUE));
        perform.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(StringContains.containsStringIgnoringCase(exceptionName)));
        final ExceptionResult exceptionResult = objectMapper.readValue(perform.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), ExceptionResult.class);
        Assertions.assertTrue(exceptionResult.getId().contains(exceptionName));
    }


    @Test
    public void testSpringAuth() throws Exception {
        final String exceptionName = KatoAuthenticationException.class.getName();

        final ResultActions perform = mvc.perform(MockMvcRequestBuilders.post("/springAuth").contentType(MediaType.APPLICATION_JSON_VALUE));
        perform.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string(StringContains.containsStringIgnoringCase(exceptionName)));
        final ExceptionResult exceptionResult = objectMapper.readValue(perform.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), ExceptionResult.class);
        Assertions.assertTrue(exceptionResult.getId().contains(exceptionName));
    }

    @Test
    public void testSpringAccessDenied() throws Exception {
        final String exceptionName = KatoAccessDeniedException.class.getName();

        final ResultActions perform = mvc.perform(MockMvcRequestBuilders.post("/springAccessDenied").contentType(MediaType.APPLICATION_JSON_VALUE));
        perform.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(StringContains.containsStringIgnoringCase(exceptionName)));
        final ExceptionResult exceptionResult = objectMapper.readValue(perform.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), ExceptionResult.class);
        Assertions.assertTrue(exceptionResult.getId().contains(exceptionName));
    }

}
