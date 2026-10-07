package com.akbar.ecommerce_backend.cart.service;


import com.akbar.ecommerce_backend.cart.dto.*;
import com.akbar.ecommerce_backend.cart.entity.CartEntity;
import com.akbar.ecommerce_backend.cart.entity.CartItemEntity;
import com.akbar.ecommerce_backend.cart.mapper.CartMapper;
import com.akbar.ecommerce_backend.cart.repository.CartItemRepository;
import com.akbar.ecommerce_backend.cart.repository.CartRepository;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.product.entity.ProductEntity;
import com.akbar.ecommerce_backend.product.enums.ProductStatus;
import com.akbar.ecommerce_backend.product.repository.ProductRepository;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;




    @Override
    @Transactional
    public CartItemResponseDTO addItemToCart(CartItemRequestDTO requestDTO) {

        Authentication authentication = 
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.debug("Adding product to cart. User email: {}, Product ID: {}, Quantity: {}",
                email,
                requestDTO.getProductId(),
                requestDTO.getQuantity());

        
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + email));

        
        ProductEntity product = productRepository.findByIdAndStatus(requestDTO.getProductId(),
                        ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id: "
                                        + requestDTO.getProductId()));

        
        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    
                    log.debug("No cart found for user. Creating new cart. User ID: {}",
                            user.getId());

                    CartEntity newCart = CartEntity.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(newCart);
                });


        CartItemEntity cartItem = cartItemRepository.findByCartIdAndProductId(
                cart.getId(), product.getId())
                .orElseGet(() -> {

                    log.debug("Product not present in cart. Creating new cart item. Cart ID: {}, Product ID: {}",
                            cart.getId(),
                            product.getId());

                    return CartItemEntity.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(0)
                            .build();
                });


        cartItem.setQuantity(
                cartItem.getQuantity() + requestDTO.getQuantity());


        CartItemEntity savedCartItem =
                cartItemRepository.save(cartItem);


        log.debug("Product added to cart successfully. Cart ID: {}, Cart Item ID: {}, Product ID: {}, Quantity: {}",
                cart.getId(),
                savedCartItem.getId(),
                product.getId(),
                savedCartItem.getQuantity());

        return cartMapper.toCartItemResponseDTO(
                savedCartItem);

    }





    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getMyCart() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.debug("Fetching cart for user. User email: {}",
                email);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + email));


        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No cart found for user id: " + user.getId()));

        log.debug("Cart fetched successfully. Cart ID: {}, User ID: {}",
                cart.getId(),
                user.getId());

        return cartMapper.tocartResponseDTO(cart);

    }





    @Override
    @Transactional
    public CartItemResponseDTO updateCartItem(Long cartItemId, CartItemUpdateRequestDTO requestDTO) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.debug("Updating cart item. User email: {}, Cart Item ID: {}, New Quantity: {}",
                email,
                cartItemId,
                requestDTO.getQuantity());


        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No user found with email: " + email));


        CartItemEntity cartItem = cartItemRepository.findByIdAndCartUserId(cartItemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No cart item found with id: " + cartItemId));


        cartItem.setQuantity(
                requestDTO.getQuantity());

        CartItemEntity updatedCartItem =
                cartItemRepository.save(cartItem);

        log.debug("Cart item updated successfully. Cart Item ID: {}, User ID: {}, New Quantity: {}",
                updatedCartItem.getId(),
                user.getId(),
                updatedCartItem.getQuantity());

        return cartMapper.toCartItemResponseDTO(updatedCartItem);

    }







    @Override
    @Transactional
    public void removeCartItem(Long cartItemId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.debug("Removing cart item. User email: {}, Cart Item ID: {}",
                email,
                cartItemId);


        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No user found with email: " + email));


        CartItemEntity cartItem = cartItemRepository.findByIdAndCartUserId(
                cartItemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No cart item found with id: " + cartItemId));


        cartItemRepository.delete(cartItem);

        log.debug("Cart item removed successfully. Cart Item ID: {}, User ID: {}",
                cartItemId,
                user.getId());

    }






    @Override
    @Transactional
    public void clearCart() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.debug("Clearing cart. User email: {}",
                email);


        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No user found with email: " + email));


        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No cart found for user id: " + user.getId()));


        cart.getCartItems().clear();

        log.debug("Cart cleared successfully. Cart ID: {}, User ID: {}",
                cart.getId(),
                user.getId());

    }






    @Override
    @Transactional(readOnly = true)
    public CartCountResponseDTO getCartCount() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        log.debug("Fetching cart count. User email: {}",
                email);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No user found with email: " + email));

        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                                "No cart found for user id: " + user.getId()));


        int totalItems = cart.getCartItems()
                .stream()
                .mapToInt(CartItemEntity::getQuantity)
                .sum();

        log.debug("Cart count fetched successfully. Cart ID: {}, User ID: {}, Total Items: {}",
                cart.getId(),
                user.getId(),
                totalItems);

        return cartMapper.tocartCountResponseDTO(totalItems);

    }




}