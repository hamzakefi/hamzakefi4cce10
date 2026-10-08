package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClient;

    private String nom;
    private String prenom;
    private String email;
    private String numPermis;

    /**
     * Côté INVERSE de la relation Client <-> Reservation (bidirectionnelle).
     * mappedBy = "client" correspond exactement au champ dans Reservation.
     * CascadeType.PERSIST : persister un Client persiste ses Reservations.
     * Pas de CascadeType.REMOVE : les reservations ne sont pas supprimées avec le client.
     * FetchType.LAZY : les reservations ne sont chargées que si on y accède.
     */
    @OneToMany(
        mappedBy = "client",
        fetch = FetchType.LAZY,
        cascade = CascadeType.PERSIST
    )
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();
}