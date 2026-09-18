package com.akbar.ecommerce_backend.cart.service;

import com.akbar.ecommerce_backend.cart.dto.*;

public interface CartService {

    CartItemResponseDTO addItemToCart(CartItemRequestDTO requestDTO);

    CartResponseDTO getMyCart();

    CartItemResponseDTO updateCartItem(Long cartItemId, CartItemUpdateRequestDTO requestDTO);

    void removeCartItem(Long cartItemId);

    void clearCart();

    CartCountResponseDTO getCartCount();

}
