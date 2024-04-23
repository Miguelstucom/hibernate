package manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

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
	MongoDatabase database;
	MongoCollection<Document> collection;
	MongoCollection<Document> collections;
	private MongoClient mongoClient;
	ArrayList<Entrada> inputs = new ArrayList<>();

	private Manager() {
		this.entradas = new ArrayList<>();
	}

	public static Manager getInstance() {
		if (manager == null) {
			manager = new Manager();
		}
		return manager;
	}

	public void connect() {
		String uri = "mongodb://localhost:27017";
		MongoClientURI mongoClientURI = new MongoClientURI(uri);
		try {
			mongoClient = new MongoClient(mongoClientURI);
			database = mongoClient.getDatabase("dam2tm06uf2p2");
			if (database != null) {
				System.out.println("Conexión establecida con éxito.");
			}
		} catch (MongoException e) {
			System.err.println("No se pudo conectar a MongoDB: " + e.getMessage());
			if (mongoClient != null) {
				mongoClient.close();
			}
		}
	}

	public void closeConnection() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	public ArrayList<Entrada> getInputData() {
		MongoCollection<Document> collection = database.getCollection("Entrada");

		for (Document document : collection.find()) {
			Entrada input = new Entrada();
			input.setId(document.getObjectId("_id").toString());
			input.setInstruccion(document.getString("instruccion"));
			inputs.add(input);
		}
		System.out.println(inputs);
		return inputs;
	}

	public void init() {
		connect();
		try {
			getInputData();
			manageActions();
			getEntradaMongo();
			showAllCamposMongo();
			showSumOfPricesMongo();
		} finally {
			closeConnection();
		}
	}

	private void manageActions() {
		for (Entrada entrada : this.inputs) {
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
					// vendimia();
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
		this.b.getVids().addAll(this.c.getVids());

		tx = session.beginTransaction();
		session.save(b);

		tx.commit();
	}

	private void addBodega(String[] split) {
		Document bodega = new Document("nombre", split[1]);
		collection = database.getCollection("Bodegas");
		collection.insertOne(bodega);
		System.out.println("Bodega añadida con éxito: " + bodega.toJson());
	}

	private void addCampo(String[] split) {
		collection = database.getCollection("Bodegas");
		Document bodega = collection.find().sort(Sorts.descending("_id")).first();
		if (bodega != null) {
			ObjectId bodegaId = bodega.getObjectId("_id");
			Document campo = new Document("id_bodega", bodegaId);
			collection = database.getCollection("Campos");
			collection.insertOne(campo);
			System.out.println("campo añadido con exito a la ultima Bodega insertada: " + bodega.getString("nombre"));
		} else {
			System.out.println("No se encontro ninguna Bodega en la base de datos.");
		}
	}

	private void addVid(String[] split) {
	    collection = database.getCollection("Campos");
	    Document campo = collection.find().sort(Sorts.descending("_id")).first();
	    if (campo != null) {
	        Document vid = new Document("tipo_vid", split[1])
	                       .append("cantidad", Integer.parseInt(split[2]))
	                       .append("price", new BigDecimal(split[3].replace(",", ".")))
	                       .append("campo", campo);
	        collection = database.getCollection("Vids");
	        collection.insertOne(vid);
	    } else {
	        System.out.println("Campo no encontrado: " + split[4]);
	    }
	}


	private void getEntradaMongo(){
		collection = database.getCollection("Entrada");
        FindIterable<Document> documents = collection.find();

        for (Document doc : documents) {
            System.out.println(doc.toJson());
        }
	}
	
    private void showAllCamposMongo() {
		collection = database.getCollection("Campos");
        FindIterable<Document> documents = collection.find();

        for (Document doc : documents) {
            System.out.println(doc.toJson());
        }
    }

    private void showSumOfPricesMongo() {
        MongoCollection<Document> collection = database.getCollection("Vids");
        List<Bson> pipeline = Arrays.asList(
            Aggregates.group(null, Accumulators.sum("totalPrice", "$price"))
        );
        for (Document doc : collection.aggregate(pipeline)) {
            BigDecimal sumOfPrices = doc.get("totalPrice", Decimal128.class).bigDecimalValue();
            System.out.println("Suma total de precios: " + sumOfPrices);
        }
    }


}
