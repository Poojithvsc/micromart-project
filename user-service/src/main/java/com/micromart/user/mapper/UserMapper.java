package com.micromart.user.mapper;

import com.micromart.user.domain.User;
import com.micromart.user.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * User Mapper - DTO to Entity mapping.
 * <p>
 * MapStruct generates implementation at compile time.
 * Configured via maven-compiler-plugin in parent POM.
 * <p>
 * @Mapper(componentModel = "spring") makes it a Spring bean
 * that can be injected with @Autowired or constructor injection.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Map User entity to UserResponse DTO.
     * <p>
     * @Mapping handles nested property mapping.
     * email.value extracts the String value from Email value object.
     */
    @Mapping(source = "email.value", target = "email")
    UserResponse toResponse(User user);
}
