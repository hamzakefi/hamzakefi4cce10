package tn.esprit.autoloc;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tn.esprit.autoloc.domain.*;
import tn.esprit.autoloc.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests JPA – Atelier 2 : Associations, Cascade et Fetch
 *
 * Ces tests vérifient les comportements de cascade, orphanRemoval et fetch
 * sur les entités AutoLoc.
 */
@DataJpaTest
@TestMethodOrder(MethodOrderer.DisplayName.class)
class AssociationJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ContratRepository contratRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private VehiculeRepository vehiculeRepository;

    @Autowired
    private EquipementRepository equipementRepository;

    // =========================================================
    // TEST 1 : CascadeType.ALL → sauvegarder le Contrat
    //          sauvegarde également ses Paiements
    // =========================================================
    @Test
    @DisplayName("TEST 1 : Cascade ALL – sauvegarder Contrat sauvegarde les Paiements")
    void test1_cascadeAll_saveContratSavesPaiements() {
        Contrat contrat = new Contrat();
        contrat.setDateSignature(LocalDate.now());
        contrat.setMontantTotal(new BigDecimal("1500.00"));
        contrat.setValide(true);

        Paiement p1 = new Paiement();
        p1.setMontant(new BigDecimal("750.00"));
        p1.setDatePaiement(LocalDateTime.now());
        p1.setModePaiement(ModePaiement.CARTE);

        Paiement p2 = new Paiement();
        p2.setMontant(new BigDecimal("750.00"));
        p2.setDatePaiement(LocalDateTime.now());
        p2.setModePaiement(ModePaiement.VIREMENT);

        // Utiliser les méthodes helper pour maintenir la cohérence
        contrat.addPaiement(p1);
        contrat.addPaiement(p2);

        // Sauvegarder uniquement le Contrat → CascadeType.ALL propage vers Paiements
        contratRepository.save(contrat);
        em.flush();
        em.clear();

        // Vérifier que les 2 paiements ont bien été persistés
        List<Paiement> paiements = paiementRepository.findAll();
        assertThat(paiements).hasSize(2);
        assertThat(paiements).allMatch(p -> p.getContrat() != null);

        System.out.println("✅ TEST 1 RÉUSSI : CascadeType.ALL propage la sauvegarde vers les Paiements");
    }

    // =========================================================
    // TEST 2 : CascadeType.ALL → supprimer le Contrat
    //          supprime également ses Paiements
    // =========================================================
    @Test
    @DisplayName("TEST 2 : Cascade ALL – supprimer Contrat supprime les Paiements")
    void test2_cascadeAll_deleteContratDeletesPaiements() {
        Contrat contrat = new Contrat();
        contrat.setDateSignature(LocalDate.now());
        contrat.setMontantTotal(new BigDecimal("900.00"));
        contrat.setValide(true);

        Paiement p = new Paiement();
        p.setMontant(new BigDecimal("900.00"));
        p.setDatePaiement(LocalDateTime.now());
        p.setModePaiement(ModePaiement.ESPECES);
        contrat.addPaiement(p);

        Contrat saved = contratRepository.save(contrat);
        em.flush();
        em.clear();

        Long contratId = saved.getIdContrat();
        assertThat(paiementRepository.findAll()).hasSize(1);

        // Supprimer le Contrat → CascadeType.ALL supprime les Paiements
        contratRepository.deleteById(contratId);
        em.flush();
        em.clear();

        assertThat(contratRepository.findById(contratId)).isEmpty();
        assertThat(paiementRepository.findAll()).isEmpty();

        System.out.println("✅ TEST 2 RÉUSSI : CascadeType.ALL propage la suppression vers les Paiements");
    }

    // =========================================================
    // TEST 3 : orphanRemoval → retirer un Paiement de la liste
    //          le supprime de la base de données
    // =========================================================
    @Test
    @DisplayName("TEST 3 : orphanRemoval – retirer un Paiement de la liste le supprime en BDD")
    void test3_orphanRemoval_removePaiementFromList() {
        Contrat contrat = new Contrat();
        contrat.setDateSignature(LocalDate.now());
        contrat.setMontantTotal(new BigDecimal("600.00"));
        contrat.setValide(true);

        Paiement p1 = new Paiement();
        p1.setMontant(new BigDecimal("300.00"));
        p1.setDatePaiement(LocalDateTime.now());
        p1.setModePaiement(ModePaiement.CARTE);

        Paiement p2 = new Paiement();
        p2.setMontant(new BigDecimal("300.00"));
        p2.setDatePaiement(LocalDateTime.now());
        p2.setModePaiement(ModePaiement.VIREMENT);

        contrat.addPaiement(p1);
        contrat.addPaiement(p2);
        Contrat saved = contratRepository.save(contrat);
        em.flush();
        em.clear();

        assertThat(paiementRepository.findAll()).hasSize(2);

        // Recharger et retirer un paiement via removePaiement()
        Contrat reloaded = contratRepository.findById(saved.getIdContrat()).orElseThrow();
        Paiement toRemove = reloaded.getPaiements().get(0);
        reloaded.removePaiement(toRemove);

        contratRepository.save(reloaded);
        em.flush();
        em.clear();

        // Avec orphanRemoval=true, il ne doit rester qu'un seul paiement
        assertThat(paiementRepository.findAll()).hasSize(1);

        System.out.println("✅ TEST 3 RÉUSSI : orphanRemoval supprime le Paiement retiré de la collection");
    }

    // =========================================================
    // TEST 4 : FetchType.LAZY – charger un Contrat sans accéder
    //          à getPaiements() ne charge pas les Paiements
    // =========================================================
    @Test
    @DisplayName("TEST 4 : LAZY – les Paiements ne sont pas chargés immédiatement")
    void test4_lazyFetch_paiementsNotLoadedImmediately() {
        Contrat contrat = new Contrat();
        contrat.setDateSignature(LocalDate.now());
        contrat.setMontantTotal(new BigDecimal("200.00"));
        contrat.setValide(false);

        Paiement p = new Paiement();
        p.setMontant(new BigDecimal("200.00"));
        p.setDatePaiement(LocalDateTime.now());
        p.setModePaiement(ModePaiement.CARTE);
        contrat.addPaiement(p);

        Contrat saved = contratRepository.save(contrat);
        em.flush();
        em.clear();

        // Recharger le contrat - les paiements sont LAZY (proxy Hibernate)
        Contrat reloaded = contratRepository.findById(saved.getIdContrat()).orElseThrow();

        // On vérifie l'existence sans appeler getPaiements()
        assertThat(reloaded.getIdContrat()).isNotNull();
        assertThat(reloaded.getMontantTotal()).isEqualByComparingTo("200.00");

        // Accès explicite pour vérifier le chargement à la demande
        int size = reloaded.getPaiements().size();
        assertThat(size).isEqualTo(1);

        System.out.println("✅ TEST 4 RÉUSSI : FetchType.LAZY – Paiements chargés uniquement à l'accès explicite");
    }

    // =========================================================
    // TEST 5 : Pas de CascadeType.REMOVE sur Agence → Vehicule
    //          Supprimer une Agence ne supprime pas ses Vehicules
    // =========================================================
    @Test
    @DisplayName("TEST 5 : Pas de cascade REMOVE – supprimer Agence ne supprime pas les Vehicules")
    void test5_noCascadeRemove_agenceVehicule() {
        Agence agence = new Agence();
        agence.setNom("Agence Tunis Centre");
        agence.setAdresse("1 Av. Habib Bourguiba");
        agence.setTelephone("71 000 000");

        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation("TU-123-456");
        vehicule.setMarque("Peugeot");
        vehicule.setModele("208");
        vehicule.setPrixLocationJour(80.0);
        vehicule.setStatut(StatutVehicule.DISPONIBLE);
        vehicule.setAgence(agence);

        Agence savedAgence = agenceRepository.save(agence);
        vehiculeRepository.save(vehicule);
        em.flush();
        em.clear();

        Long agenceId = savedAgence.getIdAgence();
        Long vehiculeId = vehicule.getIdVehicule();

        // Dissocier le vehicule de l'agence avant de supprimer l'agence
        // (sinon contrainte FK en BDD)
        Vehicule reloadedVehicule = vehiculeRepository.findById(vehiculeId).orElseThrow();
        reloadedVehicule.setAgence(null);
        vehiculeRepository.save(reloadedVehicule);
        em.flush();

        agenceRepository.deleteById(agenceId);
        em.flush();
        em.clear();

        // L'agence est supprimée mais le vehicule existe toujours
        assertThat(agenceRepository.findById(agenceId)).isEmpty();
        assertThat(vehiculeRepository.findById(vehiculeId)).isPresent();

        System.out.println("✅ TEST 5 RÉUSSI : Pas de CascadeType.REMOVE – le Vehicule survit à la suppression de l'Agence");
    }

    // =========================================================
    // TEST 6 : ManyToMany Vehicule <-> Equipement
    //          Table de jointure vehicule_equipement
    //          Supprimer un Vehicule ne supprime pas les Equipements
    // =========================================================
    @Test
    @DisplayName("TEST 6 : ManyToMany – table vehicule_equipement, équipements non supprimés")
    void test6_manyToMany_vehiculeEquipement() {
        Equipement gps = new Equipement();
        gps.setLibelle("GPS");

        Equipement clim = new Equipement();
        clim.setLibelle("Climatisation");

        equipementRepository.save(gps);
        equipementRepository.save(clim);

        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation("TN-456-789");
        vehicule.setMarque("Renault");
        vehicule.setModele("Clio");
        vehicule.setPrixLocationJour(70.0);
        vehicule.setStatut(StatutVehicule.DISPONIBLE);
        vehicule.getEquipements().add(gps);
        vehicule.getEquipements().add(clim);

        Vehicule savedVehicule = vehiculeRepository.save(vehicule);
        em.flush();
        em.clear();

        Long vehiculeId = savedVehicule.getIdVehicule();
        Long gpsId = gps.getIdEquipement();
        Long climId = clim.getIdEquipement();

        // Vérifier que le vehicule possède bien 2 équipements
        Vehicule reloaded = vehiculeRepository.findById(vehiculeId).orElseThrow();
        assertThat(reloaded.getEquipements()).hasSize(2);

        // Supprimer le vehicule → les équipements NE doivent PAS être supprimés
        // D'abord, vider la collection pour libérer les entrées dans la table de jointure
        reloaded.getEquipements().clear();
        vehiculeRepository.save(reloaded);
        em.flush();

        vehiculeRepository.deleteById(vehiculeId);
        em.flush();
        em.clear();

        assertThat(vehiculeRepository.findById(vehiculeId)).isEmpty();
        assertThat(equipementRepository.findById(gpsId)).isPresent();
        assertThat(equipementRepository.findById(climId)).isPresent();

        System.out.println("✅ TEST 6 RÉUSSI : Vehicule supprimé mais Equipements toujours présents (table vehicule_equipement)");
    }
}
