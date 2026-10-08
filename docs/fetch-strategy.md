# Fetch Strategy & Cascade – Atelier 2 AutoLoc

## Tableau récapitulatif des associations JPA

| Association | Type | Côté propriétaire | Fetch | Cascade | orphanRemoval | Justification |
|---|---|---|---|---|---|---|
| Contrat → Paiement | `@OneToMany` | `Paiement` (FK `contrat_id`) | `LAZY` | `ALL` | `true` | Un paiement dépend entièrement du contrat : même création, même suppression. L'orphanRemoval garantit qu'un paiement retiré de la liste est supprimé de la BDD. |
| Agence → Vehicule | `@OneToMany` | `Vehicule` (FK `agence_id`) | `LAZY` | Aucune | `false` | Un véhicule peut être réaffecté à une autre agence ou exister sans agence. Pas de cascade REMOVE : un véhicule survit à la fermeture d'une agence. |
| Agence → Employe | `@OneToMany` | `Employe` (FK `agence_id`) | `LAZY` | Aucune | `false` | Un employé peut changer d'agence. Pas de suppression automatique : un employé n'est pas lié au cycle de vie de l'agence. |
| Vehicule ↔ Equipement | `@ManyToMany` | `Vehicule` (table `vehicule_equipement`) | `LAZY` | Aucune | N/A | Les équipements sont partagés entre plusieurs véhicules. Supprimer un véhicule ne doit pas supprimer des équipements utilisés par d'autres. |
| Client → Reservation | `@OneToMany` | `Reservation` (FK `client_id`) | `LAZY` | `PERSIST` | `false` | Persister un nouveau client avec ses réservations en une seule opération. Pas de cascade REMOVE : les réservations ont une valeur archivistique indépendante. |
| Reservation → Vehicule | `@ManyToOne` | `Reservation` (FK `vehicule_id`) | `LAZY` | Aucune | N/A | Le véhicule existe indépendamment de la réservation. Pas de cascade pour éviter de supprimer un véhicule lors d'une annulation. |
| Reservation ↔ Contrat | `@OneToOne` | `Reservation` (FK `contrat_id`) | `LAZY` | `ALL` côté Reservation | N/A | Le contrat est intrinsèquement lié au cycle de vie de la réservation. Une réservation crée, modifie et supprime son contrat associé. |
| Vehicule → Maintenance | `@OneToMany` | `Maintenance` (FK `vehicule_id`) | `LAZY` | `PERSIST` | `false` | Persister un véhicule avec ses entrées de maintenance initiales en une seule opération. Pas de cascade REMOVE : l'historique de maintenance a de la valeur même après la revente du véhicule. |

---

## Explication détaillée par association

### 1. Contrat → Paiement (@OneToMany / @ManyToOne)

```java
// Dans Contrat (côté INVERSE)
@OneToMany(
    mappedBy = "contrat",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.LAZY
)
private List<Paiement> paiements = new ArrayList<>();

// Dans Paiement (côté PROPRIÉTAIRE – porte la FK)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "contrat_id")
private Contrat contrat;
```

**Pourquoi CascadeType.ALL ?**
Un paiement n'existe que dans le contexte d'un contrat. Lorsqu'un contrat est créé, ses paiements le sont aussi. Lorsqu'il est supprimé, ses paiements n'ont plus de raison d'exister.

**Pourquoi orphanRemoval = true ?**
Si on retire un paiement de `contrat.getPaiements()`, ce paiement devient orphelin (il n'a plus de contrat). Hibernate le supprime automatiquement. Sans `orphanRemoval`, le paiement resterait en base avec `contrat_id = NULL`.

**Différence cascade REMOVE vs orphanRemoval :**
- `CascadeType.REMOVE` : supprimer le contrat → supprimer ses paiements
- `orphanRemoval` : retirer un paiement de la collection → supprimer ce paiement

---

### 2. Agence → Vehicule (@OneToMany / @ManyToOne)

```java
// Dans Agence (côté INVERSE)
@OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)
private List<Vehicule> vehicules = new ArrayList<>();

// Dans Vehicule (côté PROPRIÉTAIRE)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "agence_id")
private Agence agence;
```

**Pourquoi aucune cascade ?**
Un véhicule est un bien de valeur qui peut être réaffecté ou vendu indépendamment de l'agence qui le gère. Fermer une agence ne doit pas supprimer automatiquement sa flotte de véhicules.

---

### 3. Agence → Employe (@OneToMany / @ManyToOne)

```java
// Dans Agence (côté INVERSE)
@OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)
private List<Employe> employes = new ArrayList<>();

// Dans Employe (côté PROPRIÉTAIRE)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "agence_id")
private Agence agence;
```

**Pourquoi aucune cascade ?**
Un employé peut être muté vers une autre agence. La suppression d'une agence ne justifie pas la suppression du dossier de l'employé.

---

### 4. Vehicule ↔ Equipement (@ManyToMany)

```java
// Dans Vehicule (côté PROPRIÉTAIRE – possède la table de jointure)
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "vehicule_equipement",
    joinColumns = @JoinColumn(name = "vehicule_id"),
    inverseJoinColumns = @JoinColumn(name = "equipement_id")
)
private Set<Equipement> equipements = new HashSet<>();

// Dans Equipement (côté INVERSE)
@ManyToMany(mappedBy = "equipements", fetch = FetchType.LAZY)
private Set<Vehicule> vehicules = new HashSet<>();
```

**Pourquoi Set et non List ?**
Pour une relation `@ManyToMany`, `Set` évite les doublons et est plus performant avec Hibernate (pas de DELETE + re-INSERT sur modification).

**Pourquoi aucune cascade ?**
Un équipement (GPS, climatisation...) est partagé entre plusieurs véhicules. Supprimer un véhicule ne doit pas supprimer un équipement qui équipe d'autres véhicules.

---

### 5. Client → Reservation (@OneToMany / @ManyToOne)

```java
// Dans Client (côté INVERSE)
@OneToMany(
    mappedBy = "client",
    fetch = FetchType.LAZY,
    cascade = CascadeType.PERSIST
)
private List<Reservation> reservations = new ArrayList<>();

// Dans Reservation (côté PROPRIÉTAIRE)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "client_id")
private Client client;
```

**Pourquoi uniquement PERSIST ?**
On veut pouvoir créer un client avec ses premières réservations en une seule opération. En revanche, supprimer un client ne doit pas supprimer son historique de réservations (valeur archivistique).

---

### 6. Reservation → Vehicule (@ManyToOne)

```java
// Dans Reservation (côté PROPRIÉTAIRE)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "vehicule_id")
private Vehicule vehicule;
```

**Pourquoi aucune cascade ?**
Le véhicule existe indépendamment de la réservation. Annuler une réservation ne doit surtout pas supprimer le véhicule.

---

### 7. Reservation ↔ Contrat (@OneToOne)

```java
// Dans Reservation (côté PROPRIÉTAIRE – porte la FK)
@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
@JoinColumn(name = "contrat_id", unique = true)
private Contrat contrat;
```

**Pourquoi CascadeType.ALL côté Reservation ?**
Le contrat de location est la concrétisation juridique de la réservation. Son cycle de vie est entièrement couplé à la réservation : une réservation confirmée génère un contrat, une annulation peut invalider le contrat.

---

### 8. Vehicule → Maintenance (@OneToMany / @ManyToOne)

```java
// Dans Vehicule (côté INVERSE)
@OneToMany(
    mappedBy = "vehicule",
    fetch = FetchType.LAZY,
    cascade = CascadeType.PERSIST
)
private List<Maintenance> maintenances = new ArrayList<>();

// Dans Maintenance (côté PROPRIÉTAIRE)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "vehicule_id")
private Vehicule vehicule;
```

**Pourquoi uniquement PERSIST ?**
On veut enregistrer un véhicule avec ses entrées de maintenance initiales en une seule opération. En revanche, l'historique de maintenance est un document légal qui doit être conservé même si le véhicule est revendu ou sorti du parc.

---

## Règles générales appliquées

| Règle | Explication |
|---|---|
| **LAZY par défaut** | Évite les chargements en cascade non maîtrisés. Les données ne sont chargées que si elles sont explicitement accédées. |
| **EAGER jamais par défaut** | Provoque des requêtes N+1 et des jointures inutiles. À utiliser uniquement si le cas métier l'exige absolument. |
| **mappedBy du côté inverse** | `mappedBy` dit à JPA que c'est l'autre entité qui gère la FK. L'oublier créerait une deuxième table de jointure ou une colonne parasite. |
| **Cascade selon le cycle de vie** | La cascade est justifiée uniquement si l'entité enfant n'a pas de sens sans son parent (ex. Paiement sans Contrat). |
| **orphanRemoval sur les dépendants stricts** | Utilisé uniquement pour Contrat→Paiement où un paiement sans contrat n'a aucun sens métier. |
| **Pas de @Data sur les entités** | @Data génère equals/hashCode/toString basés sur tous les champs, ce qui provoque des StackOverflowError avec les relations bidirectionnelles. |
| **Collections initialisées** | Initialiser avec new ArrayList<>() ou new HashSet<>() évite les NullPointerException et facilite les opérations add/remove. |
