package manager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import model.Bodega;
import model.Campo;
import model.Entrada;
import model.Vid;
import utils.TipoVid;

public class Manager {
	private static Manager manager;
	private ArrayList<Entrada> entradas;
	private Session session;
	private Transaction tx;
	private Bodega b;
	private Campo c;
	private ArrayList<Vid> vidsPendientes;

	private Manager () {
		this.vidsPendientes = new ArrayList<>();
		this.entradas = new ArrayList<>();
	}
	
	public static Manager getInstance() {
		if (manager == null) {
			manager = new Manager();
		}
		return manager;
	}
	
	private void createSession() {
		Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
		org.hibernate.SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
    	session = sessionFactory.openSession();
	}

	public void init() {
		createSession();
		getEntrada();
		manageActions();
		showAllCampos();
		showSumOfPrices();
		session.close();
	}
	
	public void addVidToPendingList(Vid v) {
	    this.vidsPendientes.add(v);
	}

	private void manageActions() {
		for (Entrada entrada : this.entradas) {
			try {
				System.out.println(entrada.getInstruccion());
				switch (entrada.getInstruccion().toUpperCase().split(" ")[0]) {
					case "B":
						addBodega(entrada.getInstruccion().split(" "));
						break;
					case "C":
						addCampo(entrada.getInstruccion().split(" "));
						break;
					case "V":
						addVid(entrada.getInstruccion().split(" "));
						break;
					case "#":
						vendimia();
						break;
					default:
						System.out.println("Instruccion incorrecta");
				}
			} catch (HibernateException e) {
				e.printStackTrace();
				if (tx != null) {
					tx.rollback();
				}
			}
		}
	}

	private void vendimia() {
	    this.b.getVids().addAll(vidsPendientes);

	    tx = session.beginTransaction();
	    for (Vid v : vidsPendientes) {
	        session.saveOrUpdate(v);
	    }
	    session.saveOrUpdate(b);
	    tx.commit();

	    vidsPendientes.clear();
	}

	private void addVid(String[] split) {
	    String[] parts = split[3].split(" ");
	    BigDecimal valor = new BigDecimal(parts[0].replace(",", "."));
	    Vid v = new Vid(TipoVid.valueOf(split[1].toUpperCase()), Integer.parseInt(split[2]), valor);
	    tx = session.beginTransaction();
	    session.save(v);

	    c.addVid(v);
	    session.save(c);

	    tx.commit();
	}


	private void addCampo(String[] split) {
		c = new Campo(b);
		tx = session.beginTransaction();
		
		int id = (Integer) session.save(c);
		c = session.get(Campo.class, id);
		
		tx.commit();
	}

	private void addBodega(String[] split) {
		b = new Bodega(split[1]);
		tx = session.beginTransaction();
		
		int id = (Integer) session.save(b);
		b = session.get(Bodega.class, id);
		
		tx.commit();
		
	}

	private void getEntrada() {
		tx = session.beginTransaction();
		Query q = session.createQuery("select e from Entrada e");
		this.entradas.addAll(q.list());
		tx.commit();
	}

	private void showAllCampos() {
		tx = session.beginTransaction();
		Query q = session.createQuery("select c from Campo c");
		List<Campo> list = q.list();
		for (Campo c : list) {
			System.out.println(c);
		}
		tx.commit();
	}
	
	private void showSumOfPrices() {
	    tx = session.beginTransaction();
	    Query q = session.createQuery("select sum(v.price) from Vid v");
	    BigDecimal sum = (BigDecimal) q.uniqueResult();
	    
	    if (sum != null) {
	        System.out.println("-Suma de precios de todas las Vid: " + sum +"€");
	    } else {
	        System.out.println("No hay Vids en la base de datos.");
	    }
	    
	    tx.commit();
	}


	
}

