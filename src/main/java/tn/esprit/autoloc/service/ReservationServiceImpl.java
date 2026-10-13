package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.ReservationRepository;

import java.util.List;

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

    /** Refuse une dateFin nulle ou antérieure/égale à dateDebut si les deux sont renseignées. */
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
