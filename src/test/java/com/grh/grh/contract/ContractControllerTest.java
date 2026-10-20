package com.grh.grh.contract;


import com.grh.grh.config.SecurityConfig;
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

import java.math.BigDecimal;
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

@WebMvcTest(ContractController.class)
@Import(SecurityConfig.class)
class ContractControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ContractService contractService;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;
    @MockitoBean private JwtService jwtService;

    // ===================== AUXILIARES =====================

    private ContractResponseDTO response(Long id) {
        ContractResponseDTO dto = new ContractResponseDTO();
        dto.setId(id);
        dto.setContractType(ContractType.PERMANENT);
        dto.setSalary(new BigDecimal("30000"));
        return dto;
    }

    // JSON válido escrito a mano: así probamos exactamente lo que enviaría el frontend
    private String validCreateJson() {
        return """
            {
              "employeeId": 1,
              "contractType": "PERMANENT",
              "commencementDate": "%s",
              "vacationDays": 22,
              "salary": 30000,
              "workPlace": "OFFICE",
              "workSchedule": {
                "weeklyHours": 40,
                "startTime": "09:00:00",
                "endTime": "18:00:00",
                "daysOff": 2
              }
            }
            """.formatted(LocalDate.now());
    }

    private String validUpdateJson() {
        return """
            {
              "contractType": "PERMANENT",
              "commencementDate": "%s",
              "vacationDays": 25,
              "salary": 35000,
              "workPlace": "REMOTE",
              "workSchedule": {
                "weeklyHours": 35,
                "startTime": "08:00:00",
                "endTime": "16:00:00",
                "daysOff": 2
              }
            }
            """.formatted(LocalDate.now());
    }

    // ===================== CREATE =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_devuelve201() throws Exception {
        when(contractService.create(any(ContractRequestDTO.class))).thenReturn(response(1L));

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.contractType").value("PERMANENT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_sinSalario_devuelve400() throws Exception {
        String json = validCreateJson().replace("\"salary\": 30000,", "");

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON).content(json).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.salary").exists());
        verify(contractService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_conSalarioNegativo_devuelve400() throws Exception {
        String json = validCreateJson().replace("30000", "-5");

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON).content(json).with(csrf()))
                .andExpect(status().isBadRequest());
        verify(contractService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_temporalSinFechaDeFin_devuelve400() throws Exception {
        // Regla @AssertTrue: un contrato no permanente necesita fecha de fin
        String json = validCreateJson().replace("PERMANENT", "TEMPORARY");

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON).content(json).with(csrf()))
                .andExpect(status().isBadRequest());
        verify(contractService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_horarioConFinAnteriorAlInicio_devuelve400() throws Exception {
        String json = validCreateJson()
                .replace("\"startTime\": \"09:00:00\"", "\"startTime\": \"20:00:00\"");

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON).content(json).with(csrf()))
                .andExpect(status().isBadRequest());
        verify(contractService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_conTipoInexistente_devuelve400() throws Exception {
        String json = validCreateJson().replace("PERMANENT", "NOEXISTE");

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON).content(json).with(csrf()))
                .andExpect(status().isBadRequest());   // requiere el handler de HttpMessageNotReadableException
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_siElEmpleadoYaTieneContrato_devuelve409() throws Exception {
        when(contractService.create(any(ContractRequestDTO.class)))
                .thenThrow(new ConflictException("Este empleado ya tiene un contrato"));

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON).content(validCreateJson()).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Este empleado ya tiene un contrato"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_siElEmpleadoNoExiste_devuelve404() throws Exception {
        when(contractService.create(any(ContractRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Empleado para el contrato no encontrado"));

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON).content(validCreateJson()).with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ===================== UPDATE =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_devuelve200() throws Exception {
        when(contractService.update(eq(1L), any(ContractUpdateDTO.class))).thenReturn(response(1L));

        mockMvc.perform(put("/api/contracts/1")
                        .contentType(MediaType.APPLICATION_JSON).content(validUpdateJson()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_sinEmployeeId_esValido() throws Exception {
        // El DTO de edición no lleva employeeId: el contrato no cambia de dueño
        when(contractService.update(eq(1L), any(ContractUpdateDTO.class))).thenReturn(response(1L));

        mockMvc.perform(put("/api/contracts/1")
                        .contentType(MediaType.APPLICATION_JSON).content(validUpdateJson()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_conVacacionesFueraDeRango_devuelve400() throws Exception {
        String json = validUpdateJson().replace("\"vacationDays\": 25", "\"vacationDays\": 99");

        mockMvc.perform(put("/api/contracts/1")
                        .contentType(MediaType.APPLICATION_JSON).content(json).with(csrf()))
                .andExpect(status().isBadRequest());
        verify(contractService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_siNoExiste_devuelve404() throws Exception {
        when(contractService.update(eq(99L), any(ContractUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Contrato no encontrado: 99"));

        mockMvc.perform(put("/api/contracts/99")
                        .contentType(MediaType.APPLICATION_JSON).content(validUpdateJson()).with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ===================== DELETE =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/contracts/1").with(csrf()))
                .andExpect(status().isNoContent());
        verify(contractService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_siNoExiste_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Contrato no encontrado: 99"))
                .when(contractService).delete(99L);

        mockMvc.perform(delete("/api/contracts/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ===================== GET =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_devuelve200ConLista() throws Exception {
        when(contractService.getAll()).thenReturn(List.of(response(1L), response(2L)));

        mockMvc.perform(get("/api/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getById_loPuedeLlamarUnEmpleado() throws Exception {
        when(contractService.getById(1L)).thenReturn(response(1L));

        mockMvc.perform(get("/api/contracts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_noExponeLaPropiedadAuxiliarTimeRangeValid() throws Exception {
        ContractResponseDTO dto = response(1L);
        WorkSchedule w = new WorkSchedule();
        w.setWeeklyHours(40);
        w.setStartTime(java.time.LocalTime.of(9, 0));
        w.setEndTime(java.time.LocalTime.of(18, 0));
        w.setDaysOff(2);
        dto.setWorkSchedule(w);
        when(contractService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/contracts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workSchedule.weeklyHours").value(40))
                .andExpect(jsonPath("$.workSchedule.timeRangeValid").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getById_siElServicioDeniegaElAcceso_devuelve403() throws Exception {
        when(contractService.getById(2L))
                .thenThrow(new ForbiddenAccessException("No tienes acceso a este contrato."));

        mockMvc.perform(get("/api/contracts/2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tienes acceso a este contrato."));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getByEmployeeId_devuelve200() throws Exception {
        when(contractService.getByEmployeeId(5L)).thenReturn(response(9L));

        mockMvc.perform(get("/api/contracts/5/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByEmployeeId_sinContrato_devuelve404() throws Exception {
        when(contractService.getByEmployeeId(5L))
                .thenThrow(new ResourceNotFoundException("El empleado no tiene contrato"));

        mockMvc.perform(get("/api/contracts/5/employee"))
                .andExpect(status().isNotFound());
    }

    // ===================== SEGURIDAD POR ROL =====================

    @Test
    void sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/contracts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void empleado_noPuedeCrear_devuelve403() throws Exception {
        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON).content(validCreateJson()).with(csrf()))
                .andExpect(status().isForbidden());
        verify(contractService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void manager_noPuedeEditarNiBorrar_devuelve403() throws Exception {
        mockMvc.perform(put("/api/contracts/1")
                        .contentType(MediaType.APPLICATION_JSON).content(validUpdateJson()).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/contracts/1").with(csrf()))
                .andExpect(status().isForbidden());
        verify(contractService, never()).update(any(), any());
        verify(contractService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void manager_noPuedeListarTodosLosContratos_devuelve403() throws Exception {
        mockMvc.perform(get("/api/contracts"))
                .andExpect(status().isForbidden());
        verify(contractService, never()).getAll();
    }
}
