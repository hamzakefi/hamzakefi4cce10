package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.MaintenanceRepository;

import java.util.List;

/**
 * Implémentation du service Maintenance.
 *
 * <p>L'entité Maintenance possède : idMaintenance, dateDebut, dateFin, description, vehicule.
 * Note : le modèle ne possède pas de champ "cout". La règle "refuser un coût négatif" du TP
 * ne peut pas s'appliquer au modèle actuel (documenté dans docs/service-notes.md, section D).
 * La règle "date de fin antérieure à la date de début" est bien implémentée.</p>
 *
 * <p>Règles métier :</p>
 * <ul>
 *   <li>create : refuse un idMaintenance non nul</li>
 *   <li>create/update : refuse une dateFin antérieure ou égale à dateDebut</li>
 *   <li>findById : lève ResourceNotFoundException si absent</li>
 *   <li>update : copie dateDebut, dateFin, description, vehicule</li>
 *   <li>deleteById : vérifie l'existence avant suppression</li>
 * </ul>
 */
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

    /**
     * Valide que la date de fin est postérieure à la date de début.
     * Tolérant si l'une ou l'autre des dates est nulle.
     */
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
