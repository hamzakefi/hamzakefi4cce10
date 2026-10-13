package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.EquipementRepository;

import java.util.List;

// Note : l'entité Equipement ne possède pas de champ quantite dans le modèle JPA actuel.
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
