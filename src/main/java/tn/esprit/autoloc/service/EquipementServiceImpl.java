package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.EquipementRepository;

import java.util.List;

/**
 * Implémentation du service Equipement.
 *
 * <p>L'entité Equipement possède : idEquipement, libelle, vehicules (Set).
 * Note : le modèle ne possède pas de champ quantité. La règle "refuser une quantité négative"
 * du TP ne peut pas s'appliquer au modèle actuel. Cette contrainte est documentée dans
 * docs/service-notes.md (section D).</p>
 *
 * <p>Règles métier appliquées :</p>
 * <ul>
 *   <li>create : refuse un idEquipement non nul</li>
 *   <li>create : refuse un libellé vide ou nul</li>
 *   <li>findById : lève ResourceNotFoundException si absent</li>
 *   <li>update : copie uniquement libelle ; la collection vehicules est gérée par Vehicule</li>
 *   <li>deleteById : vérifie l'existence avant suppression</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class EquipementServiceImpl implements IEquipementService {

    private final EquipementRepository equipementRepository;

    @Override
    public Equipement create(Equipement equipement) {
        if (equipement.getIdEquipement() != null) {
            throw new IllegalArgumentException(
                "Un nouvel équipement ne doit pas avoir d'identifiant"
            );
        }
        if (equipement.getLibelle() == null || equipement.getLibelle().isBlank()) {
            throw new IllegalArgumentException(
                "Le libellé de l'équipement est obligatoire"
            );
        }
        return equipementRepository.save(equipement);
    }

    @Override
    public Equipement findById(Long id) {
        return equipementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipement", id));
    }

    @Override
    public List<Equipement> findAll() {
        return equipementRepository.findAll();
    }

    @Override
    public Equipement update(Long id, Equipement equipement) {
        Equipement existant = findById(id);

        if (equipement.getLibelle() == null || equipement.getLibelle().isBlank()) {
            throw new IllegalArgumentException(
                "Le libellé de l'équipement est obligatoire"
            );
        }

        existant.setLibelle(equipement.getLibelle());
        // La collection vehicules est gérée via le côté propriétaire (Vehicule) : non écrasée ici.

        return equipementRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!equipementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipement", id);
        }
        equipementRepository.deleteById(id);
    }
}
