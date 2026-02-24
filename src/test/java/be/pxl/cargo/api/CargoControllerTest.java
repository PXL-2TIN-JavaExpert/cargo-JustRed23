package be.pxl.cargo.api;

import be.pxl.cargo.api.request.CreateCargoRequest;
import be.pxl.cargo.domain.Location;
import be.pxl.cargo.exceptions.NonUniqueCodeException;
import be.pxl.cargo.service.CargoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CargoController.class)
public class CargoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CargoService cargoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testAddCargoSucceeds() throws Exception {
        CreateCargoRequest validRequest = new CreateCargoRequest("C01", 150d, Location.AIRPORT_X, Location.WAREHOUSE_A);
        mockMvc.perform(MockMvcRequestBuilders.post("/cargos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
        ).andExpect(status().isCreated());

        verify(cargoService, times(1)).createCargo(any(CreateCargoRequest.class));
    }

    @Test
    public void testAddCargoWithDuplicateCode() throws Exception {
        doThrow(new NonUniqueCodeException()).when(cargoService).createCargo(any(CreateCargoRequest.class));
        CreateCargoRequest validRequest = new CreateCargoRequest("C01", 150d, Location.AIRPORT_X, Location.WAREHOUSE_A);
        mockMvc.perform(MockMvcRequestBuilders.post("/cargos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
        ).andExpect(status().isBadRequest());

        verify(cargoService, times(1)).createCargo(any(CreateCargoRequest.class));
    }

    @Test
    public void testAddCargoWithMissingCode() throws Exception {
        CreateCargoRequest missingCode = new CreateCargoRequest("", 150d, Location.AIRPORT_X, Location.WAREHOUSE_A);
        mockMvc.perform(MockMvcRequestBuilders.post("/cargos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingCode))
        ).andExpect(status().isBadRequest());

            verify(cargoService, never()).createCargo(any(CreateCargoRequest.class));
    }

    @Test
    public void testAddCargoWithInvalidWeight() throws Exception {
        CreateCargoRequest invalidWeight = new CreateCargoRequest("C02", 50d, Location.AIRPORT_X, Location.WAREHOUSE_A);
        mockMvc.perform(MockMvcRequestBuilders.post("/cargos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidWeight))
        ).andExpect(status().isBadRequest());

        verify(cargoService, never()).createCargo(any(CreateCargoRequest.class));
    }

    @Test
    public void testAddCargoWithInvalidCargoOrigin() throws Exception {
        CreateCargoRequest missingOrigin = new CreateCargoRequest("C03", 150d, null, Location.WAREHOUSE_A);
        mockMvc.perform(MockMvcRequestBuilders.post("/cargos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingOrigin))
        ).andExpect(status().isBadRequest());

        verify(cargoService, never()).createCargo(any(CreateCargoRequest.class));
    }

    @Test
    public void testAddCargoWithInvalidCargoDestination() throws Exception {
        CreateCargoRequest missingDestination = new CreateCargoRequest("C04", 150d, Location.AIRPORT_X, null);
        mockMvc.perform(MockMvcRequestBuilders.post("/cargos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingDestination))
        ).andExpect(status().isBadRequest());

        verify(cargoService, never()).createCargo(any(CreateCargoRequest.class));
    }

    @Test
    public void testGetCargoStatistics() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/cargos/statistics"))
                .andExpect(status().isOk());

        verify(cargoService, times(1)).getCargoStatistics();
    }
}
