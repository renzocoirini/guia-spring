package dev.renzo.crud.repository;

import dev.renzo.crud.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<dev.renzo.crud.entity.Producto, Integer> {
    
    Optional<Producto> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}