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

    @OneToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}