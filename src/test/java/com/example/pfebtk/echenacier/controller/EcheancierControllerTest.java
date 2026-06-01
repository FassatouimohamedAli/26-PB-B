package com.example.pfebtk.echenacier.controller;

import com.example.pfebtk.echeancier.controller.EcheancierController;
import com.example.pfebtk.echeancier.dto.EcheancierResp;
import com.example.pfebtk.echeancier.entity.StatutEcheance;
import com.example.pfebtk.echeancier.service.EcheancierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EcheancierControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EcheancierService echeancierService;

    @InjectMocks
    private EcheancierController echeancierController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(echeancierController).build();
    }

    @Test
    void getAll_ShouldReturnListOfEcheanciers() throws Exception {
        List<EcheancierResp> mockList = Arrays.asList(
                createEcheancierResp(1L, 500.0, StatutEcheance.A_VENIR, LocalDate.now().plusMonths(1)),
                createEcheancierResp(2L, 500.0, StatutEcheance.PAYE, LocalDate.now().minusMonths(1))
        );
        when(echeancierService.getAll()).thenReturn(mockList);

        mockMvc.perform(get("/api/resp/echeanciers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].statut").value("PAYE"));

        verify(echeancierService, times(1)).getAll();
    }

    @Test
    void getByDemande_WithValidId_ShouldReturnEcheanciers() throws Exception {
        List<EcheancierResp> mockList = Arrays.asList(
                createEcheancierResp(1L, 500.0, StatutEcheance.A_VENIR, LocalDate.now().plusMonths(1))
        );
        when(echeancierService.getByDemande(10L)).thenReturn(mockList);

        mockMvc.perform(get("/api/resp/demande/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(echeancierService, times(1)).getByDemande(10L);
    }

    @Test
    void getByDemande_WithNoEcheanciers_ShouldReturnEmptyList() throws Exception {
        when(echeancierService.getByDemande(99L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/resp/demande/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(echeancierService, times(1)).getByDemande(99L);
    }

    @Test
    void payer_WithValidId_ShouldCallService() throws Exception {
        doNothing().when(echeancierService).payer(5L);

        mockMvc.perform(patch("/api/resp/payer/5"))
                .andExpect(status().isOk());

        verify(echeancierService, times(1)).payer(5L);
    }

    @Test
    void getRetards_ShouldReturnOnlyRetardedEcheances() throws Exception {
        List<EcheancierResp> mockList = Arrays.asList(
                createEcheancierResp(1L, 500.0, StatutEcheance.EN_RETARD, LocalDate.now().minusDays(10)),
                createEcheancierResp(2L, 500.0, StatutEcheance.EN_RETARD, LocalDate.now().minusDays(5))
        );
        when(echeancierService.getRetards()).thenReturn(mockList);

        mockMvc.perform(get("/api/resp/retards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].statut").value("EN_RETARD"))
                .andExpect(jsonPath("$[1].statut").value("EN_RETARD"));

        verify(echeancierService, times(1)).getRetards();
    }

    @Test
    void getRetards_WhenNoRetards_ShouldReturnEmptyList() throws Exception {
        when(echeancierService.getRetards()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/resp/retards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(echeancierService, times(1)).getRetards();
    }

    private EcheancierResp createEcheancierResp(Long id, Double montant, StatutEcheance statut, LocalDate dateEcheance) {
        return EcheancierResp.builder()
                .id(id)
                .statut(statut)
                .dateEcheance(dateEcheance)
                .build();
    }
}