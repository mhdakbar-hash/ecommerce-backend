package com.akbar.ecommerce_backend.user.service;

import com.akbar.ecommerce_backend.exception.EmailAlreadyExistsException;
import com.akbar.ecommerce_backend.exception.ResourceNotFoundException;
import com.akbar.ecommerce_backend.common.PageResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserRequestDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserResponseDTO;
import com.akbar.ecommerce_backend.user.dto.user.UserUpdateRequestDTO;
import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.enums.Role;
import com.akbar.ecommerce_backend.user.enums.UserStatus;
import com.akbar.ecommerce_backend.user.mapper.UserMapper;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import com.akbar.ecommerce_backend.user.specification.UserSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }



    @Override
    @Transactional
    public UserResponseDTO registerUser(UserRequestDTO requestDTO) {

        String email = requestDTO.getEmail().trim().toLowerCase();

        log.info("User registration requested.");

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        UserEntity user= userMapper.toEntity(requestDTO);

        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setEmail(email);
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        UserEntity savedUser = userRepository.save(user);

        log.info("User registered successfully. User ID: {}", savedUser.getId());

        return userMapper.toResponseDTO(savedUser);
    }




    @Override
    public UserResponseDTO getUserById(Long id) {

        UserEntity user = userRepository.findByIdAndStatusNot(id, UserStatus.DELETED)
                .orElseThrow(()-> new ResourceNotFoundException("No user found with id: " + id));

        return userMapper.toResponseDTO(user);
    }




    @Override
    public PageResponseDTO<UserResponseDTO> getAllUsers(String keyword, UserStatus status, Pageable pageable) {

        log.debug("Fetching users. Keyword: {}, Status: {}, Page: {}, Size: {}",
                keyword,
                status,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Specification<UserEntity> specification = UserSpecification.isNotDeleted();


        if (keyword != null && !keyword.isBlank()){

            specification = specification.and(
                    UserSpecification.hasKeyword(keyword)
            );
        }

        if (status != null){

            specification = specification.and(
                    UserSpecification.hasStatus(status)
            );
        }

        Page<UserEntity> users = userRepository.findAll(specification, pageable);

        log.debug("Fetched {} users out of {} total users. Page: {}, Size: {}",
                users.getNumberOfElements(),
                users.getTotalElements(),
                users.getNumber(),
                users.getSize());

        List<UserResponseDTO> response = users.getContent()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();

        return new PageResponseDTO<>(
                response,
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isFirst(),
                users.isLast()
        );


        /*return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)       //- old code
                .toList();*/
    }


        @Override
        @Transactional
        public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestDTO) {

            UserEntity user = userRepository.findByIdAndStatusNot(id, UserStatus.DELETED)
                    .orElseThrow(()-> new ResourceNotFoundException("No user found with id: " + id));

            log.info("Updating user. User ID: {}", id);

            user.setFirstName(requestDTO.getFirstName());
            user.setLastName(requestDTO.getLastName());
            user.setPhoneNumber(requestDTO.getPhoneNumber());

            userRepository.save(user);

            log.info(
                    "User updated successfully. User ID: {}",
                    user.getId());

            return userMapper.toResponseDTO(user);

        }



    @Override
    @Transactional
    public void deleteUser(Long id) {

        UserEntity user = userRepository.findByIdAndStatusNot(id, UserStatus.DELETED)
                .orElseThrow(()-> new ResourceNotFoundException("No user found with id: " + id));


        log.info("Soft delete requested. User ID: {}", user.getId());

        user.setStatus(UserStatus.DELETED);

        userRepository.save(user);

        log.info("User soft deleted successfully. User ID: {}", user.getId());

    }


    @Override
    @Transactional
    public UserResponseDTO restoreUser(Long id) {

        UserEntity user = userRepository.findByIdAndStatus(id, UserStatus.DELETED)
                .orElseThrow(()-> new ResourceNotFoundException("No user found with id: " + id));


        log.info("Restore requested. User ID: {}", user.getId());

        user.setStatus(UserStatus.ACTIVE);

        UserEntity savedUser = userRepository.save(user);

        log.info("User restored successfully. User ID: {}", savedUser.getId());

        return userMapper.toResponseDTO(savedUser);

    }
}
