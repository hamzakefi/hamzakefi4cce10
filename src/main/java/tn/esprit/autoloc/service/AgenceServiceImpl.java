package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.AgenceRepository;

import java.util.List;

/**
 * Implémentation du service Agence.
 *
 * <p>Règles métier :</p>
 * <ul>
 *   <li>create : refuse un idAgence non nul</li>
 *   <li>create : refuse un nom vide ou nul</li>
 *   <li>findById : lève ResourceNotFoundException si absent</li>
 *   <li>update : copie nom, adresse, telephone ; conserve les collections vehicules et employes</li>
 *   <li>deleteById : vérifie l'existence avant suppression</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AgenceServiceImpl implements IAgenceService {

    private final AgenceRepository agenceRepository;

    @Override
    public Agence create(Agence agence) {
        if (agence.getIdAgence() != null) {
            throw new IllegalArgumentException(
                "Une nouvelle agence ne doit pas avoir d'identifiant"
            );
        }
        if (agence.getNom() == null || agence.getNom().isBlank()) {
            throw new IllegalArgumentException(
                "Le nom de l'agence est obligatoire"
            );
        }
        return agenceRepository.save(agence);
    }

    @Override
    public Agence findById(Long id) {
        return agenceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Agence", id));
    }

    @Override
    public List<Agence> findAll() {
        return agenceRepository.findAll();
    }

    @Override
    public Agence update(Long id, Agence agence) {
        Agence existant = findById(id);

        if (agence.getNom() == null || agence.getNom().isBlank()) {
            throw new IllegalArgumentException(
                "Le nom de l'agence est obligatoire"
            );
        }

        // Copie des attributs scalaires uniquement ; les collections sont conservées.
        existant.setNom(agence.getNom());
        existant.setAdresse(agence.getAdresse());
        existant.setTelephone(agence.getTelephone());

        return agenceRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agence", id);
        }
        agenceRepository.deleteById(id);
    }
}
