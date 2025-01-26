package fr.adriencaubel.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<LigneDetail> ligneDetails = new ArrayList<LigneDetail>();

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<LigneDetail> getLigneDetails() {
        return ligneDetails;
    }

    public void setLigneDetails(List<LigneDetail> ligneDetails) {
        this.ligneDetails = ligneDetails;
    }
}

