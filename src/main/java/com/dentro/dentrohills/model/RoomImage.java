package com.dentro.dentrohills.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "room_image")
public class RoomImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}
