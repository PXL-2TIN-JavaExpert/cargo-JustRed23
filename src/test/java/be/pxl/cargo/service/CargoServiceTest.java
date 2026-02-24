package be.pxl.cargo.service;

import be.pxl.cargo.api.request.CreateCargoRequest;
import be.pxl.cargo.api.response.CargoStatistics;
import be.pxl.cargo.domain.Cargo;
import be.pxl.cargo.domain.Location;
import be.pxl.cargo.exceptions.NonUniqueCodeException;
import be.pxl.cargo.repository.CargoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoServiceTest {

    @Mock
    private CargoRepository cargoRepository;

    @InjectMocks
    private CargoService cargoService;

    @Test
    public void testCreateCargoSucceeds() {
        CreateCargoRequest validRequest = new CreateCargoRequest("C01", 150d, Location.AIRPORT_X, Location.WAREHOUSE_A);
        cargoService.createCargo(validRequest);
        verify(cargoRepository, times(1)).save(any(Cargo.class));
    }

    @Test
    public void testCreateCargoFailsWithDuplicateCode() {
        when(cargoRepository.findCargoByCode(any())).thenReturn(Optional.of(new Cargo()));
        CreateCargoRequest validRequest = new CreateCargoRequest("C01", 150d, Location.AIRPORT_X, Location.WAREHOUSE_A);
        assertThrows(NonUniqueCodeException.class, () -> cargoService.createCargo(validRequest));
        verify(cargoRepository, never()).save(any(Cargo.class));
    }

    @Test
    public void testGetCargoStatistics() {
        Cargo cargo = new Cargo("CARGO1", 200d, Location.AIRPORT_X, Location.WAREHOUSE_A);
        Cargo cargo2 = new Cargo("CARGO2", 300d, Location.AIRPORT_X, Location.WAREHOUSE_A);
        cargo2.arrive(Location.SEA_PORT_Z);
        Cargo cargo3 = new Cargo("CARGO3", 250d, Location.AIRPORT_X, Location.WAREHOUSE_A);
        cargo3.arrive(Location.WAREHOUSE_A);
        when(cargoRepository.findAll()).thenReturn(List.of(cargo, cargo2, cargo3));

        CargoStatistics statistics = cargoService.getCargoStatistics();
        assertEquals(3, statistics.getStatusCount().size());
        assertEquals(cargo2.getCode(), statistics.getHeaviestCargo());
        assertEquals(250d, statistics.getAverageCargoWeight());
        assertEquals(1, statistics.getCountCargosAtWarehouseA());
        assertEquals(0d, statistics.getTotalWeightDeliveredAtCityB());

        verify(cargoRepository, times(1)).findAll();
    }
}
