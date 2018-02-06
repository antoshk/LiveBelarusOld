/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.live_belarus;
import org.hibernate.SessionFactory; 
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;

import antoshk.instagram.entity.*;

/**
 *
 * @author User
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        try {
            
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
             .configure()
             .build();

             Metadata metadata = new MetadataSources( standardRegistry )
             .addAnnotatedClass(State.class) 
             .addAnnotatedClass(User.class) 
             .addAnnotatedClass(TempTag.class)
             .addAnnotatedClass(TagToUser.class)
             .addAnnotatedClass(CoreTag.class)
             .addAnnotatedClass(TagLink.class)
             // You can add more entity classes here like above
             .getMetadataBuilder()
             .applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE )
             .build();

             sessionFactory = metadata.getSessionFactoryBuilder().build();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }    
}
