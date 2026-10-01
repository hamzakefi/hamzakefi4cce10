package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVehicule;

    private String immatriculation;
    private String marque;
    private String modele;
    private Double prixLocationJour;

    @Enumerated(EnumType.STRING)
    private StatutVehicule statut; // DISPONIBLE, LOUE, EN_MAINTENANCE

    @ManyToOne
    @JoinColumn(name = "agence_id")
    private Agence agence;

    @OneToMany(mappedBy = "vehicule")
    private List<Reservation> reservations;
}
