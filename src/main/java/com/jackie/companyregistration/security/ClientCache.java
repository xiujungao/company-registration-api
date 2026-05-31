package com.jackie.companyregistration.security;

import com.jackie.companyregistration.repository.ClientRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * In-memory index of API keys to client IDs for {@link ApiKeyAuthFilter}.
 * <p>
 * All rows from the {@code clients} table are loaded when the application is ready
 * ({@link ApplicationReadyEvent}), after SQL seed scripts have run. Lookups use an
 * unmodifiable map so the cache is a fixed snapshot until {@link #reload()} or process restart.
 * <p>
 * Keys are compared with {@link MessageDigest#isEqual(byte[], byte[])} to avoid timing
 * leaks when validating secrets. After changing {@code clients} in the database, call
 * {@code POST /api/admin/cache/clients/reload} on each running instance (or restart).
 */
@Component
public class ClientCache {

    private final ClientRepository clientRepository;
    private Map<String, String> clientIdByApiKey = Map.of();

    /**
     * @param clientRepository repository used to load rows from {@code clients}
     */
    public ClientCache(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Performs the initial cache load after the application (and deferred SQL scripts) is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    void loadOnStartup() {
        reload();
    }

    /**
     * Reloads the in-memory index from {@code clients}.
     *
     * @return number of clients loaded
     */
    public int reload() {
        var map = new HashMap<String, String>();
        for (var client : clientRepository.findAll()) {
            map.put(client.getApiKey(), client.getClientId());
        }
        clientIdByApiKey = Map.copyOf(map);
        return clientIdByApiKey.size();
    }

    /**
     * Resolves a client ID for the given API key, if valid.
     *
     * @param providedKey value from the {@code X-API-Key} request header
     * @return matching client ID, or empty if the key is unknown
     */
    public Optional<String> findClientIdByApiKey(String providedKey) {
        for (var entry : clientIdByApiKey.entrySet()) {
            if (keysMatch(providedKey, entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Constant-time comparison of two API key strings.
     *
     * @param provided key from the request
     * @param stored   key from the cache
     * @return {@code true} if the keys match
     */
    private static boolean keysMatch(String provided, String stored) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8)
        );
    }

}
