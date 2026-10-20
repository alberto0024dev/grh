package com.grh.grh.employee;

import com.grh.grh.config.SecurityConfig;
import com.grh.grh.exception.BadRequestException;
import com.grh.grh.exception.ConflictException;
import com.grh.grh.exception.ForbiddenAccessException;
import com.grh.grh.exception.ResourceNotFoundException;
import com.grh.grh.security.JwtService;
import com.grh.grh.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private EmployeeService employeeService;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;
    @MockitoBean private JwtService jwtService;

    // Un usuario por rol, con las mismas authorities que le da ApplicationUserRole
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(authorities = {"ROLE_ADMIN", "employee:view", "employee:update", "reports:view"})
    @interface AsAdmin {}

    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(authorities = {"ROLE_MANAGER", "employee:view", "employee:update",
            "reports:view", "profile:read", "profile:update"})
    @interface AsManager {}

    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(authorities = {"ROLE_EMPLOYEE", "profile:read", "profile:update"})
    @interface AsEmployee {}

    // ===================== AUXILIARES =====================

    private EmployeeResponseDTO response(Long id, String name) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(name.toLowerCase() + "@grh.test");
        return dto;
    }

    // JSON que enviaría el frontend al crear
    private String createJson() {
        return """
            {
              "name": "Ana",
              "firstSurname": "García",
              "secondSurname": "López",
              "idNumber": "12345678Z",
              "address": "Calle Mayor 1",
              "emergencyContact": "Juan 600000000",
              "phoneNumber": "612345678",
              "email": "ana@grh.test",
              "birthDate": "1995-05-10",
              "socialSecurityNumber": "123456789012",
              "role": "EMPLOYEE",
              "departmentId": 1,
              "positionIds": [1]
            }
            """;
    }

    private String updateJson() {
        return """
            {
              "name": "Ana",
              "firstSurname": "García",
              "secondSurname": "López",
              "address": "Calle Nueva 2",
              "emergencyContact": "Juan 600000000",
              "phoneNumber": "612345678",
              "departmentId": 1,
              "positionIds": [1]
            }
            """;
    }

    private void postCreate(String json, int expectedStatus) throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON).content(json).with(csrf()))
                .andExpect(status().is(expectedStatus));
    }

    // ===================== CREATE =====================

    @Test
    @AsAdmin
    void create_devuelve201ConLaContrasenaTemporal() throws Exception {
        EmployeeResponseDTO created = response(1L, "Ana");
        created.setTemporaryPassword("Abc123xyz456");
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON).content(createJson()).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.temporaryPassword").value("Abc123xyz456"));
    }

    @Test
    @AsAdmin
    void create_nuncaDevuelveDniNiSeguridadSocial() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(response(1L, "Ana"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON).content(createJson()).with(csrf()))
                .andExpect(jsonPath("$.idNumber").doesNotExist())
                .andExpect(jsonPath("$.socialSecurityNumber").doesNotExist());
    }

    @Test
    @AsAdmin
    void create_conDniDeLetraIncorrecta_devuelve400() throws Exception {
        postCreate(createJson().replace("12345678Z", "12345678K"), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_conTelefonoDeOchoDigitos_devuelve400() throws Exception {
        postCreate(createJson().replace("612345678", "61234567"), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_conSeguridadSocialConLetras_devuelve400() throws Exception {
        postCreate(createJson().replace("123456789012", "12345678901a"), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_conEmailMalFormado_devuelve400() throws Exception {
        postCreate(createJson().replace("ana@grh.test", "ana@grh"), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_conNombreConNumeros_devuelve400() throws Exception {
        postCreate(createJson().replace("\"name\": \"Ana\"", "\"name\": \"Ana123\""), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_conFechaDeNacimientoFutura_devuelve400() throws Exception {
        postCreate(createJson().replace("1995-05-10", LocalDate.now().plusYears(1).toString()), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_siEsMenorDeDieciseisAnios_devuelve400() throws Exception {
        postCreate(createJson().replace("1995-05-10", LocalDate.now().minusYears(15).toString()), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_sinCargos_devuelve400() throws Exception {
        postCreate(createJson().replace("[1]", "[]"), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_sinDepartamento_devuelve400() throws Exception {
        postCreate(createJson().replace("\"departmentId\": 1,", ""), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_conRolInexistente_devuelve400() throws Exception {
        // Requiere el handler de HttpMessageNotReadableException
        postCreate(createJson().replace("\"EMPLOYEE\"", "\"NOEXISTE\""), 400);
        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @AsAdmin
    void create_conJsonMalFormado_devuelve400() throws Exception {
        postCreate("{ esto no es json", 400);
    }

    @Test
    @AsAdmin
    void create_traduceLosErroresDelServicio() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                .thenThrow(new ConflictException("Ya existe un empleado con ese DNI"))
                .thenThrow(new BadRequestException("El email debe ser del dominio @grh.test"))
                .thenThrow(new ResourceNotFoundException("Departamento no encontrado"));

        postCreate(createJson(), 409);
        postCreate(createJson(), 400);
        postCreate(createJson(), 404);
    }

    // ===================== UPDATE =====================

    @Test
    @AsAdmin
    void update_devuelve200() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeUpdateDTO.class))).thenReturn(response(1L, "Ana"));

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @AsAdmin
    void update_sinDireccion_devuelve400() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson().replace("\"address\": \"Calle Nueva 2\",", ""))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.address").exists());
        verify(employeeService, never()).updateEmployee(any(), any());
    }

    @Test
    @AsAdmin
    void update_conTelefonoInvalido_devuelve400() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson().replace("612345678", "abcdefghi"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
        verify(employeeService, never()).updateEmployee(any(), any());
    }

    @Test
    @AsAdmin
    void update_siNoExiste_devuelve404() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(EmployeeUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Empleado no encontrado: 99"));

        mockMvc.perform(put("/api/employees/99")
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson()).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Empleado no encontrado: 99"));
    }

    @Test
    @AsAdmin
    void update_conIdNoNumerico_devuelve400() throws Exception {
        // Requiere el handler de MethodArgumentTypeMismatchException
        mockMvc.perform(put("/api/employees/abc")
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson()).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ===================== ACTIVAR / DESACTIVAR =====================

    @Test
    @AsAdmin
    void deactivate_yActivate_respondenConExito() throws Exception {
        mockMvc.perform(patch("/api/employees/1/deactivate").with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(patch("/api/employees/1/activate").with(csrf()))
                .andExpect(status().isNoContent());
        verify(employeeService).deactivate(1L);
        verify(employeeService).activate(1L);
    }

    @Test
    @AsAdmin
    void deactivate_siNoExiste_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Empleado no encontrado: 99"))
                .when(employeeService).deactivate(99L);

        mockMvc.perform(patch("/api/employees/99/deactivate").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ===================== LECTURA =====================

    @Test
    @AsAdmin
    void getAll_devuelveLaLista() throws Exception {
        when(employeeService.getAll()).thenReturn(List.of(response(1L, "Ana"), response(2L, "Luis")));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("Luis"));
    }

    @Test
    @AsManager
    void getById_loPuedeLlamarUnManager() throws Exception {
        when(employeeService.getById(5L)).thenReturn(response(5L, "Ana"));

        mockMvc.perform(get("/api/employees/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @AsManager
    void getById_siElServicioDeniegaElAcceso_devuelve403() throws Exception {
        when(employeeService.getById(7L))
                .thenThrow(new ForbiddenAccessException("No tienes acceso a este empleado"));

        mockMvc.perform(get("/api/employees/7"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes acceso a este empleado"));
    }

    // ===================== PERFIL PROPIO (/me) =====================

    @Test
    @AsEmployee
    void me_devuelveElPerfil() throws Exception {
        when(employeeService.getOwnProfile()).thenReturn(response(3L, "Ana"));

        mockMvc.perform(get("/api/employees/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @AsManager
    void me_tambienLoPuedeUsarUnManager() throws Exception {
        when(employeeService.getOwnProfile()).thenReturn(response(1L, "Pedro"));

        mockMvc.perform(get("/api/employees/me")).andExpect(status().isOk());
    }

    @Test
    @AsEmployee
    void updateMe_devuelve200() throws Exception {
        when(employeeService.updateMe(any(EmployeeProfileDTO.class))).thenReturn(response(3L, "Ana"));

        mockMvc.perform(patch("/api/employees/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\": \"Calle Nueva 5\", \"phoneNumber\": \"612345678\"}")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @AsEmployee
    void updateMe_conSoloUnCampo_esValido() throws Exception {
        // PATCH: lo que no viene (null) no se toca
        when(employeeService.updateMe(any(EmployeeProfileDTO.class))).thenReturn(response(3L, "Ana"));

        mockMvc.perform(patch("/api/employees/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\": \"612345678\"}")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @AsEmployee
    void updateMe_conDireccionVacia_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/employees/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\": \"\"}").with(csrf()))
                .andExpect(status().isBadRequest());
        verify(employeeService, never()).updateMe(any());
    }

    @Test
    @AsEmployee
    void updateMe_conTelefonoInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/employees/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\": \"abc\"}").with(csrf()))
                .andExpect(status().isBadRequest());
        verify(employeeService, never()).updateMe(any());
    }

    // ===================== SEGURIDAD POR ROL =====================

    @Test
    void sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/employees/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @AsEmployee
    void empleado_noPuedeCrearListarNiVerAOtros() throws Exception {
        postCreate(createJson(), 403);
        mockMvc.perform(get("/api/employees")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/employees/5")).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson()).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/employees/1/deactivate").with(csrf()))
                .andExpect(status().isForbidden());
        verify(employeeService, never()).createEmployee(any());
        verify(employeeService, never()).getAll();
    }

    @Test
    @AsManager
    void manager_noPuedeCrearEditarNiDesactivar() throws Exception {
        postCreate(createJson(), 403);
        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson()).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/employees/1/deactivate").with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/employees")).andExpect(status().isForbidden());
        verify(employeeService, never()).createEmployee(any());
        verify(employeeService, never()).updateEmployee(any(), any());
        verify(employeeService, never()).deactivate(any());
    }

    @Test
    @AsAdmin
    void admin_noTienePerfilPropio_devuelve403() throws Exception {
        // El admin no tiene ficha de empleado y no lleva la authority profile:read
        mockMvc.perform(get("/api/employees/me")).andExpect(status().isForbidden());
        verify(employeeService, never()).getOwnProfile();
    }
}
