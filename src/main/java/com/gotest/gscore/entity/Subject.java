package com.gotest.gscore.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "subjects")
public class Subject {
    @Id
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code; // match header "toan", "hoa_hoc"

    @Column(name = "name", length = 100)
    private String name; // match header, too

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> scores = new ArrayList<>();
}

