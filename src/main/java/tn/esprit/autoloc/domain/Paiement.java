package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPaiement;

    private BigDecimal montant;
    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiement;

    /**
     * Côté PROPRIÉTAIRE de la relation Contrat <-> Paiement.
     * La clé étrangère contrat_id est portée par la table paiement.
     * FetchType.LAZY : le Contrat n'est chargé que si on y accède explicitement.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id")
    private Contrat contrat;
}