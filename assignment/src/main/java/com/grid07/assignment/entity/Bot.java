package com.grid07.assignment.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "bots")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String name;
    private String persona_description;

}
