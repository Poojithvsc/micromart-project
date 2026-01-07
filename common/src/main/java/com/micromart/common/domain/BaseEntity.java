package com.micromart.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base entity class implementing the Layer Supertype pattern from PEAA.
 * <p>
 * PEAA Pattern: Layer Supertype
 * A type that acts as the supertype for all types in its layer.
 * <p>
 * All domain entities should extend this class to inherit:
 * - Primary key (id) with auto-generation
 * - Consistent equals/hashCode based on id
 * - Serializable support
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/layerSupertype.html">Layer Supertype</a>
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key using database sequence/identity generation.
     * Using Long for better range and compatibility with PostgreSQL BIGSERIAL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Check if the entity is new (not yet persisted).
     *
     * @return true if the entity has no id yet
     */
    public boolean isNew() {
        return this.id == null;
    }

    /**
     * Equality based on entity id.
     * Two entities are equal if they have the same non-null id.
     * New entities (without id) are only equal to themselves.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        // New entities are only equal to themselves
        if (this.id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    /**
     * Hash code based on entity class.
     * Using class-based hash to ensure consistency across entity lifecycle.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
