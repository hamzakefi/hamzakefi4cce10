package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.ClientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    private final ClientRepository clientRepository;

    @Override
    public Client create(Client client) {
        if (client.getIdClient() != null) {
            throw new IllegalArgumentException(
                "Un nouveau client ne doit pas avoir d'identifiant"
            );
        }
        if (client.getNom() == null || client.getNom().isBlank()) {
            throw new IllegalArgumentException(
                "Le nom du client est obligatoire"
            );
        }
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                "L'email du client est obligatoire"
            );
        }
        return clientRepository.save(client);
    }

    @Override
    public Client findById(Long id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    @Override
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Override
    public Client update(Long id, Client client) {
        Client existant = findById(id);

        if (client.getNom() == null || client.getNom().isBlank()) {
            throw new IllegalArgumentException(
                "Le nom du client est obligatoire"
            );
        }

        existant.setNom(client.getNom());
        existant.setPrenom(client.getPrenom());
        existant.setEmail(client.getEmail());
        existant.setNumPermis(client.getNumPermis());

        return clientRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client", id);
        }
        clientRepository.deleteById(id);
    }
}
