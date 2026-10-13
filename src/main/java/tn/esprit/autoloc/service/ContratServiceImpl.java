package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.ContratRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implémentation du service Contrat.
 *
 * <p>Injection par constructeur via Lombok @RequiredArgsConstructor.
 * Spring utilise le constructeur généré pour injecter ContratRepository.
 * Le champ est déclaré final pour garantir l'immuabilité de la dépendance.</p>
 *
 * <p>Règles métier :</p>
 * <ul>
 *   <li>create : refuse un idContrat non nul</li>
 *   <li>findById : lève ResourceNotFoundException si absent</li>
 *   <li>update : refuse un montantTotal négatif, conserve les paiements existants</li>
 *   <li>deleteById : vérifie l'existence avant suppression</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ContratServiceImpl implements IContratService {

    private final ContratRepository contratRepository;

    // -------------------------------------------------------
    // Étape 2.3 — create()
    // -------------------------------------------------------
    @Override
    public Contrat create(Contrat contrat) {
        if (contrat.getIdContrat() != null) {
            throw new IllegalArgumentException(
                "Un nouveau contrat ne doit pas avoir d'identifiant"
            );
        }
        return contratRepository.save(contrat);
    }

    // -------------------------------------------------------
    // Étape 2.4 — findById()
    // -------------------------------------------------------
    @Override
    public Contrat findById(Long id) {
        return contratRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Contrat", id)
            );
    }

    // -------------------------------------------------------
    // Étape 2.5 — findAll()
    // -------------------------------------------------------
    @Override
    public List<Contrat> findAll() {
        return contratRepository.findAll();
    }

    // -------------------------------------------------------
    // Étape 2.6 — update()
    // Règle : ne jamais écraser l'idContrat ni la collection des paiements.
    // Validation : montantTotal ne peut pas être négatif.
    // -------------------------------------------------------
    @Override
    public Contrat update(Long id, Contrat contrat) {
        Contrat existant = findById(id);

        if (contrat.getMontantTotal() != null
                && contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Le montant total d'un contrat ne peut pas être négatif"
            );
        }

        // Copie des attributs modifiables uniquement
        existant.setDateSignature(contrat.getDateSignature());
        existant.setMontantTotal(contrat.getMontantTotal());
        existant.setValide(contrat.getValide());
        // Les paiements (collection) ne sont pas écrasés : existant.paiements est conservé.

        return contratRepository.save(existant);
    }

    // -------------------------------------------------------
    // Étape 2.7 — deleteById()
    // -------------------------------------------------------
    @Override
    public void deleteById(Long id) {
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contrat", id);
        }
        contratRepository.deleteById(id);
    }
}
