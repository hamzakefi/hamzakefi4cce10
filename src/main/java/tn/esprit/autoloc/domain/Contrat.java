package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrat implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idContrat;

    private LocalDate dateSignature;
    private BigDecimal montantTotal;
    private Boolean valide;

    /**
     * Côté INVERSE de la relation Contrat <-> Paiement (bidirectionnelle).
     * mappedBy = "contrat" correspond exactement au champ dans Paiement.
     * CascadeType.ALL : sauvegarder/modifier/supprimer le Contrat propage vers Paiement.
     * orphanRemoval = true : retirer un Paiement de la liste supprime-le de la BDD.
     * FetchType.LAZY : les paiements ne sont chargés que si on y accède.
     */
    @OneToMany(
        mappedBy = "contrat",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Paiement> paiements = new ArrayList<>();

    // -------------------------------------------------------
    // Méthodes helper pour maintenir la cohérence des deux côtés
    // -------------------------------------------------------
    public void addPaiement(Paiement paiement) {
        paiements.add(paiement);
        paiement.setContrat(this);
    }

    public void removePaiement(Paiement paiement) {
        paiements.remove(paiement);
        paiement.setContrat(null);
    }
}