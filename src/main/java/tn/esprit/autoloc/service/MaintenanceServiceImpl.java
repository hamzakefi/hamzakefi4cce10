package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.MaintenanceRepository;

import java.util.List;

// Note : l'entité Maintenance ne possède pas de champ cout dans le modèle JPA actuel.
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements IMaintenanceService {

    private final MaintenanceRepository maintenanceRepository;

    @Override
    public Maintenance create(Maintenance maintenance) {
        if (maintenance.getIdMaintenance() != null) {
            throw new IllegalArgumentException(
                "Une nouvelle maintenance ne doit pas avoir d'identifiant"
            );
        }
        validerDates(maintenance);
        return maintenanceRepository.save(maintenance);
    }

    @Override
    public Maintenance findById(Long id) {
        return maintenanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Maintenance", id));
    }

    @Override
    public List<Maintenance> findAll() {
        return maintenanceRepository.findAll();
    }

    @Override
    public Maintenance update(Long id, Maintenance maintenance) {
        Maintenance existant = findById(id);

        validerDates(maintenance);

        existant.setDateDebut(maintenance.getDateDebut());
        existant.setDateFin(maintenance.getDateFin());
        existant.setDescription(maintenance.getDescription());
        existant.setVehicule(maintenance.getVehicule());

        return maintenanceRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Maintenance", id);
        }
        maintenanceRepository.deleteById(id);
    }

    /** Refuse une dateFin antérieure ou égale à dateDebut si les deux sont renseignées. */
    private void validerDates(Maintenance maintenance) {
        if (maintenance.getDateDebut() != null
                && maintenance.getDateFin() != null
                && !maintenance.getDateFin().isAfter(maintenance.getDateDebut())) {
            throw new IllegalArgumentException(
                "La date de fin de maintenance doit être postérieure à la date de début"
            );
        }
    }
}
