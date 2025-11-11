package com.moveinsync.sentiment.repository;

import com.moveinsync.sentiment.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SystemConfig entity
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {

    /**
     * Find configuration by key
     * @param configKey the configuration key
     * @return Optional SystemConfig
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * Find all configurations that start with a prefix
     * @param prefix the prefix to search for
     * @return List of SystemConfig
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configKey LIKE :prefix%")
    List<SystemConfig> findByConfigKeyStartingWith(String prefix);

    /**
     * Check if a configuration key exists
     * @param configKey the configuration key
     * @return true if exists, false otherwise
     */
    boolean existsByConfigKey(String configKey);

    /**
     * Delete configuration by key
     * @param configKey the configuration key
     */
    void deleteByConfigKey(String configKey);
}