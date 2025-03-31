package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;

import java.util.ArrayList;

public class EsgCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // E, S, G

    private String name;  //  "Environment"

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<EsgIndicator> indicators = new ArrayList<>();
}
