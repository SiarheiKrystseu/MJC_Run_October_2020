package com.epam.esm.dao;

import com.epam.esm.exception.DaoException;
import com.epam.esm.model.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class GiftCertificateDaoImpl implements GiftCertificateDao {

    @Override
    public Optional<GiftCertificate> find(Long id) {
        Session session = HibernateAnnotationUtil.getSessionFactory().openSession();
        Optional<GiftCertificate> giftCertificate;
        try {
            String hql = "FROM GiftCertificate c WHERE c.id = :id";
            Query query = session.createQuery(hql);
            query.setParameter("id", id);
            giftCertificate = query.uniqueResultOptional();
        } catch (Exception e) {
            throw new DaoException(MessageFormat.format("Unable to get a certificate: {0}", e.getMessage()));
        } finally {
            session.close();
        }
        return giftCertificate;
    }

    @Override
    public Long save(GiftCertificate certificate) {
        Session session = HibernateAnnotationUtil.getSessionFactory().openSession();
        session.beginTransaction();
        try {
            session.persist(certificate);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            throw new DaoException(MessageFormat.format("Unable save certificate: {0}", e.getMessage()));
        } finally {
            session.close();
        }
        return certificate.getId();
    }

    @Override
    public void update(GiftCertificate certificate) {
        Session session = HibernateAnnotationUtil.getSessionFactory().openSession();
        session.beginTransaction();
        try {
            session.update(certificate);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            throw new DaoException(MessageFormat.format("Unable update certificate: {0}", e.getMessage()));
        } finally {
            session.close();
        }
    }

    @Override
    public void delete(Long id) {
        Session session = HibernateAnnotationUtil.getSessionFactory().openSession();
        session.beginTransaction();
        try {
            String hql = "DELETE GiftCertificate c WHERE c.id = :id";
            Query query = session.createQuery(hql);
            query.setParameter("id", id);
            query.executeUpdate();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            throw new DaoException(MessageFormat.format("Unable delete certificate: {0}", e.getMessage()));
        } finally {
            session.close();
        }
    }

    @Override
    public List<GiftCertificate> findAll(Long page, Long pageSize) {
        Session session = HibernateAnnotationUtil.getSessionFactory().openSession();
        List<GiftCertificate> firstPage = new ArrayList<>();
        try {
            Criteria criteria = session.createCriteria(GiftCertificate.class);
            criteria.setFirstResult(Math.toIntExact((page - 1) * pageSize));
            criteria.setMaxResults(Math.toIntExact(pageSize));
            firstPage = criteria.list();
        } catch (Exception e) {
            throw new DaoException(MessageFormat.format("Unable to get a list of certificates: {0}", e.getMessage()));
        } finally {
            session.close();
        }
        return firstPage;
    }

    @Override
    public List<GiftCertificate> getCertificates(CertificateSearchQuery query, Long page, Long pageSize) {

        Session session = HibernateAnnotationUtil.getSessionFactory().openSession();
        List<GiftCertificate> firstPage = new ArrayList<>();
        try {
            Criteria criteria = session.createCriteria(GiftCertificate.class);
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<GiftCertificate> criteriaQuery = criteriaBuilder.createQuery(GiftCertificate.class);
            Root<GiftCertificate> certificateRoot = criteriaQuery.from(GiftCertificate.class);


            if (query.hasTagName()) {
                criteria.createAlias("tags", "tag");
                criteria.add(Restrictions.eq("tag.name", query.getTagName()));
                criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

            }
            if (query.hasPartOfDescription()) {
                criteria.add(Restrictions.like("description", "%" + query.getPartOfDescription() + "%"));

            }
            if (query.hasPartOfName()) {
                criteria.add(Restrictions.like("name", "%" + query.getPartOfName() + "%"));
            }
            if (query.hasSortParameter()) {
                if ("DESC".equals(query.getSortOrder())) {
                    criteria.addOrder(org.hibernate.criterion.Order.desc(query.getSortParameter()));
                } else {
                    criteria.addOrder(org.hibernate.criterion.Order.asc(query.getSortParameter()));
                }
            }
            criteria.setFirstResult(Math.toIntExact((page - 1) * pageSize));
            criteria.setMaxResults(Math.toIntExact(pageSize));
            firstPage = criteria.list();
        } catch (Exception e) {
            throw new DaoException(MessageFormat.format("Unable to get a list of certificates: {0}", e.getMessage()));
        } finally {
            session.close();
        }
        return firstPage;
    }

    @Override
    public List<GiftCertificate> findCertificatesByTags(List<String> tagNames, Long page, Long pageSize) {
        Session session = HibernateAnnotationUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List<GiftCertificate> firstPage = new ArrayList<>();
        try {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<GiftCertificate> criteriaQuery = criteriaBuilder.createQuery(GiftCertificate.class);
            Root<GiftCertificate> certificateRoot = criteriaQuery.from(GiftCertificate.class);
            Join<GiftCertificate, Tag> tagsJoin = certificateRoot.join(GiftCertificate_.tags);
            criteriaQuery.where(tagsJoin.get(Tag_.NAME).in(tagNames));
            criteriaQuery.select(certificateRoot).distinct(true);

            firstPage = session.createQuery(criteriaQuery)
                    .setFirstResult(Math.toIntExact(Math.toIntExact((page - 1) * pageSize)))
                    .setMaxResults(Math.toIntExact(Math.toIntExact(pageSize)))
                    .getResultList();

        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new DaoException(MessageFormat.format("Unable to get a list of certificates: {0}", e.getMessage()));
        } finally {
            session.getTransaction().commit();
            session.close();
        }
        return firstPage;
    }
}
