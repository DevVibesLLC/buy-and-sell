package am.devvibes.buyandsell.entity.cart;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private ItemEntity item;

}