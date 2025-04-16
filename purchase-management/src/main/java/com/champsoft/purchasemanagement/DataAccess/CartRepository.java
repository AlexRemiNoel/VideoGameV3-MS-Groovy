package com.champsoft.purchasemanagement.DataAccess;


import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, String> {
    Cart findCartByCartId_Uuid(String cartId);
}
