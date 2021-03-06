package dbService;

import base.DBService;
import dao.DAO;
import dao.DaoFactory;
import datasets.AddressDataSet;
import datasets.DataSet;
import datasets.PhoneDataSet;
import datasets.UserDataSet;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class DBServiceHibernateImpl implements DBService {
    private final SessionFactory sessionFactory;

    public DBServiceHibernateImpl() {

        Configuration configuration = new Configuration()
                //.configure(new File("HW141WAR/src/main/resources/hibernate.cfg.xml"));
                //.configure(new File("hibernate.cfg.xml"));
                .configure("hibernate.cfg.xml");

        configuration.addAnnotatedClass(DataSet.class);
        configuration.addAnnotatedClass(UserDataSet.class);
        configuration.addAnnotatedClass(PhoneDataSet.class);
        configuration.addAnnotatedClass(AddressDataSet.class);

        sessionFactory = createSessionFactory(configuration);
    }

    private static SessionFactory createSessionFactory(Configuration configuration) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    @Override
    public <T extends DataSet> void save(T user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.getTransaction();
            transaction.begin();
            DAO dao = DaoFactory.getDataSetDAO(user.getClass(), session);
            dao.save(user);
            transaction.commit();

        }
    }

    @Override
    public <T extends DataSet> T load(long id, Class<T> clazz) {
        try (Session session = sessionFactory.openSession()) {
            DAO dao = DaoFactory.getDataSetDAO(clazz, session);
            return  dao.load(id, clazz);
        }
    }

    @Override
    public <T extends DataSet> List<T> readAll(Class<T> clazz) {
        try (Session session = sessionFactory.openSession()) {
            DAO dao = DaoFactory.getDataSetDAO(clazz, session);
            return dao.readAll(clazz);
        }
    }

    @Override
    public void close() throws Exception {
        sessionFactory.close();
    }
}