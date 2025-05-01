package repsy.repository;

import repsy.model.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    Optional<Package> findByNameAndVersion(String name, String version);

    boolean existsByNameAndVersion(String name, String version);
}