package com.icl.auth.repository;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserRepository implements UserRepository {
    EntityManager em;

    @Autowired
    public JpaUserRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(em.createNamedQuery(User.GET_BY_LOGIN, User.class)
                .setParameter("login", login)
                .getSingleResult());
    }

    @Override
    public User save(User user) {
        if (user.isNew()) {
            em.persist(user);
            return user;
        } else {
            return em.merge(user);
        }
    }

    @Override
    public void deleteById(Long id) throws UserNotFoundException {
        User user;
        if ((user = em.find(User.class, id)) != null) {
            em.remove(user);
        } else {
            throw new UserNotFoundException(id);
        }
    }

    @Override
    public List<User> findAll() {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> rootEntry = criteriaQuery.from(User.class);
        CriteriaQuery<User> all = criteriaQuery.select(rootEntry);
        TypedQuery<User> findAll = em.createQuery(all);
        return findAll.getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        return em.find(User.class, id) != null;
    }
}
