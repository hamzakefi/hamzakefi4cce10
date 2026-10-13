package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.PaiementRepository;

import java.util.List;

/**
 * Implémentation du service Paiement (lecture seule).
 *
 * <p>Les paiements sont créés, modifiés ou supprimés uniquement via ContratServiceImpl,
 * grâce à la relation @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
 * définie dans l'entité Contrat.</p>
 *
 * <p>Ce service n'expose que findById et findAll pour permettre la consultation.</p>
 */
@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements IPaiementService {

    private final PaiementRepository paiementRepository;

    @Override
    public Paiement findById(Long id) {
        return paiementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paiement", id));
    }

    @Override
    public List<Paiement> findAll() {
        return paiementRepository.findAll();
    }
}
