package com.epam.esm.dao;

import com.epam.esm.exception.DaoException;
import com.epam.esm.model.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    @Override
    public Optional<User> find(Long id) {
        return Optional.empty();
    }

    @Override
    public Long save(User model) {
        return null;
    }

    @Override
    public void update(User model) {

    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public List<User> findAll(int from, int pageSize) {
        Session session = HibernateAnnotationUtil.getSessionFactory().openSession();
        List<User> firstPage = new ArrayList<>();
        try {
            Criteria criteria = session.createCriteria(User.class);
            criteria.setFirstResult(from);
            criteria.setMaxResults(pageSize);
            firstPage = criteria.list();
        } catch (Exception e) {
            throw new DaoException(MessageFormat.format("Unable to get a list of users: {0}", e.getMessage()));
        } finally {
            session.close();
        }
        return firstPage;
    }
}
