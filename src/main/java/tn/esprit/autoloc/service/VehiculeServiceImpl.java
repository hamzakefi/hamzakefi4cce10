package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.VehiculeRepository;

import java.util.List;

/**
 * Implémentation du service Vehicule.
 *
 * <p>L'entité Vehicule possède : idVehicule, immatriculation, marque, modele,
 * prixLocationJour (Double), statut (StatutVehicule), agence, reservations, maintenances, equipements.
 * Le modèle ne possède pas de champ kilométrage.</p>
 *
 * <p>Règles métier :</p>
 * <ul>
 *   <li>create : refuse un idVehicule non nul</li>
 *   <li>create : refuse un prixLocationJour négatif</li>
 *   <li>findById : lève ResourceNotFoundException si absent</li>
 *   <li>update : copie immatriculation, marque, modele, prixLocationJour, statut, agence</li>
 *   <li>update : refuse un prixLocationJour négatif</li>
 *   <li>deleteById : vérifie l'existence avant suppression</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements IVehiculeService {

    private final VehiculeRepository vehiculeRepository;

    @Override
    public Vehicule create(Vehicule vehicule) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException(
                "Un nouveau véhicule ne doit pas avoir d'identifiant"
            );
        }
        if (vehicule.getPrixLocationJour() != null && vehicule.getPrixLocationJour() < 0) {
            throw new IllegalArgumentException(
                "Le prix de location journalier ne peut pas être négatif"
            );
        }
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    public Vehicule update(Long id, Vehicule vehicule) {
        Vehicule existant = findById(id);

        if (vehicule.getPrixLocationJour() != null && vehicule.getPrixLocationJour() < 0) {
            throw new IllegalArgumentException(
                "Le prix de location journalier ne peut pas être négatif"
            );
        }

        existant.setImmatriculation(vehicule.getImmatriculation());
        existant.setMarque(vehicule.getMarque());
        existant.setModele(vehicule.getModele());
        existant.setPrixLocationJour(vehicule.getPrixLocationJour());
        existant.setStatut(vehicule.getStatut());
        existant.setAgence(vehicule.getAgence());
        // Les collections reservations, maintenances et equipements sont conservées.

        return vehiculeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule", id);
        }
        vehiculeRepository.deleteById(id);
    }
}
