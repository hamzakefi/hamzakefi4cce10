package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.ReservationRepository;

import java.util.List;

/**
 * Implémentation du service Reservation.
 *
 * <p>L'entité Reservation possède : idReservation, dateDebut, dateFin,
 * statut (StatutReservation), client, vehicule, contrat (@OneToOne).</p>
 *
 * <p>Règles métier :</p>
 * <ul>
 *   <li>create : refuse un idReservation non nul</li>
 *   <li>create : refuse une dateFin antérieure ou égale à dateDebut</li>
 *   <li>findById : lève ResourceNotFoundException si absent</li>
 *   <li>update : refuse une dateFin antérieure ou égale à dateDebut</li>
 *   <li>update : copie dateDebut, dateFin, statut, client, vehicule, contrat</li>
 *   <li>deleteById : vérifie l'existence avant suppression</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final ReservationRepository reservationRepository;

    @Override
    public Reservation create(Reservation reservation) {
        if (reservation.getIdReservation() != null) {
            throw new IllegalArgumentException(
                "Une nouvelle réservation ne doit pas avoir d'identifiant"
            );
        }
        validerDates(reservation);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation update(Long id, Reservation reservation) {
        Reservation existant = findById(id);

        validerDates(reservation);

        existant.setDateDebut(reservation.getDateDebut());
        existant.setDateFin(reservation.getDateFin());
        existant.setStatut(reservation.getStatut());
        existant.setClient(reservation.getClient());
        existant.setVehicule(reservation.getVehicule());
        existant.setContrat(reservation.getContrat());

        return reservationRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        reservationRepository.deleteById(id);
    }

    /**
     * Valide que la date de fin est postérieure à la date de début.
     * Les deux dates peuvent être nulles (réservation incomplète tolérée à la création).
     */
    private void validerDates(Reservation reservation) {
        if (reservation.getDateDebut() != null
                && reservation.getDateFin() != null
                && !reservation.getDateFin().isAfter(reservation.getDateDebut())) {
            throw new IllegalArgumentException(
                "La date de fin doit être postérieure à la date de début"
            );
        }
    }
}
