package com.grh.grh.position;

import com.grh.grh.config.SecurityConfig;
import com.grh.grh.exception.ConflictException;
import com.grh.grh.exception.ResourceNotFoundException;
import com.grh.grh.security.JwtService;
import com.grh.grh.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PositionController.class)
@Import(SecurityConfig.class)
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PositionService positionService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_debeRetornar200ConListaDeCargos() throws Exception {
        when(positionService.getAll()).thenReturn(List.of(
                new PositionResponseDTO(1L, "Desarrollador", JobCategory.SENIOR, List.of()),
                new PositionResponseDTO(2L, "Analista", JobCategory.JUNIOR, List.of())
        ));

        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Desarrollador"))
                .andExpect(jsonPath("$[1].name").value("Analista"));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_debeRetornar200CuandoExiste() throws Exception {
        when(positionService.getById(1L)).thenReturn(
                new PositionResponseDTO(1L, "Desarrollador", JobCategory.SENIOR, List.of())
        );

        mockMvc.perform(get("/api/positions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Desarrollador"));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void create_debeRetornar201CuandoSeCreaCargo() throws Exception {
        PositionRequestDTO request = new PositionRequestDTO("Desarrollador", JobCategory.SENIOR);
        PositionResponseDTO response = new PositionResponseDTO(1L, "Desarrollador", JobCategory.SENIOR, List.of());

        when(positionService.create(any(PositionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Desarrollador"));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void update_debeRetornar200CuandoSeActualizaCargo() throws Exception {
        PositionRequestDTO request = new PositionRequestDTO("Ingeniero", JobCategory.LEAD);
        PositionResponseDTO response = new PositionResponseDTO(1L, "Ingeniero", JobCategory.LEAD, List.of());

        when(positionService.update(eq(1L), any(PositionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/positions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ingeniero"))
                .andExpect(jsonPath("$.jobCategory").value("LEAD"));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_debeRetornar204CuandoSeElimina() throws Exception {
        mockMvc.perform(delete("/api/positions/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void create_debeRetornar403CuandoRolNoTienePermiso() throws Exception {
        PositionRequestDTO request = new PositionRequestDTO("Desarrollador", JobCategory.SENIOR);

        mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void create_debeRetornar400SiElNombreEstaVacio() throws Exception {
        mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"jobCategory\":\"SENIOR\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_debeRetornar400SiLaCategoriaNoExiste() throws Exception {
        mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dev\",\"jobCategory\":\"NOEXISTE\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());   // requiere el handler de HttpMessageNotReadableException
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_debeRetornar409SiElCargoEstaEnUso() throws Exception {
        doThrow(new ConflictException("En uso")).when(positionService).delete(1L);

        mockMvc.perform(delete("/api/positions/1").with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("En uso"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_debeRetornar404SiNoExiste() throws Exception {
        when(positionService.getById(99L)).thenThrow(new ResourceNotFoundException("Cargo no encontrado: 99"));

        mockMvc.perform(get("/api/positions/99"))
                .andExpect(status().isNotFound());
    }
    @Test
    void getAll_sinAutenticar_debeRetornar401() throws Exception {
        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void delete_debeRetornar403ParaManager() throws Exception {
        mockMvc.perform(delete("/api/positions/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}