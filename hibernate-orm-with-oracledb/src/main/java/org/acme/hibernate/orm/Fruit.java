package org.acme.hibernate.orm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;

import java.util.Date;

@Entity
@NamedQuery(name = "Fruits.findAll", query = "SELECT f FROM Fruit f ORDER BY f.name")
public class Fruit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fruit_seq")
    @SequenceGenerator(name = "fruit_seq", sequenceName = "FRUIT_SEQ", allocationSize = 1, initialValue = 10)
    private Integer id;

    @Column(length = 40, unique = true)
    private String name;

    public Fruit() {
    }

    public Fruit(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
