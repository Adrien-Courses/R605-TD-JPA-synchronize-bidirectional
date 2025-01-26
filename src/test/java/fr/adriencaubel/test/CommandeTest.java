package fr.adriencaubel.test;

import org.junit.jupiter.api.*;

import fr.adriencaubel.entity.Commande;
import fr.adriencaubel.entity.LigneDetail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class CommandeTest {

	private void initializeDatabase(EntityManager em) {
		em.getTransaction().begin();

		Commande commande = new Commande();

		LigneDetail ligneDetail1 = new LigneDetail();
		LigneDetail ligneDetail2 = new LigneDetail();

		// Ajouter des deux côtés de la relation
		ligneDetail1.setCommande(commande);
		ligneDetail2.setCommande(commande);
		commande.getLigneDetails().add(ligneDetail1);
		commande.getLigneDetails().add(ligneDetail2);

		// Comme CASCADE.ALL seulement besoin de préciser la commande
		em.persist(commande);

		em.getTransaction().commit();
	}

	@Test
	public void testRemoveLigneDetailNotWorking() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-bidirectional");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		initializeDatabase(em);

		transaction.begin();

		Commande commande = em.find(Commande.class, 1L);

		LigneDetail ligneDetail = commande.getLigneDetails().get(0);

		em.remove(ligneDetail); // Supprimer la ligne en BDD ?? Not working

		transaction.commit();
	}

	@Test
	public void testRemoveLigneDetailInsuffisant() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-bidirectional");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		initializeDatabase(em);

		transaction.begin();

		Commande commande = em.find(Commande.class, 1L);

		LigneDetail ligneDetail = commande.getLigneDetails().get(0);
		ligneDetail.setCommande(null); // THIS ADDED

		em.remove(ligneDetail); // Supprimer la ligne en BDD ?? Non dé-association via NULL

		transaction.commit();
	}

	@Test
	public void testRemoveLigneDetailWorking() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-bidirectional");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		initializeDatabase(em);

		transaction.begin();

		Commande commande = em.find(Commande.class, 1L);

		LigneDetail ligneDetail = commande.getLigneDetails().get(0);
		commande.getLigneDetails().remove(ligneDetail); // THIS ADDED

		em.remove(ligneDetail); // Working

		transaction.commit();
	}

	@Test
	public void testRemoveLigneDetailWorkingConventional() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-bidirectional");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		initializeDatabase(em);

		transaction.begin();

		Commande commande = em.find(Commande.class, 1L);

		LigneDetail ligneDetail = commande.getLigneDetails().get(0);
		commande.getLigneDetails().remove(ligneDetail); // THIS ADDED
		ligneDetail.setCommande(null); // THIS ADDED
		
		em.remove(ligneDetail); // Working

		transaction.commit();
	}
}