package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Paiement;

import java.util.List;

/**
 * Service Paiement en lecture seule.
 *
 * <p>Les opérations d'écriture (création, modification, suppression) passent
 * par ContratServiceImpl via la relation @OneToMany avec CascadeType.ALL.</p>
 */
public interface IPaiementService {

    Paiement findById(Long id);

    List<Paiement> findAll();
}
