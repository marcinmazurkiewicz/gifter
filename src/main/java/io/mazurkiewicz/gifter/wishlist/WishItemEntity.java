package io.mazurkiewicz.gifter.wishlist;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity(name = "wish_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WishItemEntity {

    @Id
    @GeneratedValue
    Long id;
    UUID itemPublicId;
    String userPublicId;
    String name;
    String link;

    public WishItemEntity(String userPublicId, String name, String link) {
        this.itemPublicId = UUID.randomUUID();
        this.userPublicId = userPublicId;
        this.name = name;
        this.link = link;
    }
}
