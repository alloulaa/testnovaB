package com.example.testnova.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity

public class CV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCV;
}